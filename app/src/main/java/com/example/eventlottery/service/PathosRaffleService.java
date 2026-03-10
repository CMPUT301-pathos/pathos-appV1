package com.example.eventlottery.service;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for randomly selecting entrants from the waiting list.
 *
 * Responsibilities:
 * - Draw an initial batch of entrants for an event
 * - Draw a single replacement when a selected entrant declines or is cancelled
 *
 * User stories supported:
 * - US 01.05.01: Another chance to be chosen when someone declines
 * - US 02.05.02: Sample a specified number of attendees
 * - US 02.05.03: Draw a replacement applicant
 *
 * @author Dmitriy Limanets
 * @version 1.0
 */
public class PathosRaffleService {

    private final WaitListRepository waitListRepository;

    public PathosRaffleService(WaitListRepository waitListRepository) {
        this.waitListRepository = waitListRepository;
    }

    /**
     * Callback for draw results.
     */
    public interface RaffleCallback {
        void onDrawComplete(List<WaitListRecord> selected);
        void onFailure(Exception e);
    }

    /**
     * Draw an initial batch of entrants from the waiting list.
     * If fewer WAITING entrants exist than count, all are invited.
     *
     * @param eventId the event to draw for
     * @param count   number of entrants to select
     * @param callback returns the list of selected entrants
     */
    public void drawInitial(String eventId, int count, RaffleCallback callback) {
        waitListRepository.getRecordsByStatusAsync(eventId, WaitStatus.WAITING,
                new WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(List<WaitListRecord> waitingEntrants) {
                        if (waitingEntrants.isEmpty()) {
                            callback.onDrawComplete(new ArrayList<>());
                            return;
                        }

                        Collections.shuffle(waitingEntrants);
                        int drawCount = Math.min(count, waitingEntrants.size());
                        List<WaitListRecord> selected = waitingEntrants.subList(0, drawCount);

                        for (WaitListRecord record : selected) {
                            waitListRepository.updateStatus(
                                    record.getEventId(), record.getDeviceId(), WaitStatus.INVITED
                            );
                        }

                        callback.onDrawComplete(new ArrayList<>(selected));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Draw a single replacement entrant from remaining WAITING entrants.
     * Called by the organizer when a selected entrant declines or is cancelled.
     *
     * @param eventId  the event to draw a replacement for
     * @param callback returns a list containing the single replacement, or empty if none available
     */
    public void drawReplacement(String eventId, RaffleCallback callback) {
        drawInitial(eventId, 1, callback);
    }
}
