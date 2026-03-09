package com.example.eventlottery.controller;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.service.PathosNotifyService;
import com.google.android.gms.tasks.Task;
import java.util.List;

public class NotificationController {

    private final PathosNotifyService notifyService;
    private final NotificationLogRepository notificationRepo;

    private final ProfileRepository profileRepository;

    public NotificationController(PathosNotifyService notifyService,
                                  NotificationLogRepository notificationRepo,
                                  ProfileRepository profileRepository) {
        this.notifyService = notifyService;
        this.notificationRepo = notificationRepo;
        this.profileRepository = profileRepository;
    }

    public Task<Void> sendWinNotification(String userId, String eventId, String eventName) {
        String msg = "You were selected for " + eventName + "!";

        profileRepository.getProfile(userId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(com.example.eventlottery.domain.UserProfile profile) {
                if (profile != null && profile.getNotifications()) {
                    notifyService.notifyWin(userId, eventId, msg);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });

        return null;
    }

    public Task<List<NotificationRecord>> getMyNotifications(String userId, int limit) {
        return notificationRepo.listForUser(userId, limit);
    }

    // author wasnt specified but edited by Heorhii Litvinov
}