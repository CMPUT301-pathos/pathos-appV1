package com.example.eventlottery.controller;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.service.PathosNotifyService;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class NotificationController {

    private final PathosNotifyService notifyService;
    private final NotificationLogRepository notificationRepo;

    public NotificationController(PathosNotifyService notifyService, NotificationLogRepository notificationRepo) {
        this.notifyService = notifyService;
        this.notificationRepo = notificationRepo;
    }

    public Task<Void> sendWinNotification(String userId, String eventId, String eventName) {
        String msg = "You were selected for " + eventName + "!";
        return notifyService.notifyWin(userId, eventId, msg);
    }

    public Task<List<NotificationRecord>> getMyNotifications(String userId, int limit) {
        return notificationRepo.listForUser(userId, limit);
    }
}