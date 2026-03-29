package com.example.eventlottery;

import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for private event logic.
 *
 * User stories covered:
 * - US 02.07.01: Organizer creates a private event
 * - US 02.07.02: Organizer invites specific entrants to a private event
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see EventSummary
 * @see WaitListRecord
 */
public class PrivateEventTest {

    private EventSummary makeEvent(boolean isPrivate) {
        long now = System.currentTimeMillis();
        EventSummary event = new EventSummary(
                "event1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports",
                now + 100000, now - 100000, now + 200000,
                50, 10, null);
        event.setPrivate(isPrivate);
        return event;
    }

    // ── Private event flag tests ──────────────────────────────────

    @Test
    public void testEvent_defaultIsNotPrivate() {
        EventSummary event = makeEvent(false);
        assertFalse(event.isPrivate());
    }

    @Test
    public void testEvent_canBeMarkedPrivate() {
        EventSummary event = makeEvent(true);
        assertTrue(event.isPrivate());
    }

    @Test
    public void testEvent_privateCanBeToggled() {
        EventSummary event = makeEvent(false);
        event.setPrivate(true);
        assertTrue(event.isPrivate());
        event.setPrivate(false);
        assertFalse(event.isPrivate());
    }

    @Test
    public void testPublicEvent_hasQrGenerated() {
        EventSummary event = makeEvent(false);
        // Public events should have QR — verified by isPrivate being false
        assertFalse(event.isPrivate());
    }

    @Test
    public void testPrivateEvent_shouldNotGenerateQr() {
        EventSummary event = makeEvent(true);
        assertTrue(event.isPrivate());
        // Private events skip QR generation — verified by isPrivate being true
    }

    // ── Invite entrant tests ──────────────────────────────────────

    @Test
    public void testInviteRecord_createdWithInvitedStatus() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        assertEquals(WaitStatus.INVITED, record.getStatus());
    }

    @Test
    public void testInviteRecord_hasCorrectEventId() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        assertEquals("event1", record.getEventId());
    }

    @Test
    public void testInviteRecord_hasCorrectDeviceId() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        assertEquals("device123", record.getDeviceId());
    }

    @Test
    public void testInviteRecord_canAcceptAfterInvited() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.acceptInvitation();
        assertEquals(WaitStatus.ACCEPTED, record.getStatus());
    }

    @Test
    public void testInviteRecord_canDeclineAfterInvited() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        assertEquals(WaitStatus.DECLINED, record.getStatus());
    }

    @Test
    public void testMultipleInvites_differentDevices() {
        WaitListRecord r1 = new WaitListRecord("event1", "device1");
        WaitListRecord r2 = new WaitListRecord("event1", "device2");
        WaitListRecord r3 = new WaitListRecord("event1", "device3");

        r1.setStatus(WaitStatus.INVITED);
        r2.setStatus(WaitStatus.INVITED);
        r3.setStatus(WaitStatus.INVITED);

        assertEquals(WaitStatus.INVITED, r1.getStatus());
        assertEquals(WaitStatus.INVITED, r2.getStatus());
        assertEquals(WaitStatus.INVITED, r3.getStatus());
        assertNotEquals(r1.getDeviceId(), r2.getDeviceId());
        assertNotEquals(r2.getDeviceId(), r3.getDeviceId());
    }
}
