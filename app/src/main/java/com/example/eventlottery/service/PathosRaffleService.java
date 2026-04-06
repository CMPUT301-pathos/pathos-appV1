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
 * - Mark entrants who were not chosen in the initial draw as NOT_SELECTED
 * - Draw a single replacement when a selected entrant declines or is cancelled
 *
 * Raffle semantics:
 * - Initial draw:
 *   - selected entrants are marked INVITED
 *   - non-selected entrants are marked NOT_SELECTED
 * - Replacement draw:
 *   - only one remaining WAITING entrant is promoted to INVITED
 *   - remaining WAITING entrants are left unchanged
 *
 * Status semantics:
 * - WAITING: entrant is still eligible for a future draw
 * - INVITED: entrant has been selected
 * - ACCEPTED: entrant accepted the invitation
 * - DECLINED: entrant was invited and declined
 * - CANCELLED: entrant was cancelled by the organizer/system
 * - NOT_SELECTED: entrant participated in a raffle round but was not chosen
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen
 * - US 01.05.01: Another chance to be chosen when someone declines
 * - US 02.05.02: Sample a specified number of attendees
 * - US 02.05.03: Draw a replacement applicant
 *
 * Revision note:
 * - Updated to use NOT_SELECTED instead of DECLINED for entrants
 *   who were not chosen in the initial draw.
 * - Updated replacement draw logic so it no longer reuses the
 *   initial-draw method and incorrectly reassigns statuses.
 *
 * @author Dmitriy Limanets, Kenneth Joseph
 * @version 1.2
 */
public class PathosRaffleService {

    private final WaitListRepository waitListRepository;

    /**
     * Constructs the raffle service using the provided wait-list repository.
     *
     * @param waitListRepository repository used to read and update waiting list records
     */
    public PathosRaffleService(WaitListRepository waitListRepository) {
        this.waitListRepository = waitListRepository;
    }

    /**
     * Callback for draw results.
     */
    public interface RaffleCallback {
        /**
         * Called when the draw completes successfully.
         *
         * @param selected the list of selected entries
         */
        void onDrawComplete(List<WaitListRecord> selected);

        /**
         * Called when the draw operation fails.
         *
         * @param e exception describing the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Draws an initial batch of entrants from the WAITING list.
     *
     * Behavior:
     * - randomly shuffles all WAITING entrants
     * - marks the selected subset as INVITED
     * - marks the non-selected subset as NOT_SELECTED
     *
     * If fewer WAITING entrants exist than the requested count,
     * all WAITING entrants are invited.
     *
     * @param eventId event identifier
     * @param count number of entrants to invite
     * @param callback returns the selected entrants
     */
    public void drawInitial(String eventId, int count, RaffleCallback callback) {
        waitListRepository.getRecordsByStatusAsync(
                eventId,
                WaitStatus.WAITING,
                new WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(List<WaitListRecord> waitingEntrants) {
                        if (waitingEntrants == null || waitingEntrants.isEmpty()) {
                            callback.onDrawComplete(new ArrayList<>());
                            return;
                        }

                        Collections.shuffle(waitingEntrants);

                        int drawCount = Math.min(count, waitingEntrants.size());
                        List<WaitListRecord> selected =
                                new ArrayList<>(waitingEntrants.subList(0, drawCount));
                        List<WaitListRecord> notSelected =
                                new ArrayList<>(waitingEntrants.subList(drawCount, waitingEntrants.size()));

                        for (WaitListRecord record : selected) {
                            waitListRepository.updateStatus(
                                    record.getEventId(),
                                    record.getDeviceId(),
                                    WaitStatus.INVITED
                            );
                        }

                        for (WaitListRecord record : notSelected) {
                            waitListRepository.updateStatus(
                                    record.getEventId(),
                                    record.getDeviceId(),
                                    WaitStatus.NOT_SELECTED
                            );
                        }

                        callback.onDrawComplete(selected);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                }
        );
    }

    /**
     * Draws a single replacement entrant from the remaining WAITING list.
     *
     * Behavior:
     * - randomly selects one entrant still marked WAITING
     * - marks that entrant as INVITED
     * - leaves all other WAITING entrants unchanged
     *
     * This method is intentionally separate from drawInitial(...) so that
     * replacement draws do not re-mark other entrants as NOT_SELECTED.
     *
     * @param eventId event identifier
     * @param callback returns a list containing the replacement entrant,
     *                 or an empty list if none are available
     */
    public void drawReplacement(String eventId, RaffleCallback callback) {
        waitListRepository.getRecordsByStatusAsync(
                eventId,
                WaitStatus.WAITING,
                new WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(List<WaitListRecord> waitingEntrants) {
                        if (waitingEntrants == null || waitingEntrants.isEmpty()) {
                            callback.onDrawComplete(new ArrayList<>());
                            return;
                        }

                        Collections.shuffle(waitingEntrants);
                        WaitListRecord replacement = waitingEntrants.get(0);

                        waitListRepository.updateStatus(
                                replacement.getEventId(),
                                replacement.getDeviceId(),
                                WaitStatus.INVITED
                        );

                        List<WaitListRecord> selected = new ArrayList<>();
                        selected.add(replacement);
                        callback.onDrawComplete(selected);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                }
        );
    }
}