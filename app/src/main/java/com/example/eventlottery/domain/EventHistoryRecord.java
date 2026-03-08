package com.example.eventlottery.domain;

/**
 * Represents an event history record for a user in the application.
 * Tracks events that a user has registered for, along with their selection status.
 * Used to display event history to entrants (US 01.02.03).
 *
 * @author Hasrat Singh Chauhan
 * @version 1.0
 * @since 1.0
 * @see UserProfile
 */
public class EventHistoryRecord {
    private String deviceId;

    /**
     * Unique identifier for the event.
     * Matches the event ID in the Firestore events collection.
     */
    private String eventId;

    /**
     * Display name of the event.
     * Example: "Swimming Lessons for Beginners"
     */
    private String eventName;

    /**
     * Date when the event takes place or took place.
     * Stored as string for flexible formatting.
     */
    private String eventDate;

    /**
     * Current status of the user's participation in this event.
     * Possible values:
     * - "WAITING"      : User is on the waiting list
     * - "SELECTED"     : User was selected in lottery (won)
     * - "NOT_SELECTED" : User was not selected in lottery (lost)
     * - "ACCEPTED"     : User accepted the invitation after being selected
     * - "DECLINED"     : User declined the invitation after being selected
     * - "CANCELLED"    : User was removed/cancelled from event
     */
    private String status;

    /**
     * Timestamp when this history record was created.
     * Used for sorting history chronologically (newest first).
     * Stored as milliseconds since epoch.
     */
    private long timestamp;

    /**
     * Empty constructor required by Firestore for deserialization.
     * Do not use for creating new history records directly.
     */
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public EventHistoryRecord(String deviceId, String number, String swimmingLessons, String s, String selected) {}

    /**
     * Constructs a new event history record.
     * Automatically sets the timestamp to current system time.
     * Used to track a user's interaction with an event.
     *
     * @param eventId   The unique identifier of the event
     * @param eventName The display name of the event
     * @param eventDate The date of the event
     * @param status    The user's status for this event (WAITING, SELECTED, etc.)
     */
    public EventHistoryRecord(String eventId, String eventName, String eventDate, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Returns the event ID.
     *
     * @return The unique identifier of the event
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the event ID.
     *
     * @param eventId The unique identifier of the event
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Returns the event name.
     *
     * @return The display name of the event
     */
    public String getEventName() { return eventName; }

    /**
     * Sets the event name.
     *
     * @param eventName The display name of the event
     */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /**
     * Returns the event date.
     *
     * @return The date of the event as a string
     */
    public String getEventDate() { return eventDate; }

    /**
     * Sets the event date.
     *
     * @param eventDate The date of the event
     */
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    /**
     * Returns the user's current status for this event.
     *
     * @return The status string (WAITING, SELECTED, etc.)
     */
    public String getStatus() { return status; }

    /**
     * Sets the user's status for this event.
     *
     * @param status The status to set (WAITING, SELECTED, etc.)
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns the timestamp when this record was created.
     *
     * @return The creation timestamp in milliseconds since epoch
     */
    public long getTimestamp() { return timestamp; }

    /**
     * Sets the timestamp for this record.
     * Used when loading from Firestore.
     *
     * @param timestamp The timestamp in milliseconds since epoch
     */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}