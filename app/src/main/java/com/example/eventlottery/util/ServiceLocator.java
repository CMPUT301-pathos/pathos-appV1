package com.example.eventlottery.util;

import com.example.eventlottery.controller.NotificationController;
import com.example.eventlottery.data.data.NotificationLogRepository;
import com.example.eventlottery.data.firebase.FirestoreNotificationLogRepository;
import com.example.eventlottery.service.PathosNotifyService;

public class ServiceLocator {

    private static NotificationLogRepository notificationRepo;
    private static PathosNotifyService notifyService;
    private static NotificationController notificationController;

    public static NotificationLogRepository notificationRepo() {
        if (notificationRepo == null) notificationRepo = new FirestoreNotificationLogRepository();
        return notificationRepo;
    }

    public static PathosNotifyService notifyService() {
        if (notifyService == null) notifyService = new PathosNotifyService(notificationRepo());
        return notifyService;
    }

    public static NotificationController notificationController() {
        if (notificationController == null) {
            notificationController = new NotificationController(notifyService(), notificationRepo());
        }
        return notificationController;
    }
}