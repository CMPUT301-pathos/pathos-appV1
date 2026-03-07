package com.example.eventlottery.service;

import com.example.eventlottery.data.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.domain.NotificationType;
import com.google.android.gms.tasks.Task;

public class PathosNotifyService {
    private final NotificationLogRepository notificationRepo;

    public PathosNotifyService(NotificationLogRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    public Task<Void> notifyWin(String recipientId, String eventId, String message) {
        NotificationRecord record = new NotificationRecord(recipientId, eventId, NotificationType.WIN, message);
        return notificationRepo.add(record);
    }
}