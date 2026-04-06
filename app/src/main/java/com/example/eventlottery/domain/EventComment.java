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

    /**
     * Empty constructor required for Firestore deserialization.
     */
    public EventComment() {
        // Required for Firestore
    }

    /**
     * Creates a new event comment with default metadata.
     *
     * @param eventId   event identifier
     * @param userId    author user identifier
     * @param userName  author display name
     * @param content   comment content text
     */
    public EventComment(String eventId, String userId, String userName, String content) {
        this.eventId = eventId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = Timestamp.now();
        this.isFlagged = false;
    }

    /**
     * Returns the Firestore document ID of the comment.
     *
     * @return comment ID
     */
    public String getId() { return id; }

    /**
     * Sets the Firestore document ID for this comment.
     *
     * @param id comment document ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the event ID that this comment belongs to.
     *
     * @return event identifier
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the event ID for this comment.
     *
     * @param eventId event identifier
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Returns the user ID of the comment author.
     *
     * @return author user identifier
     */
    public String getUserId() { return userId; }

    /**
     * Sets the user ID of the comment author.
     *
     * @param userId author user identifier
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * Returns the display name of the comment author.
     *
     * @return author display name
     */
    public String getUserName() { return userName; }

    /**
     * Sets the display name of the comment author.
     *
     * @param userName author display name
     */
    public void setUserName(String userName) { this.userName = userName; }

    /**
     * Returns the comment text.
     *
     * @return comment content
     */
    public String getContent() { return content; }

    /**
     * Sets the comment text.
     *
     * @param content comment content
     */
    public void setContent(String content) { this.content = content; }

    /**
     * Returns the timestamp when the comment was created.
     *
     * @return creation timestamp
     */
    public Timestamp getTimestamp() { return timestamp; }

    /**
     * Sets the timestamp for this comment.
     *
     * @param timestamp creation timestamp
     */
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    /**
     * Returns true if the comment has been flagged for review.
     *
     * @return true when flagged
     */
    public boolean isFlagged() { return isFlagged; }

    /**
     * Marks the comment as flagged or unflagged.
     *
     * @param flagged flag state
     */
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    /**
     * Returns the reason why the comment was flagged.
     *
     * @return flag reason text
     */
    public String getFlagReason() { return flagReason; }

    /**
     * Sets the reason why the comment was flagged.
     *
     * @param flagReason flag reason text
     */
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }
}
