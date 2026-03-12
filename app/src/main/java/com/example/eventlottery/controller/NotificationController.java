package com.example.eventlottery.controller;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.service.PathosNotifyService;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * NotificationController
 *
 * Handles sending and retrieving notifications for users.
 *
 * Responsibilities:
 * - Send "win" notifications to entrants for lottery events.
 * - Retrieve recent notifications for a specific user.
 *
 * Collaborates with:
 * - {@link PathosNotifyService} for push notifications.
 * - {@link NotificationLogRepository} for storing/retrieving notifications.
 * - {@link ProfileRepository} (currently unused but may be used for user lookups).
 *
 * Example usage:
 * <pre>
 * NotificationController controller = new NotificationController(notifyService, notificationRepo, profileRepo);
 * controller.sendWinNotification(userId, eventId, eventName);
 * controller.getMyNotifications(userId, 20);
 * </pre>
 *
 * @author
 * @version 1.0
 */
public class NotificationController {

    private final PathosNotifyService notifyService;
    private final NotificationLogRepository notificationRepo;

    /**
     * Constructs a NotificationController.
     *
     * @param notifyService service to send push notifications
     * @param notificationRepo repository for notification records
     * @param profileRepository repository for user profiles (not currently used)
     */
    public NotificationController(PathosNotifyService notifyService, NotificationLogRepository notificationRepo, ProfileRepository profileRepository) {
        this.notifyService = notifyService;
        this.notificationRepo = notificationRepo;
    }

    /**
     * Sends a "win" notification to the user for a given event.
     *
     * @param userId ID of the user to notify
     * @param eventId ID of the event
     * @param eventName Name of the event
     * @return Task representing the asynchronous operation
     */
    public Task<Void> sendWinNotification(String userId, String eventId, String eventName) {
        String msg = "You were selected for " + eventName + "!";
        return notifyService.notifyWin(userId, eventId, msg);
    }

    /**
     * Retrieves the most recent notifications for a user.
     *
     * @param userId ID of the user
     * @param limit Maximum number of notifications to retrieve
     * @return Task containing a list of {@link NotificationRecord}
     */
    public Task<List<NotificationRecord>> getMyNotifications(String userId, int limit) {
        return notificationRepo.listForUser(userId, limit);
    }
}