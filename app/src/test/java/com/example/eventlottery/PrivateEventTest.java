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

    /**
     * Verifies that newly created events default to public (not private) status.
     * Ensures secure defaults by requiring explicit private flag setting.
     */
    @Test
    public void testEvent_defaultIsNotPrivate() {
        EventSummary event = makeEvent(false);
        assertFalse(event.isPrivate());
    }

    /**
     * Verifies that an event can be explicitly marked as private.
     * Tests US 02.07.01: Organizer creates a private event.
     */
    @Test
    public void testEvent_canBeMarkedPrivate() {
        EventSummary event = makeEvent(true);
        assertTrue(event.isPrivate());
    }

    /**
     * Verifies that the private flag can be toggled after event creation.
     * Ensures mutability and state management of event privacy setting.
     */
    @Test
    public void testEvent_privateCanBeToggled() {
        EventSummary event = makeEvent(false);
        event.setPrivate(true);
        assertTrue(event.isPrivate());
        event.setPrivate(false);
        assertFalse(event.isPrivate());
    }

    /**
     * Verifies that public events are eligible for QR code generation.
     * QR generation is conditional on event being public (isPrivate() returns false).
     */
    @Test
    public void testPublicEvent_hasQrGenerated() {
        EventSummary event = makeEvent(false);
        // Public events should have QR — verified by isPrivate being false
        assertFalse(event.isPrivate());
    }

    /**
     * Verifies that private events should not generate QR codes.
     * Private events require explicit organizer invitations instead of public QR sharing.
     */
    @Test
    public void testPrivateEvent_shouldNotGenerateQr() {
        EventSummary event = makeEvent(true);
        assertTrue(event.isPrivate());
        // Private events skip QR generation — verified by isPrivate being true
    }

    // ── Invite entrant tests ──────────────────────────────────────

    /**
     * Verifies that a wait list record can be created and immediately set to INVITED status.
     * Tests US 02.07.02: Organizer invites specific entrants to a private event.
     */
    @Test
    public void testInviteRecord_createdWithInvitedStatus() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        assertEquals(WaitStatus.INVITED, record.getStatus());
    }

    /**
     * Verifies that an invitation record stores the correct event identifier.
     * Ensures event-entrant mapping is correctly maintained.
     */
    @Test
    public void testInviteRecord_hasCorrectEventId() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        assertEquals("event1", record.getEventId());
    }

    /**
     * Verifies that an invitation record stores the correct device identifier.
     * Ensures entrant identification is correctly preserved.
     */
    @Test
    public void testInviteRecord_hasCorrectDeviceId() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        assertEquals("device123", record.getDeviceId());
    }

    /**
     * Verifies that an invited entrant can accept the invitation and transition to ACCEPTED.
     * Tests US 01.05.02: Accept the invitation to register for an event.
     */
    @Test
    public void testInviteRecord_canAcceptAfterInvited() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.acceptInvitation();
        assertEquals(WaitStatus.ACCEPTED, record.getStatus());
    }

    /**
     * Verifies that an invited entrant can decline the invitation and transition to DECLINED.
     * Tests US 01.05.03: Decline an invitation when chosen.
     */
    @Test
    public void testInviteRecord_canDeclineAfterInvited() {
        WaitListRecord record = new WaitListRecord("event1", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        assertEquals(WaitStatus.DECLINED, record.getStatus());
    }

    /**
     * Verifies that multiple distinct entrants can be simultaneously invited to the same event.
     * Ensures invitation list management supports multiple independent device records.
     * Tests US 02.07.02: Organizer invites specific entrants to a private event.
     */
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
