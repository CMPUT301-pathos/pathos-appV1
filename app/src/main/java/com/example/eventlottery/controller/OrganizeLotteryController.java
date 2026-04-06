package com.example.eventlottery.controller;

import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.service.PathosNotifyService;
import com.example.eventlottery.service.PathosRaffleService;

import java.util.List;

/**
 * Controller that orchestrates the lottery draw process for organizers.
 *
 * Responsibilities:
 * - Trigger initial draw for an event
 * - Trigger replacement draw when an entrant declines or is cancelled
 * - Send win notifications to selected entrants
 *
 * User stories supported:
 * - US 01.05.01: Another chance to be chosen when someone declines
 * - US 02.05.02: Sample a specified number of attendees
 * - US 02.05.03: Draw a replacement applicant
 * - US 02.05.01: Send notification to chosen entrants
 *
 * @author Dmitriy Limanets
 * @version 1.0
 */
public class OrganizeLotteryController {

    private final PathosRaffleService raffleService;
    private final PathosNotifyService notifyService;

    /**
     * Callback interface for lottery draw results.
     */
    public interface LotteryCallback {
        /**
         * Called when the draw completes successfully.
         *
         * @param selected list of selected waitlist records
         */
        void onSuccess(List<WaitListRecord> selected);

        /**
         * Called when the draw fails.
         *
         * @param e exception describing the failure
         */
        void onFailure(Exception e);
    }

    public OrganizeLotteryController(PathosRaffleService raffleService,
                                     PathosNotifyService notifyService) {
        this.raffleService = raffleService;
        this.notifyService = notifyService;
    }

    /**
     * Run the initial lottery draw and notify all selected entrants.
     *
     * @param eventId   the event to draw for
     * @param eventName event name for the notification message
     * @param count     number of entrants to select
     * @param callback  returns the selected entrants
     */
    /**
     * Executes the initial lottery draw for an event and sends notifications to selected entrants.
     *
     * @param eventId the event to draw for
     * @param eventName the event name used in notification text
     * @param count number of entrants to select
     * @param callback callback invoked after draw success or failure
     */
    public void runInitialDraw(String eventId, String eventName, int count, LotteryCallback callback) {
        raffleService.drawInitial(eventId, count, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                for (WaitListRecord record : selected) {
                    notifyService.notifyWin(record.getDeviceId(), eventId,
                            "You were selected for " + eventName + "!");
                }
                callback.onSuccess(selected);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Draw a single replacement and notify them.
     * Called by the organizer when a previously selected entrant
     * declines or is cancelled.
     *
     * @param eventId   the event to draw a replacement for
     * @param eventName event name for the notification message
     * @param callback  returns the replacement (single-element list), or empty if none available
     */
    /**
     * Executes a replacement lottery draw and sends a notification to the newly selected entrant.
     *
     * @param eventId the event for which to draw a replacement
     * @param eventName the event name used in notification text
     * @param callback callback invoked after replacement draw
     */
    public void runReplacementDraw(String eventId, String eventName, LotteryCallback callback) {
        raffleService.drawReplacement(eventId, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                for (WaitListRecord record : selected) {
                    notifyService.notifyWin(record.getDeviceId(), eventId,
                            "A spot opened up! You were selected for " + eventName + "!");
                }
                callback.onSuccess(selected);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}