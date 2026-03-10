package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.eventlottery.controller.WaitingListController;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;

/**
 * Fragment displaying the notifications/invitations screen for entrants.
 *
 * Responsibilities:
 * - Display win/lose lottery notifications as cards
 * - Allow entrant to accept or decline an invitation
 * - Clear lose notifications
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 *
 * @author Fawaz Mansoor
 * @version 1.0
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

        LinearLayout notificationsContainer = view.findViewById(R.id.notifications_container);

        // Win notification card
        addWinNotification(inflater, notificationsContainer, eventName);

        // Lose notification card (for demo)
        addLoseNotification(inflater, notificationsContainer, "Some Other Event");

        return view;
    }

    private void addWinNotification(LayoutInflater inflater, LinearLayout container, String name) {
        View card = inflater.inflate(R.layout.item_notification, container, false);

        TextView tvEventName = card.findViewById(R.id.tvEventName);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        Button btnAccept = card.findViewById(R.id.btnAccept);
        Button btnDecline = card.findViewById(R.id.btnDecline);
        LinearLayout winButtonsRow = card.findViewById(R.id.winButtonsRow);
        Button btnClear = card.findViewById(R.id.btnClear);

        tvEventName.setText(name != null ? name : "Event");
        tvMessage.setText("You have won the lottery for this event!");
        tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        winButtonsRow.setVisibility(View.VISIBLE);
        btnClear.setVisibility(View.GONE);

        btnAccept.setOnClickListener(v -> {
            waitingListController.acceptInvitation(eventId, deviceId);
            Toast.makeText(getContext(), "You have accepted the invitation!", Toast.LENGTH_SHORT).show();
            container.removeView(card);
        });

        btnDecline.setOnClickListener(v -> {
            waitingListController.declineInvitation(eventId, deviceId);
            Toast.makeText(getContext(), "You have declined the invitation.", Toast.LENGTH_SHORT).show();
            container.removeView(card);
        });

        container.addView(card);
    }

    private void addLoseNotification(LayoutInflater inflater, LinearLayout container, String name) {
        View card = inflater.inflate(R.layout.item_notification, container, false);

        TextView tvEventName = card.findViewById(R.id.tvEventName);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        LinearLayout winButtonsRow = card.findViewById(R.id.winButtonsRow);
        Button btnClear = card.findViewById(R.id.btnClear);

        tvEventName.setText(name != null ? name : "Event");
        tvMessage.setText("You have lost the lottery for this event :(");
        tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        winButtonsRow.setVisibility(View.GONE);
        btnClear.setVisibility(View.VISIBLE);

        btnClear.setOnClickListener(v -> container.removeView(card));

        container.addView(card);
    }
}
