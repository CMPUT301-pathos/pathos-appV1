package com.example.eventlottery.controller;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.service.PathosNotifyService;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * Controller for managing notification operations.
 *
 * Acts as an intermediary between the UI and notification services.
 *
 * Responsibilities:
 * - Send win notifications to selected entrants
 * - Retrieve notification history for a user
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen from waiting list
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see PathosNotifyService
 * @see NotificationLogRepository
 */

public class NotificationController {
    private final PathosNotifyService notifyService;
    private final NotificationLogRepository notificationRepo;

    /**
     * Creates a notification controller that bridges UI behavior and notification services.
     *
     * @param notifyService service used to send notifications
     * @param notificationRepo repository used to read notification history
     */
    public NotificationController(PathosNotifyService notifyService, NotificationLogRepository notificationRepo) {
        this.notifyService = notifyService;
        this.notificationRepo = notificationRepo;
    }

    /**
     * Sends a win notification to a user for a specific event.
     *
     * @param userId the recipient device ID
     * @param eventId the event ID
     * @param eventName event name included in the notification message
     * @return task representing the async notification send operation
     */
    public Task<Void> sendWinNotification(String userId, String eventId, String eventName) {
        String msg = "You were selected for " + eventName + "!";
        return notifyService.notifyWin(userId, eventId, msg);
    }

    /**
     * Retrieves a list of notifications for a given user.
     *
     * @param userId the user's device ID
     * @param limit maximum number of records to return
     * @return task that resolves to the user's notification history
     */
    public Task<List<NotificationRecord>> getMyNotifications(String userId, int limit) {
        return notificationRepo.listForUser(userId, limit);
    }
}