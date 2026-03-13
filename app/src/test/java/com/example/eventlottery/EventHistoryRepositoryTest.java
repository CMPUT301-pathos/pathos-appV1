package com.example.eventlottery;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for US 01.02.03 (Event History).(NOT IMPLEMENTED)
 *
 * These tests should validate:
 * 1) history list returns records for a user/device
 * 2) empty history returns empty list (no crash)
 *
 * IMPORTANT: You must adapt names/types to your actual EventHistoryRepository / EventHistoryRecord classes.
 * Re-upload EventHistoryRecord + repository and I’ll make it compile exactly.
 */
public class EventHistoryRepositoryTest {

    static class FakeEventHistoryRepository {
        List<Object> records = new ArrayList<>();

        // TODO: replace Object with your EventHistoryRecord type
        List<Object> listHistoryForDevice(String deviceId) {
            return records;
        }
    }

    @Test
    public void listHistory_returnsRecords() {
        FakeEventHistoryRepository repo = new FakeEventHistoryRepository();
        repo.records.add(new Object());
        repo.records.add(new Object());

        List<Object> out = repo.listHistoryForDevice("device_123");
        assertEquals(2, out.size());
    }

    @Test
    public void listHistory_empty_returnsEmptyList() {
        FakeEventHistoryRepository repo = new FakeEventHistoryRepository();
        List<Object> out = repo.listHistoryForDevice("device_123");
        assertNotNull(out);
        assertEquals(0, out.size());
    }
}