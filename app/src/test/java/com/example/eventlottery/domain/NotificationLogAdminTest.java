package com.example.eventlottery.domain;


import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for NotificationLog model - Admin related functionality.
 * Covers US 03.08.01
 * @author hasratsinghchauhan
 * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 * Whta being tested ? Log getters/setters, constants, constructor, toString, map conversion
 */
@RunWith(JUnit4.class)
public class NotificationLogAdminTest {

    private NotificationLog log;
    private Timestamp now;

    @Before
    public void setUp() {
        log = new NotificationLog();
        now = Timestamp.now();
    }

    /**
     * Verifies that the NotificationLog default (no-argument) constructor
     * creates a valid, non-null instance.
     */
    @Test
    public void testEmptyConstructor() {
        assertNotNull(log);
    }

    /**
     * Verifies that setting and getting log ID works correctly.
     */
    @Test
    public void testLogId() {
        log.setLogId("log123");
        assertEquals("log123", log.getLogId());
    }

    /**
     * Verifies that setting and getting sender ID works correctly.
     */
    @Test
    public void testSenderId() {
        log.setSenderId("sender123");
        assertEquals("sender123", log.getSenderId());
    }

    /**
     * Verifies that setting and getting sender name works correctly.
     */
    @Test
    public void testSenderName() {
        log.setSenderName("John Organizer");
        assertEquals("John Organizer", log.getSenderName());
    }

    /**
     * Verifies that setting and getting recipient ID works correctly.
     */
    @Test
    public void testRecipientId() {
        log.setRecipientId("recipient123");
        assertEquals("recipient123", log.getRecipientId());
    }

    /**
     * Verifies that setting and getting recipient name works correctly.
     */
    @Test
    public void testRecipientName() {
        log.setRecipientName("Jane Entrant");
        assertEquals("Jane Entrant", log.getRecipientName());
    }

    /**
     * Verifies that setting and getting event ID works correctly.
     */
    @Test
    public void testEventId() {
        log.setEventId("event123");
        assertEquals("event123", log.getEventId());
    }

    /**
     * Verifies that setting and getting event name works correctly.
     */
    @Test
    public void testEventName() {
        log.setEventName("Swimming Lessons");
        assertEquals("Swimming Lessons", log.getEventName());
    }

    /**
     * Verifies that setting and getting notification type works correctly.
     */
    @Test
    public void testNotificationType() {
        log.setNotificationType(NotificationLog.TYPE_LOTTERY_WON);
        assertEquals(NotificationLog.TYPE_LOTTERY_WON, log.getNotificationType());
    }

    /**
     * Verifies that setting and getting notification title works correctly.
     */
    @Test
    public void testTitle() {
        log.setTitle("You won!");
        assertEquals("You won!", log.getTitle());
    }

    /**
     * Verifies that setting and getting notification message works correctly.
     */
    @Test
    public void testMessage() {
        log.setMessage("Congratulations!");
        assertEquals("Congratulations!", log.getMessage());
    }

    /**
     * Verifies that setting and getting timestamp works correctly.
     */
    @Test
    public void testTimestamp() {
        log.setTimestamp(now);
        assertEquals(now, log.getTimestamp());
    }

    /**
     * Verifies that setting and getting status works correctly.
     */
    @Test
    public void testStatus() {
        log.setStatus("delivered");
        assertEquals("delivered", log.getStatus());
    }

    /**
     * Verifies that the full parameterized NotificationLog constructor
     * correctly initializes all fields.
     */
    @Test
    public void testFullConstructor() {
        NotificationLog fullLog = new NotificationLog(
                "log123",              // logId
                "sender123",            // senderId
                "John Organizer",       // senderName
                "recipient123",         // recipientId
                "Jane Entrant",         // recipientName
                "event123",             // eventId
                "Swimming Lessons",     // eventName
                NotificationLog.TYPE_LOTTERY_WON, // notificationType
                "You won!",             // title
                "Congratulations!",     // message
                now,                    // timestamp
                "delivered"             // status
        );

        assertEquals("log123", fullLog.getLogId());
        assertEquals("sender123", fullLog.getSenderId());
        assertEquals("John Organizer", fullLog.getSenderName());
        assertEquals("recipient123", fullLog.getRecipientId());
        assertEquals("Jane Entrant", fullLog.getRecipientName());
        assertEquals("event123", fullLog.getEventId());
        assertEquals("Swimming Lessons", fullLog.getEventName());
        assertEquals(NotificationLog.TYPE_LOTTERY_WON, fullLog.getNotificationType());
        assertEquals("You won!", fullLog.getTitle());
        assertEquals("Congratulations!", fullLog.getMessage());
        assertEquals(now, fullLog.getTimestamp());
        assertEquals("delivered", fullLog.getStatus());
    }

    /**
     * Verifies that toString() includes all key notification fields
     * in the string representation.
     */
    @Test
    public void testToString() {
        log.setLogId("log123");
        log.setSenderName("John");
        log.setRecipientName("Jane");
        log.setEventName("Swimming");
        log.setNotificationType(NotificationLog.TYPE_LOTTERY_WON);
        log.setStatus("delivered");

        String result = log.toString();
        assertTrue(result.contains("log123"));
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Jane"));
        assertTrue(result.contains("Swimming"));
        assertTrue(result.contains(NotificationLog.TYPE_LOTTERY_WON));
        assertTrue(result.contains("delivered"));
    }

    /**
     * Verifies that all notification type constants are defined with
     * their correct string values.
     */
    @Test
    public void testNotificationTypeConstants() {
        assertEquals("lottery_won", NotificationLog.TYPE_LOTTERY_WON);
        assertEquals("lottery_lost", NotificationLog.TYPE_LOTTERY_LOST);
        assertEquals("invitation_sent", NotificationLog.TYPE_INVITATION_SENT);
        assertEquals("waitlist_joined", NotificationLog.TYPE_WAITLIST_JOINED);
        assertEquals("event_reminder", NotificationLog.TYPE_EVENT_REMINDER);
        assertEquals("organizer_message", NotificationLog.TYPE_ORGANIZER_MESSAGE);
        assertEquals("event_cancelled", NotificationLog.TYPE_EVENT_CANCELLED);
    }

    /**
     * Verifies that toMap() serializes all notification log properties
     * correctly for Firestore storage.
     */
    @Test
    public void testToMap() {
        log.setLogId("log123");
        log.setSenderId("sender123");
        log.setSenderName("John");
        log.setRecipientId("recipient123");
        log.setRecipientName("Jane");
        log.setEventId("event123");
        log.setEventName("Swimming");
        log.setNotificationType(NotificationLog.TYPE_LOTTERY_WON);
        log.setTitle("You won!");
        log.setMessage("Congratulations!");
        log.setTimestamp(now);
        log.setStatus("delivered");

        var map = log.toMap();

        assertEquals("sender123", map.get("senderId"));
        assertEquals("John", map.get("senderName"));
        assertEquals("recipient123", map.get("recipientId"));
        assertEquals("Jane", map.get("recipientName"));
        assertEquals("event123", map.get("eventId"));
        assertEquals("Swimming", map.get("eventName"));
        assertEquals(NotificationLog.TYPE_LOTTERY_WON, map.get("notificationType"));
        assertEquals("You won!", map.get("title"));
        assertEquals("Congratulations!", map.get("message"));
        assertEquals(now, map.get("timestamp"));
        assertEquals("delivered", map.get("status"));
    }
}
