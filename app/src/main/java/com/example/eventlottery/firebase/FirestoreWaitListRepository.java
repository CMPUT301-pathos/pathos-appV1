package com.example.eventlottery.firebase;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore implementation of {@link WaitListRepository}.
 * Stores participation records in collection: "waitlist".
 * Document IDs follow the format: {eventId}_{deviceId}.
 *
 * Responsibilities:
 * - Persist entrant participation records to Firestore
 * - Update participation status in Firestore
 * - Provide async queries for lottery draw operations
 *
 * User stories supported:
 * - US 01.01.01: Join the waiting list for a specific event
 * - US 01.01.02: Leave the waiting list for a specific event
 * - US 01.05.01: Another chance to be chosen when someone declines
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 * - US 02.05.02: Sample a specified number of attendees
 * - US 02.05.03: Draw a replacement applicant
 *
 * @author Fawaz Mansoor, Dmitriy Limanets
 * @version 1.1
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

    @Override
    public void getRecordsByStatusAsync(String eventId, WaitStatus status, WaitListCallBack callback) {
        db.collection(COLLECTION)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", status.name())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WaitListRecord> records = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WaitListRecord record = new WaitListRecord(
                                doc.getString("eventId"),
                                doc.getString("deviceId")
                        );
                        record.setStatus(WaitStatus.valueOf(doc.getString("status")));
                        records.add(record);
                    }
                    callback.onSuccess(records);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getRecordAsync(String eventId, String deviceId, SingleRecordCallback callback) {
        db.collection(COLLECTION)
                .document(eventId + "_" + deviceId)
                .get()
                .addOnSuccessListener(doc ->{
                    if(doc.exists()){
                        WaitListRecord record = new WaitListRecord(
                                doc.getString("eventId"),
                                doc.getString("deviceId")
                        );
                        record.setStatus(WaitStatus.valueOf(doc.getString("status")));
                        callback.onSuccess(record);
                    }else{
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);

    }

    @Override
    public void addToWaitList(WaitListRecord record, OperationCallback callback) {
        db.collection(COLLECTION)
                .document(record.getEventId() + "_" + record.getDeviceId())
                .set(record)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void removeFromWaitList(String eventId, String deviceId, OperationCallback callback) {
        db.collection(COLLECTION)
                .document(eventId + "_" + deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }



}