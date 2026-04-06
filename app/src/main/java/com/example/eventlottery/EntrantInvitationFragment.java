package com.example.eventlottery;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.eventlottery.controller.WaitingListController;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying entrant-facing invitations and waitlist notifications.
 *
 * Responsibilities:
 * - Load waitlist records for the current device
 * - Display invitation cards for INVITED entries
 * - Display not-selected cards for NOT_SELECTED entries
 * - Display cancellation cards for CANCELLED entries
 * - Allow entrants to accept or decline an invitation
 * - Allow generic notification cards to be cleared
 *
 * Waitlist display semantics:
 * - INVITED: entrant was selected and can accept or decline
 * - NOT_SELECTED: entrant participated in a raffle draw but was not chosen
 * - DECLINED: entrant declined an invitation and should not be shown as
 *   "not selected"
 * - CANCELLED: entrant's invitation/participation was cancelled
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 *
 * Revision note:
 * - Updated to use NOT_SELECTED instead of DECLINED for the
 *   "You were not selected" entrant notification state.
 *
 * @author Fawaz Mansoor, Kenneth Joseph
 * @version 1.4
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
        de.hdodenhof.circleimageview.CircleImageView ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        String deviceId = DeviceIdentityService.getDeviceId(requireContext());
        new FirestoreProfileRepository().getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (getActivity() == null) return;
                if (profile != null && profile.getProfilePhotoUri() != null && !profile.getProfilePhotoUri().isEmpty()) {
                    com.bumptech.glide.Glide.with(requireContext())
                            .load(profile.getProfilePhotoUri())
                            .placeholder(R.drawable.ic_profile_placeholder_forstyledlayout)
                            .into(ivProfilePhoto);
                }
            }

            @Override
            public void onFailure(Exception e) { }
        });

        tvEmpty = new TextView(requireContext());
        tvEmpty.setText("No notifications yet.");
        tvEmpty.setTextColor(0xCCFFFFFF);
        tvEmpty.setGravity(Gravity.CENTER);
        tvEmpty.setPadding(0, 48, 0, 0);
        tvEmpty.setVisibility(View.GONE);
        notificationsContainer.addView(tvEmpty);

        loadNotifications(inflater);


        return view;
    }

    /**
     * Helper function
     */

    private void loadEventNameAndAddGenericCard(LayoutInflater inflater,
                                                String eventId,
                                                String type,
                                                String message,
                                                String docId) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (getActivity() == null) return;

                    String eventName = doc.exists() && doc.getString("name") != null
                            ? doc.getString("name")
                            : "Event Update";

                    addTypedGenericNotification(inflater, type, eventName, message, docId);
                    checkEmpty();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) return;
                    addTypedGenericNotification(inflater, type, "Event Update", message, docId);
                    checkEmpty();
                });
    }


    /**
     * Loads waitlist-driven entrant notifications and unread generic
     * notifications for the current device.
     *
     * @param inflater inflater used to create notification cards
     */
    private void loadNotifications(LayoutInflater inflater) {
        notificationsContainer.removeAllViews();
        notificationsContainer.addView(tvEmpty);
        tvEmpty.setVisibility(View.VISIBLE);

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
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                        records.add(record);
                    }

                    for (WaitListRecord record : records) {
                        if (record.getStatus() == WaitStatus.INVITED) {
                            loadEventNameAndAddCard(inflater, record, WaitStatus.INVITED);
                        } else if (record.getStatus() == WaitStatus.NOT_SELECTED) {
                            loadEventNameAndAddCard(inflater, record, WaitStatus.NOT_SELECTED);
                        } else if (record.getStatus() == WaitStatus.CANCELLED) {
                            loadEventNameAndAddCard(inflater, record, WaitStatus.CANCELLED);
                        }
                    }

                    checkEmpty();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) return;
                    Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                });

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snap -> {
                    if (getActivity() == null) return;

                    for (QueryDocumentSnapshot doc : snap) {
                        String message = doc.getString("message");
                        String docId = doc.getId();
                        String type = doc.getString("type");
                        String eventId = doc.getString("eventId");

                        if (message != null) {
                            if ("CO_ORGANIZER_ADDED".equals(type) && eventId != null) {
                                loadEventNameAndAddGenericCard(inflater, eventId, type, message, docId);
                            } else {
                                addGenericNotification(inflater, "Waitlist Update", message, docId);
                            }
                        }
                    }
                    checkEmpty();
                });
    }

    /**
     * Loads the event name and adds the appropriate card for the supplied status.
     *
     * @param inflater inflater used to create notification cards
     * @param record waitlist record for the current entrant
     * @param status status to render
     */
    private void loadEventNameAndAddCard(LayoutInflater inflater,
                                         WaitListRecord record,
                                         WaitStatus status) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(record.getEventId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (getActivity() == null) return;

                    String eventName = doc.exists() && doc.getString("name") != null
                            ? doc.getString("name")
                            : record.getEventId();

                    addCardForStatus(inflater, eventName, record, status);
                    checkEmpty();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) return;
                    addCardForStatus(inflater, record.getEventId(), record, status);
                    checkEmpty();
                });
    }

    /**
     * Routes status values to the correct notification card UI.
     *
     * @param inflater inflater used to create notification cards
     * @param eventName display name of the event
     * @param record waitlist record backing this notification
     * @param status waitlist status to render
     */
    private void addCardForStatus(LayoutInflater inflater,
                                  String eventName,
                                  WaitListRecord record,
                                  WaitStatus status) {
        if (status == WaitStatus.INVITED) {
            addWinNotification(inflater, eventName, record);
        } else if (status == WaitStatus.NOT_SELECTED) {
            addNotSelectedNotification(inflater, eventName);
        } else if (status == WaitStatus.CANCELLED) {
            addCancelledNotification(inflater, eventName);
        }
    }

    /**
     * Adds an invitation card for an entrant who was selected.
     *
     * @param inflater inflater used to create notification cards
     * @param eventName display name of the event
     * @param record waitlist record backing this invitation
     */
    private void addWinNotification(LayoutInflater inflater,
                                    String eventName,
                                    WaitListRecord record) {
        // Check if this is a private event invite
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(record.getEventId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (getActivity() == null) return;
                    Boolean isPrivate = doc.getBoolean("isPrivate");
                    boolean privateEvent = isPrivate != null && isPrivate;
                    buildInviteCard(inflater, eventName, record, privateEvent);
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) return;
                    buildInviteCard(inflater, eventName, record, false);
                });
    }

    private void buildInviteCard(LayoutInflater inflater,
                                 String eventName,
                                 WaitListRecord record,
                                 boolean isPrivate) {
        View card = inflater.inflate(R.layout.item_notification, notificationsContainer, false);

        TextView tvEventName = card.findViewById(R.id.tvEventName);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        MaterialButton btnAccept = card.findViewById(R.id.btnAccept);
        MaterialButton btnDecline = card.findViewById(R.id.btnDecline);
        LinearLayout winButtonsRow = card.findViewById(R.id.winButtonsRow);
        MaterialButton btnClear = card.findViewById(R.id.btnClear);

        tvEventName.setText(eventName);
        tvMessage.setText(isPrivate
                ? "🔒 You've been invited to this private event!"
                : "🎉 You have won the lottery for this event!");
        winButtonsRow.setVisibility(View.VISIBLE);
        btnClear.setVisibility(View.GONE);

        btnAccept.setOnClickListener(v -> {
            waitingListController.acceptInvitation(record.getEventId(), deviceId);
            Toast.makeText(getContext(),
                    isPrivate ? "You have accepted the private event invitation!"
                            : "You have accepted the invitation!",
                    Toast.LENGTH_SHORT).show();
            notificationsContainer.removeView(card);
            checkEmpty();
        });

        btnDecline.setOnClickListener(v -> {
            waitingListController.declineInvitation(record.getEventId(), deviceId);
            Toast.makeText(getContext(),
                    isPrivate ? "You have declined the private event invitation."
                            : "You have declined the invitation.",
                    Toast.LENGTH_SHORT).show();
            notificationsContainer.removeView(card);
            checkEmpty();
        });

        notificationsContainer.addView(card);
        tvEmpty.setVisibility(View.GONE);
        checkEmpty();
    }

    /**
     * Adds a notification card for an entrant who was not selected in the raffle.
     *
     * @param inflater inflater used to create notification cards
     * @param eventName display name of the event
     */
    private void addNotSelectedNotification(LayoutInflater inflater, String eventName) {
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

    /**
     * Adds a cancellation notification card.
     *
     * @param inflater inflater used to create notification cards
     * @param eventName display name of the event
     */
    private void addCancelledNotification(LayoutInflater inflater, String eventName) {
        View card = inflater.inflate(R.layout.item_notification, notificationsContainer, false);

        TextView tvEventName = card.findViewById(R.id.tvEventName);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        LinearLayout winButtonsRow = card.findViewById(R.id.winButtonsRow);
        MaterialButton btnClear = card.findViewById(R.id.btnClear);

        tvEventName.setText(eventName);
        tvMessage.setText("Your invitation or registration for this event was cancelled.");
        winButtonsRow.setVisibility(View.GONE);
        btnClear.setVisibility(View.VISIBLE);

        btnClear.setOnClickListener(v -> {
            notificationsContainer.removeView(card);
            checkEmpty();
        });

        notificationsContainer.addView(card);
        tvEmpty.setVisibility(View.GONE);
    }

    /**
     * Adds a generic unread notification from the notifications collection.
     *
     * @param inflater inflater used to create notification cards
     * @param message message text to display
     * @param docId Firestore document id for marking the notification as read
     */
    private void addGenericNotification(LayoutInflater inflater,
                                        String title,
                                        String message,
                                        String docId) {
        View card = inflater.inflate(R.layout.item_notification, notificationsContainer, false);

        TextView tvEventName = card.findViewById(R.id.tvEventName);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        LinearLayout winButtonsRow = card.findViewById(R.id.winButtonsRow);
        MaterialButton btnClear = card.findViewById(R.id.btnClear);

        tvEventName.setText(title);
        tvMessage.setText(message);
        winButtonsRow.setVisibility(View.GONE);
        btnClear.setVisibility(View.VISIBLE);

        btnClear.setOnClickListener(v -> {
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

    private void addTypedGenericNotification(LayoutInflater inflater,
                                             String type,
                                             String eventName,
                                             String message,
                                             String docId) {
        View card = inflater.inflate(R.layout.item_notification, notificationsContainer, false);

        TextView tvEventName = card.findViewById(R.id.tvEventName);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        LinearLayout winButtonsRow = card.findViewById(R.id.winButtonsRow);
        MaterialButton btnClear = card.findViewById(R.id.btnClear);

        if ("CO_ORGANIZER_ADDED".equals(type)) {
            tvEventName.setText("Co-organizer Invite");
            tvMessage.setText("You were added as a co-organizer for " + eventName + ".");
        } else {
            tvEventName.setText("Waitlist Update");
            tvMessage.setText(message);
        }

        winButtonsRow.setVisibility(View.GONE);
        btnClear.setVisibility(View.VISIBLE);

        btnClear.setOnClickListener(v -> {
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

    /**
     * Shows the empty state whenever there are no visible notification cards.
     */
    private void checkEmpty() {
        int visibleCards = notificationsContainer.getChildCount() - 1;
        tvEmpty.setVisibility(visibleCards == 0 ? View.VISIBLE : View.GONE);
    }
}