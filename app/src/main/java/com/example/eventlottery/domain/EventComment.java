package com.example.eventlottery.domain;

import com.google.firebase.Timestamp;

/**
 * Model class for event comments.
 * Supports US 03.10.01 - Remove event comments that violate app policy.
 */
public class EventComment {
    private String id;
    private String eventId;
    private String userId;
    private String userName;
    private String content;
    private Timestamp timestamp;
    private boolean isFlagged;
    private String flagReason;

    public EventComment() {
        // Required for Firestore
    }

    public EventComment(String eventId, String userId, String userName, String content) {
        this.eventId = eventId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = Timestamp.now();
        this.isFlagged = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }
}
