package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.eventlottery.controller.WaitingListController;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.firebase.FirestoreEventRepository;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying the notifications/invitations screen for entrants.
 *
 * Responsibilities:
 * - Load all waitlist records for the current device from Firestore
 * - Display win notifications (INVITED status) with Accept/Decline buttons
 * - Display lose notifications (DECLINED/CANCELLED) with Clear button
 * - Allow entrant to accept or decline an invitation
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 *
 * @author Fawaz Mansoor
 * @version 1.1
 */
public class EntrantInvitationFragment extends Fragment {

    private LinearLayout notificationsContainer;
    private TextView tvEmpty;
    private WaitingListController waitingListController;

    /**
     * Creates a new instance of EntrantInvitationFragment with the specified event details.
     *
     * @param eventId   the ID of the event
     * @param eventName the name of the event
     * @param deviceId  the device ID of the current user
     * @return a new instance of EntrantInvitationFragment
     */
    public static EntrantInvitationFragment newInstance(String eventId, String eventName, String deviceId) {
        EntrantInvitationFragment fragment = new EntrantInvitationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putString(ARG_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the fragment. Retrieves arguments for event and device IDs,
     * and initializes the WaitingListController.
     *
     * @param savedInstanceState saved state bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        waitingListController = new WaitingListController(new FirestoreWaitListRepository());
        deviceId = DeviceIdentityService.getDeviceId(requireContext());
    }

    /**
     * Inflates the fragment layout and populates the notifications container
     * with example win and lose lottery cards.
     *
     * @param inflater           LayoutInflater to inflate views
     * @param container          parent container
     * @param savedInstanceState saved state bundle
     * @return the root view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrant_invitation, container, false);
        notificationsContainer = view.findViewById(R.id.notifications_container);

        // Add empty state TextView dynamically
        tvEmpty = new TextView(requireContext());
        tvEmpty.setText("No notifications yet.");
        tvEmpty.setTextColor(0xCCFFFFFF);
        tvEmpty.setGravity(android.view.Gravity.CENTER);
        tvEmpty.setPadding(0, 48, 0, 0);
        tvEmpty.setVisibility(View.GONE);
        notificationsContainer.addView(tvEmpty);

        loadNotifications(inflater);

        return view;
    }

    /**
     * Adds a "win" notification card to the notifications container.
     * Provides Accept and Decline buttons for the user to respond.
     *
     * @param inflater LayoutInflater to create the card view
     * @param container the LinearLayout container to add the card to
     * @param name the name of the event
     */
    private void addWinNotification(LayoutInflater inflater, LinearLayout container, String name) {
        View card = inflater.inflate(R.layout.item_notification, container, false);

        TextView tvEventName = card.findViewById(R.id.tvEventName);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        MaterialButton btnAccept = card.findViewById(R.id.btnAccept);
        MaterialButton btnDecline = card.findViewById(R.id.btnDecline);
        LinearLayout winButtonsRow = card.findViewById(R.id.winButtonsRow);
        MaterialButton btnClear = card.findViewById(R.id.btnClear);

        tvEventName.setText(eventName);
        tvMessage.setText("You have won the lottery for this event!");
        winButtonsRow.setVisibility(View.VISIBLE);
        btnClear.setVisibility(View.GONE);

        btnAccept.setOnClickListener(v -> {
            waitingListController.acceptInvitation(record.getEventId(), deviceId);
            Toast.makeText(getContext(), "You have accepted the invitation!", Toast.LENGTH_SHORT).show();
            notificationsContainer.removeView(card);
            checkEmpty();
        });

        btnDecline.setOnClickListener(v -> {
            waitingListController.declineInvitation(record.getEventId(), deviceId);
            Toast.makeText(getContext(), "You have declined the invitation.", Toast.LENGTH_SHORT).show();
            notificationsContainer.removeView(card);
            checkEmpty();
        });

        notificationsContainer.addView(card);
        tvEmpty.setVisibility(View.GONE);
    }

    /**
     * Adds a "lose" notification card to the notifications container.
     * Provides a Clear button for the user to remove the card.
     *
     * @param inflater LayoutInflater to create the card view
     * @param container the LinearLayout container to add the card to
     * @param name the name of the event
     */
    private void addLoseNotification(LayoutInflater inflater, LinearLayout container, String name) {
        View card = inflater.inflate(R.layout.item_notification, container, false);

        TextView tvEventName = card.findViewById(R.id.tvEventName);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        LinearLayout winButtonsRow = card.findViewById(R.id.winButtonsRow);
        MaterialButton btnClear = card.findViewById(R.id.btnClear);

        tvEventName.setText(eventName);
        tvMessage.setText("You were not selected for this event.");
        winButtonsRow.setVisibility(View.GONE);
        btnClear.setVisibility(View.VISIBLE);

        btnClear.setOnClickListener(v -> {
            notificationsContainer.removeView(card);
            checkEmpty();
        });

        notificationsContainer.addView(card);
        tvEmpty.setVisibility(View.GONE);
    }

    private void checkEmpty() {
        // -1 because tvEmpty is always in the container
        int visibleCards = notificationsContainer.getChildCount() - 1;
        tvEmpty.setVisibility(visibleCards == 0 ? View.VISIBLE : View.GONE);
    }
}
