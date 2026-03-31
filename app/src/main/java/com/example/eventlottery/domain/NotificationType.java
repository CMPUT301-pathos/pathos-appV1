package com.example.eventlottery.domain;

/**
 * Enum representing the type of notification sent to an entrant.
 *
 * Used to categorize notifications in the notification log.
 *
 * User stories supported:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.04.02: Receive notification when not chosen from waiting list
 * - US 01.09.01: Receive notification when chose to be a co-organizer for an event
 * @author Fawaz Mansoor, Kenneth Joseph
 * @version 1.1
 * @see NotificationRecord
 */
public enum NotificationType {
    WIN,
    LOSE,
    CO_ORGANIZER_ADDED
}