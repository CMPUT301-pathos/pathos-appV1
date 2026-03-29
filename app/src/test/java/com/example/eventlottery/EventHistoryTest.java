package com.example.eventlottery;

import com.example.eventlottery.domain.EventHistoryRecord;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for event history filtering and record logic.
 *
 * Verifies that history records are correctly created, filtered by status,
 * and contain the expected data for display in the EventHistoryFragment.
 *
 * User stories covered:
 * - US 01.02.03: View history of events registered for, selected or not
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see EventHistoryRecord
 * @see WaitListRecord
 */
public class EventHistoryTest {

    private WaitListRecord makeRecord(String eventId, WaitStatus status) {
        WaitListRecord record = new WaitListRecord(eventId, "device123");
        record.setStatus(status);
        return record;
    }

    private List<WaitListRecord> filterForHistory(List<WaitListRecord> records) {
        List<WaitListRecord> filtered = new ArrayList<>();
        for (WaitListRecord r : records) {
            if (r.getStatus() == WaitStatus.ACCEPTED
                    || r.getStatus() == WaitStatus.CANCELLED
                    || r.getStatus() == WaitStatus.DECLINED) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    @Test
    public void testHistoryRecord_storesFieldsCorrectly() {
        EventHistoryRecord record = new EventHistoryRecord(
                "event1", "Swimming Lessons", "Mar 15, 2026", "ACCEPTED");
        assertEquals("event1", record.getEventId());
        assertEquals("Swimming Lessons", record.getEventName());
        assertEquals("Mar 15, 2026", record.getEventDate());
        assertEquals("ACCEPTED", record.getStatus());
    }

    @Test
    public void testHistoryRecord_timestampSetOnCreation() {
        long before = System.currentTimeMillis();
        EventHistoryRecord record = new EventHistoryRecord(
                "event1", "Yoga Class", "Mar 10, 2026", "DECLINED");
        long after = System.currentTimeMillis();
        assertTrue(record.getTimestamp() >= before);
        assertTrue(record.getTimestamp() <= after);
    }

    @Test
    public void testHistoryFilter_acceptedStatusIncluded() {
        List<WaitListRecord> records = new ArrayList<>();
        records.add(makeRecord("event1", WaitStatus.ACCEPTED));
        List<WaitListRecord> filtered = filterForHistory(records);
        assertEquals(1, filtered.size());
        assertEquals(WaitStatus.ACCEPTED, filtered.get(0).getStatus());
    }

    @Test
    public void testHistoryFilter_cancelledStatusIncluded() {
        List<WaitListRecord> records = new ArrayList<>();
        records.add(makeRecord("event2", WaitStatus.CANCELLED));
        List<WaitListRecord> filtered = filterForHistory(records);
        assertEquals(1, filtered.size());
        assertEquals(WaitStatus.CANCELLED, filtered.get(0).getStatus());
    }

    @Test
    public void testHistoryFilter_declinedStatusIncluded() {
        List<WaitListRecord> records = new ArrayList<>();
        records.add(makeRecord("event3", WaitStatus.DECLINED));
        List<WaitListRecord> filtered = filterForHistory(records);
        assertEquals(1, filtered.size());
        assertEquals(WaitStatus.DECLINED, filtered.get(0).getStatus());
    }

    @Test
    public void testHistoryFilter_waitingStatusExcluded() {
        List<WaitListRecord> records = new ArrayList<>();
        records.add(makeRecord("event4", WaitStatus.WAITING));
        List<WaitListRecord> filtered = filterForHistory(records);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testHistoryFilter_invitedStatusExcluded() {
        List<WaitListRecord> records = new ArrayList<>();
        records.add(makeRecord("event5", WaitStatus.INVITED));
        List<WaitListRecord> filtered = filterForHistory(records);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testHistoryFilter_mixedRecords_onlyHistoryStatusesReturned() {
        List<WaitListRecord> records = new ArrayList<>();
        records.add(makeRecord("event1", WaitStatus.ACCEPTED));
        records.add(makeRecord("event2", WaitStatus.WAITING));
        records.add(makeRecord("event3", WaitStatus.DECLINED));
        records.add(makeRecord("event4", WaitStatus.INVITED));
        records.add(makeRecord("event5", WaitStatus.CANCELLED));

        List<WaitListRecord> filtered = filterForHistory(records);
        assertEquals(3, filtered.size());
    }

    @Test
    public void testHistoryFilter_emptyList_returnsEmpty() {
        List<WaitListRecord> records = new ArrayList<>();
        List<WaitListRecord> filtered = filterForHistory(records);
        assertTrue(filtered.isEmpty());
    }

    @Test
    public void testHistoryRecord_statusCanBeUpdated() {
        EventHistoryRecord record = new EventHistoryRecord(
                "event1", "Art Class", "Feb 20, 2026", "ACCEPTED");
        record.setStatus("CANCELLED");
        assertEquals("CANCELLED", record.getStatus());
    }
}
