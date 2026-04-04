package com.example.eventlottery.domain;

import com.google.firebase.Timestamp;

/**
 * Model for policy violation records
 * Tracks comments deleted by admins for policy violations.
 * Supports US 03.10.01 - Track and audit removed comments.
 */
public class PolicyViolation {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private long deletedAt;
    private String deletedBy;
    private String reason;
    private String violationType;
    private String content;
    private String eventId;
    private String eventName;

    public PolicyViolation() {
        // Required for Firestore
    }

    public PolicyViolation(String userId, String userName, String userEmail,
                           String deletedBy, String reason) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.deletedAt = System.currentTimeMillis();
        this.deletedBy = deletedBy;
        this.reason = reason;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(long deletedAt) { this.deletedAt = deletedAt; }

    // For Firestore Timestamp compatibility
    public void setDeletedAt(Timestamp timestamp) {
        this.deletedAt = timestamp != null ? timestamp.toDate().getTime() : System.currentTimeMillis();
    }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
}