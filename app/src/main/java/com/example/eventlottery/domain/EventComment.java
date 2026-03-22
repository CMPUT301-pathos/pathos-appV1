package com.example.eventlottery.domain;

import kotlinx.serialization.Required;

/**
 * Domain model for comments posted on an event.
 *
 * User stories supported:
 * - US 01.08.01: Post a comment on an event
 * - US 01.08.02: View comments on an event
 *
 * @author Edwin David
 * @version 1.0
 */
public class EventComment {
    private String commentId;
    private String eventId;
    private String deviceId;
    private String authorName;
    private String text;
    private long createdAt;

    // Required for Firestore deserialization.
    public EventComment() {}

    public EventComment(String eventId, String deviceId, String authorName, String text) {
        this.eventId = eventId;
        this.deviceId = deviceId;
        this.authorName = authorName;
        this.text = text;
        this.createdAt = System.currentTimeMillis();
    }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
