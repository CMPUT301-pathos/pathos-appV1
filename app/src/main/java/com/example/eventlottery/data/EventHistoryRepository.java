package com.example.eventlottery.data.memory;

import com.example.eventlottery.domain.EventHistoryRecord;
import com.example.eventlottery.firebase.FirestoreEventHistoryRepository;

import java.util.List;

/**
 * Interface for managing event history data operations.
 * Defines async methods for retrieving, saving, and deleting
 * event history records for a given user device.
 *
 * User stories supported:
 * - US 01.02.03: View history of events registered for, selected or not
 *
 * @author Hasrat Singh Chauhan
 * @version 1.1
 * @see EventHistoryRecord
 * @see FirestoreEventHistoryRepository
 */
public interface EventHistoryRepository {

    /**
     * Callback interface for handling asynchronous event history operations.
     * Implementations receive either a list of records on success or an
     * exception on failure.
     */
    interface EventHistoryCallback {

        /**
         * Called when the operation completes successfully.
         *
         * @param historyList the resulting list of event history records,
         *                    may be empty but never null
         */
        void onSuccess(List<EventHistoryRecord> historyList);

        /**
         * Called when the operation fails.
         *
         * @param e the exception that caused the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Retrieves all event history records for a specific user, ordered
     * by most recent first.
     *
     * @param deviceId the unique device identifier of the user
     * @param callback the callback to handle success or failure
     */
    void getHistory(String deviceId, EventHistoryCallback callback);

    /**
     * Saves a single event history record. If a record with the same
     * device ID and event ID already exists, it will be overwritten.
     *
     * @param record   the event history record to save
     * @param callback the callback to handle success or failure
     */
    void saveHistoryRecord(EventHistoryRecord record, EventHistoryCallback callback);

    /**
     * Saves multiple event history records in a single batch operation.
     * More efficient than calling saveHistoryRecord repeatedly.
     * If the list is null or empty, onSuccess is called immediately
     * with an empty list.
     *
     * @param records  the list of event history records to save
     * @param callback the callback to handle success or failure
     */
    void saveAllHistory(List<EventHistoryRecord> records, EventHistoryCallback callback);

    /**
     * Deletes a specific history record identified by device ID and event ID.
     *
     * @param deviceId the device ID of the user
     * @param eventId  the event ID to delete from history
     * @param callback the callback to handle success or failure
     */
    void deleteHistoryRecord(String deviceId, String eventId, EventHistoryCallback callback);

    /**
     * Deletes all history records for a specific user.
     * Useful when the user deletes their account.
     *
     * @param deviceId the device ID of the user
     * @param callback the callback to handle success or failure
     */
    void deleteAllHistory(String deviceId, EventHistoryCallback callback);
}