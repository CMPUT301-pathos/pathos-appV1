package com.example.eventlottery.data.memory;

import com.example.eventlottery.domain.EventHistoryRecord;
import com.example.eventlottery.firebase.FirestoreEventHistoryRepository;

import java.util.List;

/**
 * Interface for managing event history data operations.
 * Defines methods for retrieving, saving, and deleting event history records.
 * Used for US 01.02.03 - Event History display.
 *
 * @author Hasrat Singh Chauhan
 * @version 1.0
 * @see EventHistoryRecord
 * @see FirestoreEventHistoryRepository
 * @since 1.0
 */
public interface EventHistoryRepository {

    /**
     * Retrieves event history records for a given user using the Firestore callback.
     *
     * @param deviceId the user device identifier
     * @param callback callback handling the async result
     */
    void getHistory(String deviceId, FirestoreEventHistoryRepository.EventHistoryCallback callback);

    /**
     * Saves a single event history record using the Firestore callback.
     *
     * @param record history record to save
     * @param callback callback handling the async result
     */
    void saveHistoryRecord(EventHistoryRecord record, FirestoreEventHistoryRepository.EventHistoryCallback callback);

    /**
     * Saves multiple history records in one batch using the Firestore callback.
     *
     * @param records list of history records to save
     * @param callback callback handling the async result
     */
    void saveAllHistory(List<EventHistoryRecord> records, FirestoreEventHistoryRepository.EventHistoryCallback callback);

    /**
     * Deletes a specific history record using the Firestore callback.
     *
     * @param deviceId the user device identifier
     * @param eventId the event identifier
     * @param callback callback handling the async result
     */
    void deleteHistoryRecord(String deviceId, String eventId, FirestoreEventHistoryRepository.EventHistoryCallback callback);

    /**
     * Deletes all history records for a user using the Firestore callback.
     *
     * @param deviceId the user device identifier
     * @param callback callback handling the async result
     */
    void deleteAllHistory(String deviceId, FirestoreEventHistoryRepository.EventHistoryCallback callback);

    /**
     * Retrieves all event history records for a specific user.
     *
     * @param deviceId the unique device identifier of the user
     * @param callback the callback to handle success or failure
     */
    void getHistory(String deviceId, EventHistoryCallback callback);

    /**
     * Saves a single event history record.
     *
     * @param record   the event history record to save
     * @param callback the callback to handle success or failure
     */
    void saveHistoryRecord(EventHistoryRecord record, EventHistoryCallback callback);

    /**
     * Saves multiple event history records in a batch operation.
     *
     * @param records  the list of event history records to save
     * @param callback the callback to handle success or failure
     */
    void saveAllHistory(List<EventHistoryRecord> records, EventHistoryCallback callback);

    /**
     * Deletes a specific history record.
     *
     * @param deviceId the device ID of the user
     * @param eventId  the event ID to delete from history
     * @param callback the callback to handle success or failure
     */
    void deleteHistoryRecord(String deviceId, String eventId, EventHistoryCallback callback);

    /**
     * Deletes all history records for a specific user.
     *
     * @param deviceId the device ID of the user
     * @param callback the callback to handle success or failure
     */
    void deleteAllHistory(String deviceId, EventHistoryCallback callback);

    /**
     * Callback interface for handling asynchronous event history operations.
     * Follows the same pattern as ProfileCallback in ProfileRepository.
     */
    interface EventHistoryCallback {
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