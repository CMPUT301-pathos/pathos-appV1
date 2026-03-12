package com.example.eventlottery.firebase;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore implementation of the NotificationLogRepository.
 *
 * This repository is responsible for storing and retrieving notification
 * records from the Firestore database. Notifications are stored in the
 * "notifications" collection and can be queried for specific users.
 */
public class FirestoreNotificationLogRepository implements NotificationLogRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Adds a notification record to the Firestore "notifications" collection.
     *
     * A new document ID is generated and assigned to the record before it
     * is saved to the database.
     *
     * @param record the notification record to store
     * @return a Task representing the asynchronous write operation
     */
    @Override
    public Task<Void> add(NotificationRecord record) {
        // notifications collection
        CollectionReference ref = db.collection("notifications");
        DocumentReference doc = ref.document();
        record.id = doc.getId();
        return doc.set(record);
    }

    /**
     * Retrieves a list of notifications for a specific user.
     *
     * The query filters notifications by recipient ID, orders them by
     * creation time in descending order (newest first), and limits the
     * number of results returned.
     *
     * @param userId the ID of the user receiving the notifications
     * @param limit the maximum number of notifications to retrieve
     * @return a Task containing a list of NotificationRecord objects
     */
    @Override
    public Task<List<NotificationRecord>> listForUser(String userId, int limit) {
        return db.collection("notifications")
                .whereEqualTo("recipientId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    List<NotificationRecord> out = new ArrayList<>();
                    for (DocumentSnapshot d : task.getResult().getDocuments()) {
                        NotificationRecord r = d.toObject(NotificationRecord.class);
                        if (r != null) out.add(r);
                    }
                    return out;
                });
    }
}