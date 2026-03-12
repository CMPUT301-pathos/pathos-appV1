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
    /**
     * Accepts an invitation for a user on the event waiting list.
     *
     * This method creates a waitlist record for the user, verifies that the
     * current status is INVITED, and then updates the user's status to ACCEPTED
     * in the waitlist repository.
     *
     * @param eventId the ID of the event
     * @param deviceId the device identifier of the user
     */
    public void acceptInvitation(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        record.setStatus(WaitStatus.INVITED); // current status before accepting
        record.acceptInvitation();
        waitListRepository.updateStatus(eventId, deviceId, WaitStatus.ACCEPTED);
    }

    /**
     * Declines an invitation for a user on the event waiting list.
     *
     * The method creates a waitlist record, ensures the current status is
     * INVITED, and updates the user's status to DECLINED in the repository.
     *
     * @param eventId the ID of the event
     * @param deviceId the device identifier of the user
     */
    public void declineInvitation(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        waitListRepository.updateStatus(eventId, deviceId, WaitStatus.DECLINED);
    }

    /**
     * Adds a user to the waiting list for a specific event.
     *
     * A new waitlist record is created for the user and stored
     * in the waitlist repository.
     *
     * @param eventId the ID of the event
     * @param deviceId the device identifier of the user
     */
    public void joinWaitingList(String eventId, String deviceId) {
        WaitListRecord record = new WaitListRecord(eventId, deviceId);
        waitListRepository.addToWaitList(record);
    }
    /**
     * Removes a user from the waiting list of an event.
     *
     * This method deletes the user's waitlist record from the repository.
     *
     * @param eventId the ID of the event
     * @param deviceId the device identifier of the user
     */
    public void leaveWaitingList(String eventId, String deviceId) {
        waitListRepository.removeFromWaitList(eventId, deviceId);
    }
}
