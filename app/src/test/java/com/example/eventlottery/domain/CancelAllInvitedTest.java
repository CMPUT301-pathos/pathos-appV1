package com.example.eventlottery.domain;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for bulk cancellation of invited entrants.
 *
 * Validates the logic used by OrganizerEventDetailFragment's
 * "Cancel All Invited" feature.
 *
 * User story tested:
 * - US 02.06.04: Cancel entrants that did not sign up for the event
 *
 * @version 1.0
 */
public class CancelAllInvitedTest {

    private List<WaitListRecord> allRecords;

    /**
     * Helper that returns only records matching the given status.
     */
    private List<WaitListRecord> getByStatus(List<WaitListRecord> records, WaitStatus status) {
        List<WaitListRecord> result = new ArrayList<>();
        for (WaitListRecord r : records) {
            if (r.getStatus() == status) result.add(r);
        }
        return result;
    }

    /**
     * Mimics the bulk cancel logic: find all INVITED, set them to CANCELLED.
     * Returns the list of records that were cancelled.
     */
    private List<WaitListRecord> cancelAllInvited(List<WaitListRecord> records) {
        List<WaitListRecord> invited = getByStatus(records, WaitStatus.INVITED);
        for (WaitListRecord r : invited) {
            r.setStatus(WaitStatus.CANCELLED);
        }
        return invited;
    }

    @Before
    public void setUp() {
        allRecords = new ArrayList<>();

        // 2 waiting
        allRecords.add(createRecord("device1", WaitStatus.WAITING));
        allRecords.add(createRecord("device2", WaitStatus.WAITING));

        // 3 invited (these should get cancelled)
        allRecords.add(createRecord("device3", WaitStatus.INVITED));
        allRecords.add(createRecord("device4", WaitStatus.INVITED));
        allRecords.add(createRecord("device5", WaitStatus.INVITED));

        // 2 accepted
        allRecords.add(createRecord("device6", WaitStatus.ACCEPTED));
        allRecords.add(createRecord("device7", WaitStatus.ACCEPTED));

        // 1 already cancelled
        allRecords.add(createRecord("device8", WaitStatus.CANCELLED));
    }

    private WaitListRecord createRecord(String deviceId, WaitStatus status) {
        WaitListRecord r = new WaitListRecord("event1", deviceId);
        r.setStatus(status);
        return r;
    }

    /**
     * Verifies that bulk cancellation of invited entrants transitions
     * all INVITED records to CANCELLED status.
     */
    @Test
    public void testCancelAllInvitedChangesStatusToCancelled() {
        List<WaitListRecord> cancelled = cancelAllInvited(allRecords);

        assertEquals(3, cancelled.size());
        for (WaitListRecord r : cancelled) {
            assertEquals(WaitStatus.CANCELLED, r.getStatus());
        }
    }

    /**
     * Verifies that after bulk cancellation, no records remain with INVITED status.
     */
    @Test
    public void testCancelAllInvitedLeavesNoInvitedRemaining() {
        cancelAllInvited(allRecords);

        List<WaitListRecord> stillInvited = getByStatus(allRecords, WaitStatus.INVITED);
        assertEquals(0, stillInvited.size());
    }

    /**
     * Verifies that bulk cancellation of invited entrants does not affect
     * records with WAITING status.
     */
    @Test
    public void testCancelAllInvitedDoesNotAffectWaiting() {
        cancelAllInvited(allRecords);

        List<WaitListRecord> waiting = getByStatus(allRecords, WaitStatus.WAITING);
        assertEquals(2, waiting.size());
    }

    /**
     * Verifies that bulk cancellation of invited entrants does not affect
     * records with ACCEPTED status.
     */
    @Test
    public void testCancelAllInvitedDoesNotAffectAccepted() {
        cancelAllInvited(allRecords);

        List<WaitListRecord> accepted = getByStatus(allRecords, WaitStatus.ACCEPTED);
        assertEquals(2, accepted.size());
    }

    /**
     * Verifies that the total count of CANCELLED records increases by the number
     * of invited entrants cancelled (previously cancelled records are included).
     */
    @Test
    public void testCancelAllInvitedIncreasesTotalCancelledCount() {
        // 1 already cancelled before
        assertEquals(1, getByStatus(allRecords, WaitStatus.CANCELLED).size());

        cancelAllInvited(allRecords);

        // 1 original + 3 newly cancelled = 4
        assertEquals(4, getByStatus(allRecords, WaitStatus.CANCELLED).size());
    }

    /**
     * Verifies that bulk cancellation returns an empty list when there are
     * no INVITED records to cancel.
     */
    @Test
    public void testCancelAllInvitedWithNoInvitedReturnsEmpty() {
        // Remove all invited records
        List<WaitListRecord> noInvited = new ArrayList<>();
        noInvited.add(createRecord("device1", WaitStatus.WAITING));
        noInvited.add(createRecord("device2", WaitStatus.ACCEPTED));
        noInvited.add(createRecord("device3", WaitStatus.CANCELLED));

        List<WaitListRecord> cancelled = cancelAllInvited(noInvited);
        assertEquals(0, cancelled.size());
    }

    /**
     * Verifies that bulk cancellation of an empty list returns an empty result
     * without errors.
     */
    @Test
    public void testCancelAllInvitedWithEmptyList() {
        List<WaitListRecord> empty = new ArrayList<>();
        List<WaitListRecord> cancelled = cancelAllInvited(empty);
        assertEquals(0, cancelled.size());
    }

    /**
     * Verifies that bulk cancellation does not remove or add records,
     * only changes their status (total count remains the same).
     */
    @Test
    public void testTotalRecordCountUnchangedAfterCancelAll() {
        int before = allRecords.size();
        cancelAllInvited(allRecords);
        assertEquals(before, allRecords.size());
    }
}