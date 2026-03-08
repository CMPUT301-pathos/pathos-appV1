package com.example.eventlottery.controller;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

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
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        waitListRepository.addToWaitList(record);
    }

    public void leaveWaitingList(String eventId, String deviceId) {
        waitListRepository.removeFromWaitList(eventId, deviceId);
    }
}
