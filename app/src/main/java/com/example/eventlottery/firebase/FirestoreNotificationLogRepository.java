package com.example.eventlottery.firebase;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

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