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
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
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
 * - Load cancellation notifications from the notifications collection
 * - Display win notifications (INVITED status) with Accept/Decline buttons
 * - Display lose/cancelled notifications with Clear button
 * - Allow entrant to accept or decline an invitation
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 *
 * @author Fawaz Mansoor
 * @version 1.2
 */
public class EntrantInvitationFragment extends Fragment {

    private LinearLayout notificationsContainer;
    private TextView tvEmpty;
    private WaitingListController waitingListController;
    private String deviceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        waitingListController = new WaitingListController(new FirestoreWaitListRepository());
        deviceId = DeviceIdentityService.getDeviceId(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrant_invitation, container, false);
        notificationsContainer = view.findViewById(R.id.notifications_container);

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

    private void loadNotifications(LayoutInflater inflater) {
        notificationsContainer.removeAllViews();
        notificationsContainer.addView(tvEmpty);
        tvEmpty.setVisibility(View.VISIBLE);

        // Query 1: waitlist records (INVITED, DECLINED)
        FirebaseFirestore.getInstance()
                .collection("waitlist")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (getActivity() == null) return;

                    List<WaitListRecord> records = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        WaitListRecord record = new WaitListRecord(
                                doc.getString("eventId"),
                                doc.getString("deviceId")
                        );
                        String statusStr = doc.getString("status");
                        if (statusStr != null) {
                            try {
                                record.setStatus(WaitStatus.valueOf(statusStr));
                            } catch (IllegalArgumentException ignored) {}
                        }
                        records.add(record);
                    }

                    for (WaitListRecord record : records) {
                        if (record.getStatus() == WaitStatus.INVITED) {
                            loadEventNameAndAddCard(inflater, record, true);
                        } else if (record.getStatus() == WaitStatus.DECLINED) {
                            loadEventNameAndAddCard(inflater, record, false);
                        }
                    }

                    checkEmpty();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) return;
                    Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                });

        // Query 2: cancellation notifications from organizer
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snap -> {
                    if (getActivity() == null) return;
                    for (QueryDocumentSnapshot doc : snap) {
                        String message = doc.getString("message");
                        String docId = doc.getId();
                        if (message != null) {
                            addCancelNotification(inflater, message, docId);
                        }
                    }
                    checkEmpty();
                });
    }

    private void loadEventNameAndAddCard(LayoutInflater inflater,
                                         WaitListRecord record, boolean isWin) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(record.getEventId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (getActivity() == null) return;
                    String eventName = doc.exists() && doc.getString("name") != null
                            ? doc.getString("name")
                            : record.getEventId();
                    if (isWin) {
                        addWinNotification(inflater, eventName, record);
                    } else {
                        addLoseNotification(inflater, eventName, record);
                    }
                    checkEmpty();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) return;
                    if (isWin) {
                        addWinNotification(inflater, record.getEventId(), record);
                    } else {
                        addLoseNotification(inflater, record.getEventId(), record);
                    }
                    checkEmpty();
                });
    }

    private void addWinNotification(LayoutInflater inflater, String eventName,
                                    WaitListRecord record) {
        View card = inflater.inflate(R.layout.item_notification, notificationsContainer, false);

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

    private void addLoseNotification(LayoutInflater inflater, String eventName,
                                     WaitListRecord record) {
        View card = inflater.inflate(R.layout.item_notification, notificationsContainer, false);

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

    private void addCancelNotification(LayoutInflater inflater, String message, String docId) {
        View card = inflater.inflate(R.layout.item_notification, notificationsContainer, false);

        TextView tvEventName = card.findViewById(R.id.tvEventName);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        LinearLayout winButtonsRow = card.findViewById(R.id.winButtonsRow);
        MaterialButton btnClear = card.findViewById(R.id.btnClear);

        tvEventName.setText("Waitlist Update");
        tvMessage.setText(message);
        winButtonsRow.setVisibility(View.GONE);
        btnClear.setVisibility(View.VISIBLE);

        btnClear.setOnClickListener(v -> {
            // Mark as read in Firestore so it doesn't reappear
            FirebaseFirestore.getInstance()
                    .collection("notifications")
                    .document(docId)
                    .update("read", true);
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
