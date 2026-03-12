package com.example.eventlottery.firebase;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    /**
     * Adds a participant record to the waiting list in Firestore.
     *
     * The record is stored in the "waitlist" collection using a document ID
     * composed of the event ID and device ID to guarantee uniqueness.
     *
     * @param record the WaitListRecord representing the participant
     */
    @Override
    public void addToWaitList(WaitListRecord record) {
        db.collection(COLLECTION)
                .document(record.getEventId() + "_" + record.getDeviceId())
                .set(record);
    }

    /**
     * Removes a participant from the waiting list.
     *
     * The corresponding document is deleted from the Firestore collection
     * using the combined eventId_deviceId identifier.
     *
     * @param eventId the event identifier
     * @param deviceId the participant device identifier
     */
    @Override
    public void removeFromWaitList(String eventId, String deviceId) {
        db.collection(COLLECTION)
                .document(eventId + "_" + deviceId)
                .delete();
    }

    /**
     * Updates the participation status of a waiting list record.
     *
     * The status field in Firestore is updated to reflect changes such as
     * INVITED, ACCEPTED, or DECLINED.
     *
     * @param eventId the event identifier
     * @param deviceId the participant device identifier
     * @param newStatus the new waiting list status
     */
    @Override
    public void updateStatus(String eventId, String deviceId, WaitStatus newStatus) {
        db.collection(COLLECTION)
                .document(eventId + "_" + deviceId)
                .update("status", newStatus.name());
    }

    /**
     * Retrieves a specific waiting list record for an event and user.
     *
     * Note: Firestore operations are asynchronous, so this method is
     * currently not implemented. Use asynchronous queries instead.
     *
     * @param eventId the event identifier
     * @param deviceId the participant device identifier
     * @return null (method not implemented)
     */
    @Override
    public WaitListRecord getRecord(String eventId, String deviceId) {
        // Firestore reads are async — use getRecordsByEvent for now
        return null;
    }

    /**
     * Retrieves all waiting list records for a specific event.
     *
     * Note: Firestore operations are asynchronous, so this method currently
     * returns an empty list. Snapshot listeners or async queries should be
     * used instead.
     *
     * @param eventId the event identifier
     * @return empty list (method not implemented)
     */
    @Override
    public List<WaitListRecord> getRecordsByEvent(String eventId) {
        // Async — return empty list, use snapshot listeners in UI
        return new ArrayList<>();
    }

    /**
     * Retrieves waiting list records for a specific event filtered by status.
     *
     * Note: Firestore operations are asynchronous, so this method currently
     * returns an empty list. Async queries should be used instead.
     *
     * @param eventId the event identifier
     * @param status the waiting list status to filter by
     * @return empty list (method not implemented)
     */
    @Override
    public List<WaitListRecord> getRecordsByStatus(String eventId, WaitStatus status) {
        // Async — return empty list, use snapshot listeners in UI
        return new ArrayList<>();
    }

    /**
     * Asynchronously retrieves waiting list records for a given event
     * filtered by participation status.
     *
     * The method queries Firestore and converts each document into a
     * WaitListRecord object before returning the results via callback.
     *
     * @param eventId the event identifier
     * @param status the waiting list status to filter by
     * @param callback callback used to return the results or errors
     */
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
    @Override
    public void getRecordsByEventAsync(String eventId, WaitListCallBack callback) {
        db.collection(COLLECTION)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snap -> {
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
                    callback.onSuccess(records);
                })
                .addOnFailureListener(callback::onFailure);
    }



}