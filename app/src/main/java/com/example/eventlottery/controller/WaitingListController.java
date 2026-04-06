package com.example.eventlottery.controller;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import java.util.List;

/**
 * Controller for managing entrant participation in events.
 *
 * Responsibilities:
 * - join and leave waiting lists
 * - accept and decline invitations
 * - allow waiting-list records with optional geolocation metadata
 * - provide waiting-list counts for an event
 *
 * User stories supported:
 * - US 01.01.01: Join the waiting list for a specific event
 * - US 01.01.02: Leave the waiting list for a specific event
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 * - US 01.05.04: Count of total entrants on the waiting list
 * - US 02.02.02: Support storing join-location data for organizer map viewing
 *
 * @author Fawaz Mansoor, Kenneth Joseph
 * @author Edwin David on US 01.05.04
 * @version 1.1
 */
public class WaitingListController {

    private final WaitListRepository waitListRepository;

    /**
     * Creates a waiting list controller backed by the repository.
     *
     * @param waitListRepository repository used for waiting list operations
     */
    public WaitingListController(WaitListRepository waitListRepository) {
        this.waitListRepository = waitListRepository;
    }

    /**
     * Accepts an invitation for a waiting list entry and updates its status.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     */
    public void acceptInvitation(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        record.setStatus(WaitStatus.INVITED);
        record.acceptInvitation();
        waitListRepository.updateStatus(eventId, deviceId, WaitStatus.ACCEPTED);
    }

    /**
     * Declines an invitation and updates the waiting list status.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     */
    public void declineInvitation(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        waitListRepository.updateStatus(eventId, deviceId, WaitStatus.DECLINED);
    }

    /**
     * Adds a new waiting list entry for the given event and device.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     */
    public void joinWaitingList(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        waitListRepository.addToWaitList(record);
    }

    /**
     * Adds the provided waiting list record to the repository.
     *
     * @param record waiting list record to add
     */
    public void joinWaitingList(WaitListRecord record) {
        waitListRepository.addToWaitList(record);
    }

    /**
     * Removes a waiting list entry for the specified event and device.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     */
    public void leaveWaitingList(String eventId, String deviceId) {
        waitListRepository.removeFromWaitList(eventId, deviceId);
    }

    public interface CountCallback {
        void onCount(int count);
        void onFailure(Exception e);
    }

    /**
     * Retrieves the count of waiting entrants for a specific event.
     *
     * @param eventId event identifier
     * @param callback callback to receive the count or error
     */
    public void getWaitingCount(String eventId, CountCallback callback) {
        waitListRepository.getRecordsByStatusAsync(
                eventId,
                WaitStatus.WAITING,
                new WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(List<WaitListRecord> records) {
                        callback.onCount(records.size());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                }
        );
    }

    /**
     * Checks whether a specific device has already joined the waiting list for an event.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     * @param callback callback invoked with the matching record or failure
     */
    public void checkIfJoined(String eventId,
                              String deviceId,
                              WaitListRepository.SingleRecordCallback callback) {
        waitListRepository.getRecordAsync(eventId, deviceId, callback);
    }

    /**
     * Adds a waiting list entry and reports completion via callback.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     * @param callback operation callback for success or failure
     */
    public void joinWaitingList(String eventId,
                                String deviceId,
                                WaitListRepository.OperationCallback callback) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        waitListRepository.addToWaitList(record, callback);
    }

    /**
     * Adds the provided waiting list record and reports completion via callback.
     *
     * @param record waiting list record to add
     * @param callback operation callback for success or failure
     */
    public void joinWaitingList(WaitListRecord record,
                                WaitListRepository.OperationCallback callback) {
        waitListRepository.addToWaitList(record, callback);
    }

    /**
     * Removes a waiting list entry and reports completion via callback.
     *
     * @param eventId event identifier
     * @param deviceId entrant device identifier
     * @param callback operation callback for success or failure
     */
    public void leaveWaitingList(String eventId,
                                 String deviceId,
                                 WaitListRepository.OperationCallback callback) {
        waitListRepository.removeFromWaitList(eventId, deviceId, callback);
    }
}