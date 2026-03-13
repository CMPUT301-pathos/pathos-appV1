package com.example.eventlottery.service;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.domain.NotificationType;
import com.google.android.gms.tasks.Task;

/**
 * Service responsible for sending and logging entrant-facing notifications.
 *
 * Responsibilities:
 * - Create WIN notification records when an entrant is selected from the lottery
 * - Persist notification records via {@link NotificationLogRepository}
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen from waiting list
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see NotificationLogRepository
 * @see NotificationRecord
 */
public class PathosNotifyService {

    private final NotificationLogRepository notificationRepo;

    public PathosNotifyService(NotificationLogRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    /**
     * Create/log a "WIN" notification for the recipient.
     *
     * @param recipientId entrant/profile id (or device id mapping, depending on your design)
     * @param eventId     event id
     * @param message     message to display in the in-app notifications list
     */
    public Task<Void> notifyWin(String recipientId, String eventId, String message) {
        NotificationRecord record = new NotificationRecord(recipientId, eventId, NotificationType.WIN, message);
        return notificationRepo.add(record);
    }
}