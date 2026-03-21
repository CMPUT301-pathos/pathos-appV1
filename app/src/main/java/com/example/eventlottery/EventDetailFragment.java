package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.controller.WaitingListController;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.service.DeviceIdentityService;

/**
 * EventDetailFragment
 *
 * Displays detailed information for a selected event.
 *
 * Responsibilities:
 * - display event name and description
 * - show the current number of entrants on the waiting list
 * - allow browsing of event details regardless of profile completion
 * - block waiting-list participation until the user's required profile
 *   information has been completed
 * - prevent an organizer from joining the waiting list of an event
 *   they created
 *
 * Profile-completion protection:
 * - users with incomplete profiles may still browse event details
 * - users with incomplete profiles may not join or leave the waiting list
 *
 * Organizer participation protection:
 * - users are identified by device ID
 * - if the current user's device ID matches the creator device ID
 *   of the event, the join button is disabled
 *
 * User stories supported:
 * - US 01.05.04: Know how many total entrants are on the waiting list for an event
 * - US 01.06.02: Sign up for an event from the event details
 * - US 01.02.01: Entrant provides personal information
 * - US 01.02.02: Entrant updates profile information
 * - US 01.07.01: User is identified by device
 *
 * Revision note:
 * - Original event detail and waitlist functionality by Edwin David and Kenneth Joseph
 * - Organizer self-signup restriction using deviceId-based ownership added by Kenneth Joseph
 *
 * @author Edwin David, Kenneth Joseph
 * @version 1.2
 */
public class EventDetailFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_ORGANIZER_DEVICE_ID = "organizerDeviceId";

    private String eventId;
    private String eventName;
    private String description;
    private String organizerDeviceId;

    private WaitingListController waitingListController;
    private String deviceId;

    public EventDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of EventDetailFragment for a specific event.
     *
     * @param eventId Firestore ID of the event
     * @param eventName display name of the event
     * @param description event description
     * @param organizerDeviceId device ID of the organizer who created the event
     * @return configured EventDetailFragment instance
     */
    public static EventDetailFragment newInstance(String eventId,
                                                  String eventName,
                                                  String description,
                                                  String organizerDeviceId) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_ORGANIZER_DEVICE_ID, organizerDeviceId);
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

        textName.setText(eventName);
        textDescription.setText(description);

        refreshWaitCount(textWaitCount);
        configureParticipationButton(buttonJoin, textWaitCount);

        return view;
    }

    /**
     * Configures the waiting-list button based on organizer ownership,
     * profile completion, and current waiting-list membership.
     *
     * @param buttonJoin the join/leave waiting list button
     * @param textWaitCount the waiting list count label
     */
    private void configureParticipationButton(Button buttonJoin, TextView textWaitCount) {
        if (organizerDeviceId != null && organizerDeviceId.equals(deviceId)) {
            buttonJoin.setEnabled(false);
            buttonJoin.setText("You cannot join your own event");
            return;
        }

        requireCompletedProfile(new ProfileCheckCallback() {
            @Override
            public void onResult(boolean isCompleted) {
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
            }
        });
    }

    /**
     * Puts the action button into "join waiting list" mode.
     *
     * @param button the action button
     * @param waitCount the waiting list count label
     */
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
                                    "You've joined the waiting list!",
                                    Toast.LENGTH_SHORT).show();
                            setLeaveMode(button, waitCount);
                            refreshWaitCount(waitCount);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(),
                                    "Failed to join. Try again.",
                                    Toast.LENGTH_SHORT).show();
                            button.setEnabled(true);
                        }
                    });
        });
    }

    /**
     * Puts the action button into "leave waiting list" mode.
     *
     * @param button the action button
     * @param waitCount the waiting list count label
     */
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
                                    "You've left the waiting list.",
                                    Toast.LENGTH_SHORT).show();
                            setJoinMode(button, waitCount);
                            refreshWaitCount(waitCount);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(),
                                    "Failed to leave. Try again.",
                                    Toast.LENGTH_SHORT).show();
                            button.setEnabled(true);
                        }
                    });
        });
    }

    /**
     * Refreshes the visible waiting-list count for this event.
     *
     * @param textWaitCount the waiting list count label
     */
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

    /**
     * Checks whether the current user's profile is completed before allowing
     * participation-related actions.
     *
     * @param callback callback receiving true if the profile is completed,
     *                 otherwise false
     */
    private void requireCompletedProfile(ProfileCheckCallback callback) {
        new FirestoreProfileRepository().getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                boolean isCompleted = profile != null && profile.isProfileCompleted();
                callback.onResult(isCompleted);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Simple callback for profile-completion checks.
     */
    private interface ProfileCheckCallback {
        void onResult(boolean isCompleted);
    }
}