package com.example.eventlottery.firebase;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore implementation of WaitListRepository.
 * Stores participation records in collection: "waitlist"
 *
 * Responsibilities:
 * - Persist entrant participation records
 * - Update participation status in Firestore
 *
 * User stories supported:
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class FirestoreWaitListRepository implements WaitListRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION = "waitlist";

    public FirestoreWaitListRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void addToWaitList(WaitListRecord record) {
        db.collection(COLLECTION)
                .document(record.getEventId() + "_" + record.getDeviceId())
                .set(record);
    }

    @Override
    public void removeFromWaitList(String eventId, String deviceId) {
        db.collection(COLLECTION)
                .document(eventId + "_" + deviceId)
                .delete();
    }

    @Override
    public void updateStatus(String eventId, String deviceId, WaitStatus newStatus) {
        db.collection(COLLECTION)
                .document(eventId + "_" + deviceId)
                .update("status", newStatus.name());
    }

    @Override
    public WaitListRecord getRecord(String eventId, String deviceId) {
        // Firestore reads are async — use getRecordsByEvent for now
        return null;
    }

    @Override
    public List<WaitListRecord> getRecordsByEvent(String eventId) {
        // Async — return empty list, use snapshot listeners in UI
        return new ArrayList<>();
    }

    @Override
    public List<WaitListRecord> getRecordsByStatus(String eventId, WaitStatus status) {
        // Async — return empty list, use snapshot listeners in UI
        return new ArrayList<>();
    }
}