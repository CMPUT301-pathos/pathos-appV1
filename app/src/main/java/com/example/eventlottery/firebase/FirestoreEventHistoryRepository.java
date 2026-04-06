package com.example.eventlottery.firebase;

import com.example.eventlottery.data.memory.EventHistoryRepository;
import com.example.eventlottery.domain.EventHistoryRecord;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore implementation of EventHistoryRepository.
 * Handles reading and saving event history records to Firestore.
 * History records are stored in the "event_history" collection with
 * compound document IDs (deviceId_eventId) for uniqueness.
 *
 * @author Hasrat Singh Chauhan
 * @version 1.0
 * @see EventHistoryRepository
 * @see EventHistoryRecord
 * @since 1.0
 */
public class FirestoreEventHistoryRepository implements EventHistoryRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "event_history";

    /**
     * Creates a new FirestoreEventHistoryRepository with a Firestore database instance.
     */
    public FirestoreEventHistoryRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves all event history records for a specific user from Firestore.
     * Results are ordered by timestamp descending (newest first).
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
     * Uses compound ID (deviceId_eventId) to prevent duplicates.
     *
     * @param record   the event history record to save
     * @param callback the callback to handle success or failure
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
     * Saves multiple event history records to Firestore in a batch operation.
     * More efficient than saving records individually.
     *
     * @param records  the list of event history records to save
     * @param callback the callback to handle success or failure
     */
    @Override
    public void saveAllHistory(List<EventHistoryRecord> records, EventHistoryCallback callback) {
        if (records == null || records.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Use batch for multiple writes
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
     * Deletes a specific history record from Firestore.
     *
     * @param deviceId the device ID of the user
     * @param eventId  the event ID to delete from history
     * @param callback the callback to handle success or failure
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
     * Deletes all history records for a specific user.
     * Useful when user deletes their account.
     *
     * @param deviceId the device ID of the user
     * @param callback the callback to handle success or failure
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

    /**
     * Duplicate stub methods were removed because the implemented methods above already provide
     * the required Firestore-backed behavior.
     */

    /**
     * Callback interface for asynchronous event history operations.
     * Follows the same pattern as ProfileCallback in ProfileRepository.
     */
    public interface EventHistoryCallback {
        /**
         * Called when the operation completes successfully.
         *
         * @param historyList the resulting list of event history records
         */
        void onSuccess(List<EventHistoryRecord> historyList);

        /**
         * Called when the operation fails.
         *
         * @param e the exception that caused the failure
         */
        void onFailure(Exception e);
    }
}
