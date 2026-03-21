package com.example.eventlottery.service;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.domain.NotificationType;
import com.google.android.gms.tasks.Task;

/**
 * Service responsible for sending and logging entrant-facing notifications.
 *
 * Responsibilities:
 * - Create WIN notification records when an entrant is selected
 * - Create LOSE/NOT_SELECTED notification records when an entrant is not chosen
 * - Persist notification records via {@link NotificationLogRepository}
 *
 * Note:
 * - This service is optional for not-selected UI if the app already uses
 *   waitlist statuses directly in EntrantInvitationFragment.
 * - It is still useful for maintaining a notification log.
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen from waiting list
 *
 * Revision note:
 * - Added support for not-selected entrant notifications.
 *
 * @author Fawaz Mansoor, Kenneth Joseph
 * @version 1.2
 * @see NotificationLogRepository
 * @see NotificationRecord
 */
public class PathosNotifyService {

    private final NotificationLogRepository notificationRepo;

    public PathosNotifyService(NotificationLogRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    /**
     * Create/log a WIN notification for a selected entrant.
     *
     * @param recipientId entrant device id or mapped profile id
     * @param eventId event identifier
     * @param message message shown in the in-app notifications list
     * @return asynchronous Firestore task
     */
    public Task<Void> notifyWin(String recipientId, String eventId, String message) {
        NotificationRecord record = new NotificationRecord(
                recipientId,
                eventId,
                NotificationType.WIN,
                message
        );
        return notificationRepo.add(record);
    }

    /**
     * Create/log a NOT_SELECTED notification for an entrant who was not chosen.
     *
     * @param recipientId entrant device id or mapped profile id
     * @param eventId event identifier
     * @param message message shown in the in-app notifications list
     * @return asynchronous Firestore task
     */
    public Task<Void> notifyNotSelected(String recipientId, String eventId, String message) {
        NotificationRecord record = new NotificationRecord(
                recipientId,
                eventId,
                NotificationType.LOSE,
                message
        );
        return notificationRepo.add(record);
    }
}