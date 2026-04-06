package com.example.eventlottery.data;

import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import java.util.List;

/**
 * Repository interface for managing entrant waiting list records.
 *
 * Responsibilities:
 * - Add and remove entrants from event waiting lists
 * - Update and query participation status
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
 * @author Fawaz Mansoor
 * @modified Dmitriy Limanets
 * @version 1.1
 */

public interface WaitListRepository {
    /**
     * Adds a record to the waiting list.
     *
     * @param record waiting list record to add
     */
    void addToWaitList(WaitListRecord record);

    /**
     * Removes a record from the waiting list.
     *
     * @param eventId event identifier
     * @param deviceId device identifier for the entrant
     */
    void removeFromWaitList(String eventId, String deviceId);

    /**
     * Updates the status of an existing waiting list entry.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     * @param newStatus new waitlist status to set
     */
    void updateStatus(String eventId, String deviceId, WaitStatus newStatus);

    /**
     * Retrieves waiting list records for an event asynchronously.
     *
     * @param eventId event identifier
     * @param callback callback invoked with the retrieved records or error
     */
    void getRecordsByEventAsync(String eventId, WaitListCallBack callback);

    /**
     * Retrieves a single waiting list record synchronously.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     * @return matching waiting list record or null
     */
    WaitListRecord getRecord(String eventId, String deviceId);

    /**
     * Retrieves all waiting list records for an event.
     *
     * @param eventId event identifier
     * @return list of waitlist records
     */
    List<WaitListRecord> getRecordsByEvent(String eventId);

    /**
     * Retrieves all waiting list records for an event with the given status.
     *
     * @param eventId event identifier
     * @param status status to filter by
     * @return filtered list of waitlist records
     */
    List<WaitListRecord> getRecordsByStatus(String eventId, WaitStatus status);


    /**
     * Callback interface for asynchronous waiting list queries.
     */
    interface WaitListCallBack{
        /**
         * Called when the waitlist query completes successfully.
         *
         * @param records the retrieved waitlist records
         */
        void onSuccess(List<WaitListRecord> records);

        /**
         * Called when the waitlist query fails.
         *
         * @param e the exception describing the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Asynchronously retrieve all waiting list records for an event with a given status.
     * Used by {@link com.example.eventlottery.service.PathosRaffleService} for lottery draws.
     *
     * @param eventId  the event identifier
     * @param status   the status to filter by
     * @param callback returns the matching records or an error
     */
    void getRecordsByStatusAsync(String eventId, WaitStatus status, WaitListCallBack callback);

    /**
     * Retrieves a waiting list record asynchronously for a single entrant.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     * @param callback callback invoked with the found record or error
     */
    void getRecordAsync(String eventId, String deviceId, SingleRecordCallback callback);

    interface SingleRecordCallback{
        /**
         * Called when the single waitlist record is found.
         *
         * @param record the found record or null if not found
         */
        void onSuccess(WaitListRecord record); //null if not found

        /**
         * Called when the retrieval operation fails.
         *
         * @param e the exception describing the failure
         */
        void onFailure(Exception e);
    }

    interface OperationCallback{
        /**
         * Called when an operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when an operation fails.
         *
         * @param e the exception describing the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Adds a waiting list record and notifies the callback upon completion.
     *
     * @param record waiting list record to add
     * @param callback the callback for success or failure
     */
    void addToWaitList(WaitListRecord record, OperationCallback callback);

    /**
     * Removes a waiting list record and notifies the callback upon completion.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     * @param callback the callback for success or failure
     */
    void removeFromWaitList(String eventId, String deviceId, OperationCallback callback);

}