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
    void addToWaitList(WaitListRecord record);
    void removeFromWaitList(String eventId, String deviceId);
    void updateStatus(String eventId, String deviceId, WaitStatus newStatus);
    WaitListRecord getRecord(String eventId, String deviceId);
    List<WaitListRecord> getRecordsByEvent(String eventId);
    List<WaitListRecord> getRecordsByStatus(String eventId, WaitStatus status);


    /**
     * Callback interface for asynchronous waiting list queries.
     */
    interface WaitListCallBack{
        void onSuccess(List<WaitListRecord> records);
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

    void getRecordAsync(String eventId, String deviceId, SingleRecordCallback callback);

    interface SingleRecordCallback{
        void onSuccess(WaitListRecord record); //null if not found
        void onFailure(Exception e);
    }

    interface OperationCallback{
        void onSuccess();
        void onFailure(Exception e);
    }

    void addToWaitList(WaitListRecord record, OperationCallback callback);
    void removeFromWaitList(String eventId, String deviceId, OperationCallback callback);

}