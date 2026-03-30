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

    public WaitingListController(WaitListRepository waitListRepository) {
        this.waitListRepository = waitListRepository;
    }

    public void acceptInvitation(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        record.setStatus(WaitStatus.INVITED);
        record.acceptInvitation();
        waitListRepository.updateStatus(eventId, deviceId, WaitStatus.ACCEPTED);
    }

    public void declineInvitation(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        waitListRepository.updateStatus(eventId, deviceId, WaitStatus.DECLINED);
    }

    public void joinWaitingList(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        waitListRepository.addToWaitList(record);
    }

    public void joinWaitingList(WaitListRecord record) {
        waitListRepository.addToWaitList(record);
    }

    public void leaveWaitingList(String eventId, String deviceId) {
        waitListRepository.removeFromWaitList(eventId, deviceId);
    }

    public interface CountCallback {
        void onCount(int count);
        void onFailure(Exception e);
    }

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

    public void checkIfJoined(String eventId,
                              String deviceId,
                              WaitListRepository.SingleRecordCallback callback) {
        waitListRepository.getRecordAsync(eventId, deviceId, callback);
    }

    public void joinWaitingList(String eventId,
                                String deviceId,
                                WaitListRepository.OperationCallback callback) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        waitListRepository.addToWaitList(record, callback);
    }

    public void joinWaitingList(WaitListRecord record,
                                WaitListRepository.OperationCallback callback) {
        waitListRepository.addToWaitList(record, callback);
    }

    public void leaveWaitingList(String eventId,
                                 String deviceId,
                                 WaitListRepository.OperationCallback callback) {
        waitListRepository.removeFromWaitList(eventId, deviceId, callback);
    }
}