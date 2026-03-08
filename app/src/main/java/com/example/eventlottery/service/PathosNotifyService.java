package com.example.eventlottery.service;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.domain.NotificationType;
import com.google.android.gms.tasks.Task;

/**
 * Sends entrant-facing notifications and logs them through {@link NotificationLogRepository}.
 *
 * MVP scope for US 01.04.01: create a WIN notification record when an entrant is selected.
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