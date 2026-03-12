package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.service.PathosRaffleService;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * OrganizerEventDetailFragment
 *
 * Shows the organizer a list of entrants on the waitlist and accepted entrants for their event.
 *
 * User stories supported:
 * - US 02.06.01: View a list of all entrants on the waiting list
 * - US 02.06.02: View a list of all entrants who enrolled for the event
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class OrganizerEventDetailFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";

    private String eventId;
    private String eventName;

    private LinearLayout waitlistContainer;
    private LinearLayout enrolledContainer;
    private TextView tvWaitlistEmpty;
    private TextView tvEnrolledEmpty;

    private FirestoreWaitListRepository waitListRepository;
    private FirestoreProfileRepository profileRepository;
    private PathosRaffleService raffleService;

    public static OrganizerEventDetailFragment newInstance(EventSummary event) {
        OrganizerEventDetailFragment fragment = new OrganizerEventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, event.getId());
        args.putString(ARG_EVENT_NAME, event.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
        }
        waitListRepository = new FirestoreWaitListRepository();
        profileRepository = new FirestoreProfileRepository();
        raffleService = new PathosRaffleService(waitListRepository);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_event_detail, container, false);

        TextView tvTitle = view.findViewById(R.id.tv_organizer_event_title);
        MaterialButton btnRunLottery = view.findViewById(R.id.btn_run_lottery);
        btnRunLottery.setOnClickListener(v -> showLotteryDrawDialog());
        waitlistContainer = view.findViewById(R.id.container_waitlist);
        enrolledContainer = view.findViewById(R.id.container_enrolled);
        tvWaitlistEmpty = view.findViewById(R.id.tv_waitlist_empty);
        tvEnrolledEmpty = view.findViewById(R.id.tv_enrolled_empty);

        tvTitle.setText(eventName);

        loadEntrants();

        return view;
    }

    private void loadEntrants() {
        waitListRepository.getRecordsByEventAsync(eventId, new WaitListRepository.WaitListCallBack() {
            @Override
            public void onSuccess(List<WaitListRecord> records) {
                if (getActivity() == null) return;

                waitlistContainer.removeAllViews();
                enrolledContainer.removeAllViews();

                int waitingCount = 0;
                int enrolledCount = 0;

                for (WaitListRecord record : records) {
                    if (record.getStatus() == WaitStatus.WAITING ||
                            record.getStatus() == WaitStatus.INVITED) {
                        waitingCount++;
                        addEntrantRow(waitlistContainer, record.getDeviceId(), record.getStatus());
                    } else if (record.getStatus() == WaitStatus.ACCEPTED) {
                        enrolledCount++;
                        addEntrantRow(enrolledContainer, record.getDeviceId(), record.getStatus());
                    }
                }

                tvWaitlistEmpty.setVisibility(waitingCount == 0 ? View.VISIBLE : View.GONE);
                tvEnrolledEmpty.setVisibility(enrolledCount == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                Toast.makeText(getContext(), "Failed to load entrants", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addEntrantRow(LinearLayout container, String deviceId, WaitStatus status) {
        // Inflate row and set deviceId as placeholder, then load name async
        View row = LayoutInflater.from(getContext())
                .inflate(R.layout.item_entrant_row, container, false);

        TextView tvName = row.findViewById(R.id.tv_entrant_name);
        TextView tvStatus = row.findViewById(R.id.tv_entrant_status);

        tvName.setText(deviceId); // placeholder until profile loads
        tvStatus.setText(status.name());

        container.addView(row);

        // Load actual name from profile
        profileRepository.getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (getActivity() == null) return;
                if (profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
                    tvName.setText(profile.getName());
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Keep deviceId as fallback
            }
        });
    }

    private void showLotteryDrawDialog() {
        android.widget.EditText etCount = new android.widget.EditText(requireContext());
        etCount.setHint("Number of entrants to draw");
        etCount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCount.setPadding(48, 32, 48, 16);

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Run Lottery Draw")
                .setMessage("How many entrants do you want to invite?")
                .setView(etCount)
                .setPositiveButton("Draw", (dialog, which) -> {
                    String input = etCount.getText().toString().trim();
                    if (input.isEmpty()) {
                        Toast.makeText(getContext(), "Enter a number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int count = Integer.parseInt(input);
                    runLotteryDraw(count);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void runLotteryDraw(int count) {
        raffleService.drawInitial(eventId, count, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                if (getActivity() == null) return;
                if (selected.isEmpty()) {
                    Toast.makeText(getContext(),
                            "No waiting entrants to draw from.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(),
                            selected.size() + " entrant(s) invited!",
                            Toast.LENGTH_SHORT).show();
                }
                // Refresh the list
                loadEntrants();
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                Toast.makeText(getContext(),
                        "Draw failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}