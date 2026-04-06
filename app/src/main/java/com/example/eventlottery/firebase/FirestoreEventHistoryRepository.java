package com.example.eventlottery.firebase;

import com.example.eventlottery.data.memory.EventHistoryRepository;
import com.example.eventlottery.domain.EventHistoryRecord;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore implementation of {@link EventHistoryRepository}.
 * Stores and retrieves event history records from the "event_history" collection.
 * Document IDs use the format {deviceId}_{eventId} to ensure uniqueness
 * and prevent duplicate entries per user per event.
 *
 * User stories supported:
 * - US 01.02.03: View history of events registered for, selected or not
 *
 * @author Hasrat Singh Chauhan
 * @version 1.1
 * @see EventHistoryRepository
 * @see EventHistoryRecord
 */
public class FirestoreEventHistoryRepository implements EventHistoryRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "event_history";

    /**
     * Constructs a new FirestoreEventHistoryRepository using the default
     * Firestore instance.
     */
    public FirestoreEventHistoryRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves all event history records for a specific user from Firestore.
     * Results are ordered by timestamp descending so the most recent events
     * appear first.
     *
     * @param deviceId the unique device identifier of the user
     * @param callback the callback to handle success or failure
     */
    @Override
    public void getHistory(String deviceId, EventHistoryCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("deviceId", deviceId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventHistoryRecord> historyList = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        historyList = queryDocumentSnapshots.toObjects(EventHistoryRecord.class);
                    }
                    callback.onSuccess(historyList);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Saves a single event history record to Firestore.
     * Uses a compound document ID ({deviceId}_{eventId}) to prevent
     * duplicate entries for the same user and event combination.
     *
     * @param record   the event history record to save
     * @param callback the callback to handle success or failure;
     *                 onSuccess receives a list containing the saved record
     */
    @Override
    public void saveHistoryRecord(EventHistoryRecord record, EventHistoryCallback callback) {
        String docId = record.getDeviceId() + "_" + record.getEventId();
        db.collection(COLLECTION_NAME)
                .document(docId)
                .set(record)
                .addOnSuccessListener(aVoid -> {
                    List<EventHistoryRecord> singleRecord = new ArrayList<>();
                    singleRecord.add(record);
                    callback.onSuccess(singleRecord);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Saves multiple event history records to Firestore using a batch write.
     * Batch writes are atomic — either all records are saved or none are.
     * If the provided list is null or empty, onSuccess is called immediately
     * with an empty list without contacting Firestore.
     *
     * @param records  the list of event history records to save
     * @param callback the callback to handle success or failure;
     *                 onSuccess receives the original list of saved records
     */
    @Override
    public void saveAllHistory(List<EventHistoryRecord> records, EventHistoryCallback callback) {
        if (records == null || records.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        var batch = db.batch();
        for (EventHistoryRecord record : records) {
            String docId = record.getDeviceId() + "_" + record.getEventId();
            var docRef = db.collection(COLLECTION_NAME).document(docId);
            batch.set(docRef, record);
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(records))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes a specific event history record from Firestore identified by
     * the compound document ID ({deviceId}_{eventId}).
     *
     * @param deviceId the device ID of the user
     * @param eventId  the event ID of the record to delete
     * @param callback the callback to handle success or failure;
     *                 onSuccess receives an empty list
     */
    @Override
    public void deleteHistoryRecord(String deviceId, String eventId, EventHistoryCallback callback) {
        String docId = deviceId + "_" + eventId;
        db.collection(COLLECTION_NAME)
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(new ArrayList<>()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes all event history records for a specific user from Firestore.
     * Queries all records matching the device ID and deletes them in a
     * single batch operation. Useful when a user deletes their account.
     * If no records exist for the user, onSuccess is called immediately
     * with an empty list.
     *
     * @param deviceId the device ID of the user whose history should be cleared
     * @param callback the callback to handle success or failure;
     *                 onSuccess receives an empty list
     */
    @Override
    public void deleteAllHistory(String deviceId, EventHistoryCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    var batch = db.batch();
                    for (var document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess(new ArrayList<>()))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
