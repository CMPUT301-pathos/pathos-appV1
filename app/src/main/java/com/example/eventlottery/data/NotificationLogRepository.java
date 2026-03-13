package com.example.eventlottery.data;

import com.example.eventlottery.domain.NotificationRecord;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * Repository interface for managing notification log records.
 *
 * Responsibilities:
 * - Persist notification records to the data store
 * - Retrieve notification history for a specific user
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen from waiting list
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see NotificationRecord
 */

public interface NotificationLogRepository {
    Task<Void> add(NotificationRecord record);
    Task<List<NotificationRecord>> listForUser(String userId, int limit);
}