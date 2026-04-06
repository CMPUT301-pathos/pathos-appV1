package com.example.eventlottery.util;

import com.example.eventlottery.controller.NotificationController;
import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.firebase.FirestoreNotificationLogRepository;
import com.example.eventlottery.service.PathosNotifyService;

public class ServiceLocator {

    private static NotificationLogRepository notificationRepo;
    private static PathosNotifyService notifyService;
    private static NotificationController notificationController;

    /**
     * Returns a singleton NotificationLogRepository instance.
     *
     * @return repository used to persist notification logs
     */
    public static NotificationLogRepository notificationRepo() {
        if (notificationRepo == null) notificationRepo = new FirestoreNotificationLogRepository();
        return notificationRepo;
    }

    /**
     * Returns a singleton PathosNotifyService instance.
     *
     * @return service used to send and log entrant notifications
     */
    public static PathosNotifyService notifyService() {
        if (notifyService == null) notifyService = new PathosNotifyService(notificationRepo());
        return notifyService;
    }

    /**
     * Returns a singleton NotificationController instance.
     *
     * @return controller coordinating notification UI and service logic
     */
    public static NotificationController notificationController() {
        if (notificationController == null) {
            notificationController = new NotificationController(notifyService(), notificationRepo());
        }
        return notificationController;
    }
}