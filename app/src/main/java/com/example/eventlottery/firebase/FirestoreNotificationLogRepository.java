package com.example.eventlottery.firebase;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore implementation of {@link NotificationLogRepository}.
 * Stores and retrieves notification records from the "notifications" collection.
 *
 * Responsibilities:
 * - Persist notification records to Firestore
 * - Retrieve notification history for a specific user, ordered by most recent
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen from waiting list
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see NotificationLogRepository
 * @see NotificationRecord
 */
public class FirestoreNotificationLogRepository implements NotificationLogRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public Task<Void> add(NotificationRecord record) {
        // notifications collection
        CollectionReference ref = db.collection("notifications");
        DocumentReference doc = ref.document();
        record.id = doc.getId();
        return doc.set(record);
    }

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