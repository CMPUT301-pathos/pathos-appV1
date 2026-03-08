package com.example.eventlottery.domain;

import java.security.Timestamp;

// This represents, a notification record in the database
public class NotificationRecord {
    public String id;
    public String recipientId;
    public String eventId;
    public String type; // store enum name
    public String message;
    public Timestamp createdAt;
    public boolean read;
    public NotificationRecord(String recipientId, String eventId, NotificationType win, String message) {} // Firestore
    public NotificationRecord(String id, String recipientId, String eventId, String type, String message, Timestamp createdAt, boolean read) {
        this.id = id;
        this.recipientId = recipientId;
        this.eventId = eventId;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
        this.read = false; // Set to false by default
    }

}
