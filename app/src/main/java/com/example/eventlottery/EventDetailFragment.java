package com.example.eventlottery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventlottery.controller.WaitingListController;
import com.example.eventlottery.data.CommentRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.EventComment;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.firebase.FirestoreCommentRepository;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.ui.EventCommentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * EventDetailFragment
 *
 * Displays detailed information for a selected event including poster image.
 *
 * User stories supported:
 * - US 01.05.04: Know how many total entrants are on the waiting list
 * - US 01.06.02: Sign up for an event from the event details
 * - US 01.02.01: Entrant provides personal information
 * - US 01.02.02: Entrant updates profile information
 * - US 01.07.01: User is identified by device
 * - US 01.08.01: Post a comment on an event
 * - US 01.08.02: View comments on an event
 *
 * @author Edwin David, Kenneth Joseph, Fawaz Mansoor
 * @version 1.3
 */
public class EventDetailFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_ORGANIZER_DEVICE_ID = "organizerDeviceId";
    private static final String ARG_POSTER_URL = "posterUrl";

    private String eventId;
    private String eventName;
    private String description;
    private String organizerDeviceId;
    private String posterUrl;

    private WaitingListController waitingListController;
    private String deviceId;

    public EventDetailFragment() {}

    public static EventDetailFragment newInstance(String eventId,
                                                  String eventName,
                                                  String description,
                                                  String organizerDeviceId,
                                                  String posterUrl) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_ORGANIZER_DEVICE_ID, organizerDeviceId);
        args.putString(ARG_POSTER_URL, posterUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
            description = getArguments().getString(ARG_DESCRIPTION);
            organizerDeviceId = getArguments().getString(ARG_ORGANIZER_DEVICE_ID);
            posterUrl = getArguments().getString(ARG_POSTER_URL);
        }
        waitingListController = new WaitingListController(new FirestoreWaitListRepository());
        deviceId = DeviceIdentityService.getDeviceId(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_detail, container, false);

        TextView textName = view.findViewById(R.id.text_detail_event_name);
        TextView textDescription = view.findViewById(R.id.text_detail_description);
        TextView textWaitCount = view.findViewById(R.id.text_waiting_count);
        Button buttonJoin = view.findViewById(R.id.button_join_waitlist);
        ImageView ivPoster = view.findViewById(R.id.iv_event_poster);

        textName.setText(eventName);
        textDescription.setText(description);

        // Load poster
        if (posterUrl != null && !posterUrl.isEmpty()) {
            if (posterUrl.startsWith("data:image")) {
                try {
                    String base64 = posterUrl.substring(posterUrl.indexOf(",") + 1);
                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    ivPoster.setImageBitmap(bitmap);
                    ivPoster.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    ivPoster.setVisibility(View.GONE);
                }
            } else {
                Glide.with(this).load(posterUrl).into(ivPoster);
                ivPoster.setVisibility(View.VISIBLE);
            }
        }

        refreshWaitCount(textWaitCount);
        configureParticipationButton(buttonJoin, textWaitCount);
        setupComments(view);

        return view;
    }

    private void configureParticipationButton(Button buttonJoin, TextView textWaitCount) {
        if (organizerDeviceId != null && organizerDeviceId.equals(deviceId)) {
            buttonJoin.setEnabled(false);
            buttonJoin.setText("You cannot join your own event");
            return;
        }

        requireCompletedProfile(isCompleted -> {
            if (!isCompleted) {
                buttonJoin.setEnabled(false);
                buttonJoin.setText("Complete Profile to Join");
                return;
            }

            waitingListController.checkIfJoined(eventId, deviceId,
                    new WaitListRepository.SingleRecordCallback() {
                        @Override
                        public void onSuccess(WaitListRecord record) {
                            if (record != null && record.getStatus() == WaitStatus.WAITING) {
                                setLeaveMode(buttonJoin, textWaitCount);
                            } else if (record == null) {
                                setJoinMode(buttonJoin, textWaitCount);
                            } else {
                                buttonJoin.setEnabled(false);
                                buttonJoin.setText(record.getStatus().name());
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            setJoinMode(buttonJoin, textWaitCount);
                        }
                    });
        });
    }

    private void setJoinMode(Button button, TextView waitCount) {
        button.setText("Join Waiting List");
        button.setEnabled(true);
        button.setOnClickListener(v -> {
            button.setEnabled(false);
            waitingListController.joinWaitingList(eventId, deviceId,
                    new WaitListRepository.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(),
                                    "You've joined the waiting list!", Toast.LENGTH_SHORT).show();
                            setLeaveMode(button, waitCount);
                            refreshWaitCount(waitCount);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(),
                                    "Failed to join. Try again.", Toast.LENGTH_SHORT).show();
                            button.setEnabled(true);
                        }
                    });
        });
    }

    private void setLeaveMode(Button button, TextView waitCount) {
        button.setText("Leave Waiting List");
        button.setEnabled(true);
        button.setOnClickListener(v -> {
            button.setEnabled(false);
            waitingListController.leaveWaitingList(eventId, deviceId,
                    new WaitListRepository.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(),
                                    "You've left the waiting list.", Toast.LENGTH_SHORT).show();
                            setJoinMode(button, waitCount);
                            refreshWaitCount(waitCount);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(),
                                    "Failed to leave. Try again.", Toast.LENGTH_SHORT).show();
                            button.setEnabled(true);
                        }
                    });
        });
    }

    private void refreshWaitCount(TextView textWaitCount) {
        waitingListController.getWaitingCount(eventId, new WaitingListController.CountCallback() {
            @Override
            public void onCount(int count) {
                textWaitCount.setText(count + " entrants on waiting list");
            }
            @Override
            public void onFailure(Exception e) {
                textWaitCount.setText("Waitlist count unavailable");
            }
        });
    }

    private void setupComments(View view) {
        RecyclerView recyclerComments = view.findViewById(R.id.recycler_comments);
        EditText editComment = view.findViewById(R.id.edit_comment);
        Button buttonPost = view.findViewById(R.id.button_post_comment);

        List<EventComment> commentList = new ArrayList<>();
        EventCommentAdapter adapter = new EventCommentAdapter(commentList);
        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerComments.setAdapter(adapter);

        CommentRepository commentRepo = new FirestoreCommentRepository();
        commentRepo.getCommentsByEvent(eventId, new CommentRepository.CommentsCallback() {
            @Override
            public void onSuccess(List<EventComment> comments) {
                commentList.clear();
                commentList.addAll(comments);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Exception e) {}
        });

        buttonPost.setOnClickListener(v -> {
            String text = editComment.getText().toString().trim();
            if (text.isEmpty()) return;

            new FirestoreProfileRepository().getProfile(deviceId,
                    new ProfileRepository.ProfileCallback() {
                        @Override
                        public void onSuccess(UserProfile profile) {
                            String name = (profile != null && profile.getName() != null)
                                    ? profile.getName() : "Anonymous";
                            EventComment comment = new EventComment(eventId, deviceId, name, text);
                            commentRepo.addComment(comment,
                                    new CommentRepository.OperationCallback() {
                                        @Override
                                        public void onSuccess() {
                                            editComment.setText("");
                                            InputMethodManager imm = (InputMethodManager)
                                                    requireContext().getSystemService(
                                                            android.content.Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(
                                                    editComment.getWindowToken(), 0);
                                            commentList.add(comment);
                                            adapter.notifyItemInserted(commentList.size() - 1);
                                            recyclerComments.scrollToPosition(commentList.size() - 1);
                                        }
                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(getContext(),
                                                    "Failed to post comment.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(),
                                    "Could not load profile.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void requireCompletedProfile(ProfileCheckCallback callback) {
        new FirestoreProfileRepository().getProfile(deviceId,
                new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(UserProfile profile) {
                        callback.onResult(profile != null && profile.isProfileCompleted());
                    }
                    @Override
                    public void onFailure(Exception e) {
                        callback.onResult(false);
                    }
                });
    }

    private interface ProfileCheckCallback {
        void onResult(boolean isCompleted);
    }
}