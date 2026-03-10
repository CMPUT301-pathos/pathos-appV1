package com.example.eventlottery.controller;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

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
 * - US 01.05.03: Decline an invitation when chosen
 *
 * @author Fawaz Mansoor
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
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        waitListRepository.addToWaitList(record);
    }

    public void leaveWaitingList(String eventId, String deviceId) {
        waitListRepository.removeFromWaitList(eventId, deviceId);
    }
}
