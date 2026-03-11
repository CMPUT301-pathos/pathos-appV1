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
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

/**
 * Fragment displaying the details of a specific event for entrants.
 *
 * Responsibilities:
 * - Display event name and description
 * - Show the current number of entrants on the waiting list
 * - Allow entrant to join the waiting list
 *
 * User stories supported:
 * - US 01.05.04: Know how many total entrants are on the waiting list for an event
 * - US 01.06.02: Sign up for an event from the event details
 *
 * @author Edwin David
 * @version 1.0
 */
public class EventDetailFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";
    private static final String ARG_DESCRIPTION = "description";

    private String eventId;
    private String eventName;
    private String description;

    private WaitingListController waitingListController;
    private String deviceId;

    public static EventDetailFragment newInstance(String eventId, String eventName, String description) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putString(ARG_DESCRIPTION, description);
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

        // Check current status and set button accordingly
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

        return view;
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
                            Toast.makeText(getContext(), "You've joined the waiting list!", Toast.LENGTH_SHORT).show();
                            setLeaveMode(button, waitCount);
                            refreshWaitCount(waitCount);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to join. Try again.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), "You've left the waiting list.", Toast.LENGTH_SHORT).show();
                            setJoinMode(button, waitCount);
                            refreshWaitCount(waitCount);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to leave. Try again.", Toast.LENGTH_SHORT).show();
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
}
