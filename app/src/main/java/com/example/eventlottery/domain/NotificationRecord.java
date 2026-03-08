package com.example.eventlottery.domain;

import com.google.firebase.Timestamp;

/**
 * Domain model for an in-app notification.
 * Stored in Firestore via NotificationLogRepository implementations.
 */
public class NotificationRecord {

    public String id;
    public String recipientId;
    public String eventId;

    /** Stored as enum name, e.g. "WIN" */
    public String type;

    public String message;
    public Timestamp createdAt;
    public boolean read;

    /** Required empty constructor for Firestore. */
    public NotificationRecord() {
        // no-op
    }

    /** Convenience constructor used by PathosNotifyService. */
    public NotificationRecord(String recipientId, String eventId, NotificationType type, String message) {
        this.id = null;
        this.recipientId = recipientId;
        this.eventId = eventId;
        this.type = (type == null) ? null : type.name();
        this.message = message;
        this.createdAt = Timestamp.now();
        this.read = false;
    }
}