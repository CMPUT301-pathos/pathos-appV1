package com.example.eventlottery.domain;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for entrant categorization by WaitStatus.
 *
 * Validates the logic used by OrganizerEventDetailFragment to sort
 * entrants into Waiting, Invited, Enrolled, and Cancelled sections.
 *
 * User stories tested:
 * - US 02.06.01: View a list of all chosen (invited) entrants
 * - US 02.06.02: View a list of all cancelled entrants
 * - US 02.06.03: View a final list of entrants who enrolled (accepted)
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class EntrantCategorizationTest {

    private List<WaitListRecord> allRecords;

    /**
     * Helper that mimics the fragment's categorization logic.
     * Groups records into four lists by status.
     */
    private List<WaitListRecord> filterByStatuses(List<WaitListRecord> records, WaitStatus... statuses) {
        List<WaitListRecord> result = new ArrayList<>();
        for (WaitListRecord record : records) {
            for (WaitStatus s : statuses) {
                if (record.getStatus() == s) {
                    result.add(record);
                    break;
                }
            }
        }
        return result;
    }

    @Before
    public void setUp() {
        allRecords = new ArrayList<>();

        // 2 waiting
        WaitListRecord r1 = new WaitListRecord("event1", "device1");
        WaitListRecord r2 = new WaitListRecord("event1", "device2");

        // 2 invited
        WaitListRecord r3 = new WaitListRecord("event1", "device3");
        r3.setStatus(WaitStatus.INVITED);
        WaitListRecord r4 = new WaitListRecord("event1", "device4");
        r4.setStatus(WaitStatus.INVITED);

        // 2 accepted (enrolled)
        WaitListRecord r5 = new WaitListRecord("event1", "device5");
        r5.setStatus(WaitStatus.ACCEPTED);
        WaitListRecord r6 = new WaitListRecord("event1", "device6");
        r6.setStatus(WaitStatus.ACCEPTED);

        // 1 cancelled, 1 declined
        WaitListRecord r7 = new WaitListRecord("event1", "device7");
        r7.setStatus(WaitStatus.CANCELLED);
        WaitListRecord r8 = new WaitListRecord("event1", "device8");
        r8.setStatus(WaitStatus.DECLINED);

        allRecords.add(r1);
        allRecords.add(r2);
        allRecords.add(r3);
        allRecords.add(r4);
        allRecords.add(r5);
        allRecords.add(r6);
        allRecords.add(r7);
        allRecords.add(r8);
    }

    @Test
    public void testWaitingSectionShowsOnlyWaitingEntrants() {
        List<WaitListRecord> waiting = filterByStatuses(allRecords, WaitStatus.WAITING);
        assertEquals(2, waiting.size());
        for (WaitListRecord r : waiting) {
            assertEquals(WaitStatus.WAITING, r.getStatus());
        }
    }

    @Test
    public void testInvitedSectionShowsOnlyInvitedEntrants() {
        List<WaitListRecord> invited = filterByStatuses(allRecords, WaitStatus.INVITED);
        assertEquals(2, invited.size());
        for (WaitListRecord r : invited) {
            assertEquals(WaitStatus.INVITED, r.getStatus());
        }
    }

    @Test
    public void testEnrolledSectionShowsOnlyAcceptedEntrants() {
        List<WaitListRecord> enrolled = filterByStatuses(allRecords, WaitStatus.ACCEPTED);
        assertEquals(2, enrolled.size());
        for (WaitListRecord r : enrolled) {
            assertEquals(WaitStatus.ACCEPTED, r.getStatus());
        }
    }

    @Test
    public void testCancelledSectionShowsCancelledAndDeclined() {
        List<WaitListRecord> cancelled = filterByStatuses(allRecords, WaitStatus.CANCELLED, WaitStatus.DECLINED);
        assertEquals(2, cancelled.size());
        for (WaitListRecord r : cancelled) {
            assertTrue(r.getStatus() == WaitStatus.CANCELLED || r.getStatus() == WaitStatus.DECLINED);
        }
    }

    @Test
    public void testAllEntrantsAreCategorized() {
        List<WaitListRecord> waiting = filterByStatuses(allRecords, WaitStatus.WAITING);
        List<WaitListRecord> invited = filterByStatuses(allRecords, WaitStatus.INVITED);
        List<WaitListRecord> enrolled = filterByStatuses(allRecords, WaitStatus.ACCEPTED);
        List<WaitListRecord> cancelled = filterByStatuses(allRecords, WaitStatus.CANCELLED, WaitStatus.DECLINED);

        int total = waiting.size() + invited.size() + enrolled.size() + cancelled.size();
        assertEquals(allRecords.size(), total);
    }

    @Test
    public void testEmptyListReturnsEmptySections() {
        List<WaitListRecord> empty = new ArrayList<>();
        assertEquals(0, filterByStatuses(empty, WaitStatus.WAITING).size());
        assertEquals(0, filterByStatuses(empty, WaitStatus.INVITED).size());
        assertEquals(0, filterByStatuses(empty, WaitStatus.ACCEPTED).size());
        assertEquals(0, filterByStatuses(empty, WaitStatus.CANCELLED, WaitStatus.DECLINED).size());
    }

    @Test
    public void testAcceptTransitionMovesFromInvitedToEnrolled() {
        WaitListRecord record = new WaitListRecord("event1", "device99");
        record.setStatus(WaitStatus.INVITED);

        record.acceptInvitation();

        assertEquals(WaitStatus.ACCEPTED, record.getStatus());
        // Should now appear in enrolled, not invited
        List<WaitListRecord> list = new ArrayList<>();
        list.add(record);
        assertEquals(0, filterByStatuses(list, WaitStatus.INVITED).size());
        assertEquals(1, filterByStatuses(list, WaitStatus.ACCEPTED).size());
    }

    @Test
    public void testDeclineTransitionMovesFromInvitedToCancelled() {
        WaitListRecord record = new WaitListRecord("event1", "device99");
        record.setStatus(WaitStatus.INVITED);

        record.declineInvitation();

        assertEquals(WaitStatus.DECLINED, record.getStatus());
        // Should now appear in cancelled section, not invited
        List<WaitListRecord> list = new ArrayList<>();
        list.add(record);
        assertEquals(0, filterByStatuses(list, WaitStatus.INVITED).size());
        assertEquals(1, filterByStatuses(list, WaitStatus.CANCELLED, WaitStatus.DECLINED).size());
    }

    @Test(expected = IllegalStateException.class)
    public void testAcceptFromWaitingThrowsException() {
        WaitListRecord record = new WaitListRecord("event1", "device99");
        record.acceptInvitation(); // should throw — status is WAITING, not INVITED
    }

    @Test(expected = IllegalStateException.class)
    public void testDeclineFromWaitingThrowsException() {
        WaitListRecord record = new WaitListRecord("event1", "device99");
        record.declineInvitation(); // should throw — status is WAITING, not INVITED
    }
}