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

    void getHistory(String deviceId, FirestoreEventHistoryRepository.EventHistoryCallback callback);

    void saveHistoryRecord(EventHistoryRecord record, FirestoreEventHistoryRepository.EventHistoryCallback callback);

    void saveAllHistory(List<EventHistoryRecord> records, FirestoreEventHistoryRepository.EventHistoryCallback callback);

    void deleteHistoryRecord(String deviceId, String eventId, FirestoreEventHistoryRepository.EventHistoryCallback callback);

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