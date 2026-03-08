package com.example.eventlottery.data;

import com.example.eventlottery.domain.NotificationRecord;
import com.google.android.gms.tasks.Task;

import java.util.List;

public interface NotificationLogRepository {
    Task<Void> add(NotificationRecord record);
    Task<List<NotificationRecord>> listForUser(String userId, int limit);
}