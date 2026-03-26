package com.example.eventlottery.controller;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import java.util.List;

/**
 * Controller for managing entrant participation in events.
 *
 * Responsibilities:
 * - Join and leave waiting lists
 * - Accept and decline invitations
 *
 * User stories supported:
 * - US 01.01.01: Join the waiting list for a specific event
 * - US 01.01.02: Leave the waiting list for a specific event
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.04: Count of total entrants on the waiting list
 * - US 01.05.03: Decline an invitation when chosen
 *
 * @author Fawaz Mansoor
 * @author Edwin David on US 01.05.04
 * @version 1.0
 */
public class WaitingListController {

    private final WaitListRepository waitListRepository;

    public WaitingListController(WaitListRepository waitListRepository) {
        this.waitListRepository = waitListRepository;
    }

    public void acceptInvitation(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        record.setStatus(WaitStatus.INVITED); // current status before accepting
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
        // default: no location
        joinWaitingList(eventId, deviceId, 0, 0);
    }
    public void joinWaitingList(String eventId, String deviceId, double latitude, double longitude) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId, latitude, longitude);
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
        waitListRepository.getRecordsByStatusAsync(eventId, WaitStatus.WAITING,
                new WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(List<WaitListRecord> records) {
                        callback.onCount(records.size());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    //New ones below using callback
    public void checkIfJoined(String eventId, String deviceId,
                              WaitListRepository.SingleRecordCallback callback) {
        waitListRepository.getRecordAsync(eventId, deviceId, callback);
    }

    public void joinWaitingList(String eventId, String deviceId,
                                WaitListRepository.OperationCallback callback) {

        WaitListRecord record = new WaitListRecord(eventId, deviceId, 0, 0);
        waitListRepository.addToWaitList(record, callback);
    }
    public void joinWaitingList(String eventId, String deviceId,
                                double latitude, double longitude,
                                WaitListRepository.OperationCallback callback) {

        WaitListRecord record = new WaitListRecord(eventId, deviceId, latitude, longitude);
        waitListRepository.addToWaitList(record, callback);
    }

    public void leaveWaitingList(String eventId, String deviceId,
                                 WaitListRepository.OperationCallback callback) {
        waitListRepository.removeFromWaitList(eventId, deviceId, callback);
    }
}
