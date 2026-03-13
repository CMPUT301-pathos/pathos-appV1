package com.example.eventlottery.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.eventlottery.MainActivity;
import com.example.eventlottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Service for listening to real-time Firestore changes and triggering
 * local Android notifications when an entrant is invited or removed.
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen from waiting list
 * - US 01.04.03: Respect notification opt-out preference
 *
 * @author Fawaz Mansoor
 * @version 1.1
 */
public class NotificationListenerService {

    private static final String CHANNEL_ID = "pathos_notifications";
    private static final String CHANNEL_NAME = "Event Notifications";
    private static final int NOTIFICATION_ID_BASE = 1000;

    private final Context context;
    private final String deviceId;
    private ListenerRegistration listenerRegistration;
    private ListenerRegistration cancellationListenerRegistration;

    public interface OnInviteReceivedListener {
        void onInviteReceived(String eventName);
    }

    private OnInviteReceivedListener inviteListener;

    public void setOnInviteReceivedListener(OnInviteReceivedListener listener) {
        this.inviteListener = listener;
    }

    public NotificationListenerService(Context context, String deviceId) {
        this.context = context;
        this.deviceId = deviceId;
        createNotificationChannel();
    }

    /**
     * Start listening for status changes on this device's waitlist records,
     * and for cancellation notifications from the organizer.
     * Call this when the app starts (e.g. in MainActivity).
     */
    public void startListening() {
        // Listener 1: waitlist INVITED changes
        listenerRegistration = FirebaseFirestore.getInstance()
                .collection("waitlist")
                .whereEqualTo("deviceId", deviceId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    if (snapshots.getMetadata().isFromCache()) return;

                    for (com.google.firebase.firestore.DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() == com.google.firebase.firestore.DocumentChange.Type.MODIFIED
                                || change.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {

                            QueryDocumentSnapshot doc = change.getDocument();
                            String status = doc.getString("status");
                            String eventId = doc.getString("eventId");
                            Boolean notified = doc.getBoolean("notified");

                            if ("INVITED".equals(status) && !Boolean.TRUE.equals(notified)) {
                                doc.getReference().update("notified", true);
                                fetchEventNameAndNotify(eventId, true);
                            }
                        }
                    }
                });

        // Listener 2: cancellation notifications from organizer
        cancellationListenerRegistration = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("deviceId", deviceId)
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    if (snapshots.getMetadata().isFromCache()) return;

                    for (com.google.firebase.firestore.DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            QueryDocumentSnapshot doc = change.getDocument();
                            String type = doc.getString("type");
                            String message = doc.getString("message");

                            if ("CANCELLED".equals(type) && message != null) {
                                doc.getReference().update("read", true);
                                showCancellationNotification(message);
                            }
                        }
                    }
                });
    }

    /**
     * Stop listening — call this when the app is destroyed.
     */
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        if (cancellationListenerRegistration != null) {
            cancellationListenerRegistration.remove();
        }
    }

    private void fetchEventNameAndNotify(String eventId, boolean isWin) {
        new com.example.eventlottery.firebase.FirestoreProfileRepository()
                .getProfile(deviceId, new com.example.eventlottery.data.ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(com.example.eventlottery.domain.UserProfile profile) {
                        if (profile != null && !profile.isNotificationsEnabled()) return;

                        FirebaseFirestore.getInstance()
                                .collection("events")
                                .document(eventId)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    String eventName = doc.exists() && doc.getString("name") != null
                                            ? doc.getString("name")
                                            : "an event";
                                    showNotification(eventName, isWin);
                                })
                                .addOnFailureListener(e -> showNotification("an event", isWin));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        FirebaseFirestore.getInstance()
                                .collection("events")
                                .document(eventId)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    String eventName = doc.exists() && doc.getString("name") != null
                                            ? doc.getString("name")
                                            : "an event";
                                    showNotification(eventName, isWin);
                                })
                                .addOnFailureListener(e2 -> showNotification("an event", isWin));
                    }
                });
    }

    private void showNotification(String eventName, boolean isWin) {
        String title = isWin ? "🎉 You've been selected!" : "Lottery Result";
        String message = isWin
                ? "You have been invited to join: " + eventName
                : "You were not selected for: " + eventName;

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_clipboard)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_BASE + eventName.hashCode(), builder.build());
        }

        if (inviteListener != null && isWin) {
            inviteListener.onInviteReceived(eventName);
        }
    }

    private void showCancellationNotification(String message) {
        new com.example.eventlottery.firebase.FirestoreProfileRepository()
                .getProfile(deviceId, new com.example.eventlottery.data.ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(com.example.eventlottery.domain.UserProfile profile) {
                        if (profile != null && !profile.isNotificationsEnabled()) return;

                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(
                                context, 0, intent,
                                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_clipboard)
                                .setContentTitle("Waitlist Update")
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pendingIntent);

                        NotificationManager manager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (manager != null) {
                            manager.notify(NOTIFICATION_ID_BASE + message.hashCode(), builder.build());
                        }
                    }

                    @Override
                    public void onFailure(Exception e) { }
                });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for event lottery results");
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
