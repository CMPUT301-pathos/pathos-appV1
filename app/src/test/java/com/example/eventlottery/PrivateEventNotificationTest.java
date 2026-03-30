package com.example.eventlottery;

import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for private event invitation notification logic.
 *
 * User stories covered:
 * - US 01.04.01: Receive notification when chosen from waiting list
 * - US 01.05.02: Accept the invitation to register for an event
 * - US 01.05.03: Decline an invitation when chosen
 * - US 02.07.02: Organizer invites specific entrants to a private event
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see WaitListRecord
 * @see EventSummary
 */
public class PrivateEventNotificationTest {

    private EventSummary makeEvent(boolean isPrivate) {
        long now = System.currentTimeMillis();
        EventSummary event = new EventSummary(
                "event1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports",
                now + 100000, now - 100000, now + 200000, 50, 10, null);
        event.setPrivate(isPrivate);
        return event;
    }

    @Test
    public void testPrivateInvite_recordHasInvitedStatus() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setStatus(WaitStatus.INVITED);
        assertEquals(WaitStatus.INVITED, record.getStatus());
    }

    @Test
    public void testPublicInvite_recordHasInvitedStatus() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setStatus(WaitStatus.INVITED);
        assertEquals(WaitStatus.INVITED, record.getStatus());
    }

    @Test
    public void testPrivateInvite_acceptChangesStatusToAccepted() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setStatus(WaitStatus.INVITED);
        record.acceptInvitation();
        assertEquals(WaitStatus.ACCEPTED, record.getStatus());
    }

    @Test
    public void testPrivateInvite_declineChangesStatusToDeclined() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        assertEquals(WaitStatus.DECLINED, record.getStatus());
    }

    @Test
    public void testPrivateEvent_isMarkedPrivate() {
        EventSummary event = makeEvent(true);
        assertTrue(event.isPrivate());
    }

    @Test
    public void testPublicEvent_isNotMarkedPrivate() {
        EventSummary event = makeEvent(false);
        assertFalse(event.isPrivate());
    }

    @Test
    public void testPrivateInvite_cannotAcceptIfNotInvited() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        // Status is WAITING by default
        assertThrows(IllegalStateException.class, record::acceptInvitation);
    }

    @Test
    public void testPrivateInvite_cannotDeclineIfNotInvited() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        assertThrows(IllegalStateException.class, record::declineInvitation);
    }

    @Test
    public void testPrivateInvite_multipleEntrantsCanBeInvited() {
        WaitListRecord r1 = new WaitListRecord("event1", "device1");
        WaitListRecord r2 = new WaitListRecord("event1", "device2");
        r1.setStatus(WaitStatus.INVITED);
        r2.setStatus(WaitStatus.INVITED);

        r1.acceptInvitation();
        r2.declineInvitation();

        assertEquals(WaitStatus.ACCEPTED, r1.getStatus());
        assertEquals(WaitStatus.DECLINED, r2.getStatus());
    }

    @Test
    public void testPrivateInvite_afterDecline_statusIsDeclined() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        assertNotEquals(WaitStatus.INVITED, record.getStatus());
        assertEquals(WaitStatus.DECLINED, record.getStatus());
    }
}
