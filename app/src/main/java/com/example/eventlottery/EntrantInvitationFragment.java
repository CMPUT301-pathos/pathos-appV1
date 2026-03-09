package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.eventlottery.controller.WaitingListController;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;

/**
 * EntrantInvitationFragment
 *
 * Role: Notifications / invitations screen for entrants.
 * - Displays notification-like content (e.g., invitations, win/lose messaging).
 * - Provides UI hooks for accept/decline flows (depending on current wiring).
 *
 * User stories supported (UI layer):
 * - US 01.04.01: Receive notification when chosen ("win") [UI entry point]
 * - US 01.04.02: Receive notification when not chosen ("lose") [UI entry point]
 * - US 01.05.02/01.05.03: Accept/Decline invitation (if wired)
 *
 * Notes:
 * - Lottery trigger + true push/in-app notification delivery may be implemented later
 *   in PathosNotifyService/raffle integration. Document current behavior honestly.
 */


public class EntrantInvitationFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";
    private static final String ARG_DEVICE_ID = "deviceId";

    private String eventId;
    private String eventName;
    private String deviceId;

    private WaitingListController waitingListController;

    public static EntrantInvitationFragment newInstance(String eventId, String eventName, String deviceId) {
        EntrantInvitationFragment fragment = new EntrantInvitationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putString(ARG_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
            deviceId = getArguments().getString(ARG_DEVICE_ID);
        }
        waitingListController = new WaitingListController(new FirestoreWaitListRepository());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrant_invitation, container, false);

        TextView textEventName = view.findViewById(R.id.text_event_name);
        TextView textEventDetails = view.findViewById(R.id.text_event_details);
        Button buttonAccept = view.findViewById(R.id.button_accept);
        Button buttonDecline = view.findViewById(R.id.button_decline);

        textEventName.setText("You've been selected!");
        textEventDetails.setText("Event: " + eventName);

        buttonAccept.setOnClickListener(v -> {
            waitingListController.acceptInvitation(eventId, deviceId);
            Toast.makeText(getContext(), "You have accepted the invitation!", Toast.LENGTH_SHORT).show();
        });

        buttonDecline.setOnClickListener(v -> {
            waitingListController.declineInvitation(eventId, deviceId);
            Toast.makeText(getContext(), "You have declined the invitation.", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
