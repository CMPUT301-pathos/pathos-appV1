package com.example.eventlottery.domain;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * Audit log for all notifications sent in the system.
 *
 * This model captures detailed information about every notification sent to users,
 * including who sent it, who received it, which event it relates to, and the delivery status.
 * These logs are used by administrators to review notification history, troubleshoot issues,
 * and ensure compliance with platform policies.
 *
 * User Stories Supported:
 *
 * US 03.08.01 - As an administrator, I want to review logs of all notifications
 *         sent to entrants by organizers.
 *
 *
 * NOTE: I also didnt want to mess with a file that someone elese had created.
 * Unlike {link NotificationRecord} which is used for in-app display to users,
 * this class is designed specifically for administrative auditing and persists
 * additional metadata such as sender information and delivery status.
 *
 * @author Hasrat Singh Chauhan
 * @version 1.0
 * @see NotificationRecord
 */
public class NotificationLog {

    public static final String TYPE_LOTTERY_WON = "lottery_won";
    public static final String TYPE_LOTTERY_LOST = "lottery_lost";
    public static final String TYPE_EVENT_REMINDER = "event_reminder";
    public static final String TYPE_ORGANIZER_MESSAGE = "organizer_message";
    public static final String TYPE_INVITATION_SENT = "invitation_sent";
    public static final String TYPE_WAITLIST_JOINED = "waitlist_joined";
    public static final String TYPE_INVITATION_DECLINED = "invitation_declined";
    public static final String TYPE_EVENT_CANCELLED = "event_cancelled";

    private String logId;
    private String senderId;
    private String senderName;
    private String recipientId;
    private String recipientName;
    private String eventId;
    private String eventName;
    private String notificationType; // "WIN", "LOST", "INVITE", "WAITLIST_JOINED", etc.
    private String title;
    private String message;
    private Timestamp timestamp;
    private String status; // "sent", "delivered", "failed"

    /**
     * Empty constructor required for Firestore deserialization.
     * Do not remove or modify.
     */
    public NotificationLog() {
        // Required for Firestore
    }

    /**
     * Full constructor for creating a new notification log entry.
     *
     * @param logId            Unique identifier for this log entry
     * @param senderId         ID of the user who sent the notification
     * @param senderName       Display name of the sender
     * @param recipientId      ID of the user who received the notification
     * @param recipientName    Display name of the recipient
     * @param eventId          ID of the related event (may be null)
     * @param eventName        Name of the related event (may be null)
     * @param notificationType Type/category of notification
     * @param title            Notification title
     * @param message          Notification message content
     * @param timestamp        When the notification was sent
     * @param status           Delivery status
     */
    public NotificationLog(String logId, String senderId, String senderName,
                           String recipientId, String recipientName,
                           String eventId, String eventName,
                           String notificationType, String title,
                           String message, Timestamp timestamp, String status) {
        this.logId = logId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    /**
     * Converts this NotificationLog object to a Map suitable for Firestore storage.
     *
     * @return A Map containing all non-null fields of this log entry
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("senderId", senderId);
        map.put("senderName", senderName);
        map.put("recipientId", recipientId);
        map.put("recipientName", recipientName);
        map.put("eventId", eventId);
        map.put("eventName", eventName);
        map.put("notificationType", notificationType);
        map.put("title", title);
        map.put("message", message);
        map.put("timestamp", timestamp);
        map.put("status", status);
        return map;
    }

    // ================ Getters and Setters ================

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "NotificationLog{" +
                "logId='" + logId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", recipientName='" + recipientName + '\'' +
                ", eventName='" + eventName + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}