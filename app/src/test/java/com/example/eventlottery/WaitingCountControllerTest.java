package com.example.eventlottery;

import static org.junit.Assert.*;

import com.example.eventlottery.controller.WaitingListController;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit tests for US 01.05.04 - Know how many entrants are on the waiting list.
 *
 * @author Edwin David
 * @version 1.1
 */
public class WaitingCountControllerTest {

    private FakeWaitListRepository fakeRepo;
    private WaitingListController controller;

    private static class FakeWaitListRepository implements WaitListRepository {
        private final List<WaitListRecord> records = new ArrayList<>();

        void addRecord(String eventId, String deviceId, WaitStatus status) {
            WaitListRecord record = new WaitListRecord(eventId, deviceId);
            record.setStatus(status);
            records.add(record);
        }

        @Override public void addToWaitList(WaitListRecord r) { records.add(r); }
        @Override public void addToWaitList(WaitListRecord r, OperationCallback cb) { records.add(r); cb.onSuccess(); }
        @Override public void removeFromWaitList(String eId, String dId) { records.removeIf(r -> r.getEventId().equals(eId) && r.getDeviceId().equals(dId)); }
        @Override public void removeFromWaitList(String eId, String dId, OperationCallback cb) { removeFromWaitList(eId, dId); cb.onSuccess(); }
        @Override public void updateStatus(String eId, String dId, WaitStatus s) {}
        @Override public WaitListRecord getRecord(String eId, String dId) { return null; }
        @Override public void getRecordAsync(String eId, String dId, SingleRecordCallback cb) { cb.onSuccess(null); }
        @Override public List<WaitListRecord> getRecordsByEvent(String eId) { return new ArrayList<>(); }
        @Override public void getRecordsByEventAsync(String eId, WaitListCallBack cb) { cb.onSuccess(new ArrayList<>()); }

        @Override
        public List<WaitListRecord> getRecordsByStatus(String eventId, WaitStatus status) {
            return records.stream()
                    .filter(r -> r.getEventId().equals(eventId) && r.getStatus() == status)
                    .collect(Collectors.toList());
        }

        @Override
        public void getRecordsByStatusAsync(String eventId, WaitStatus status, WaitListCallBack callback) {
            callback.onSuccess(new ArrayList<>(getRecordsByStatus(eventId, status)));
        }
    }

    @Before
    public void setUp() {
        fakeRepo = new FakeWaitListRepository();
        controller = new WaitingListController(fakeRepo);
    }

    // Testing US 01.05.04

    /**
     * Verifies that querying the waiting list count returns the correct number of entrants with WAITING status.
     * Tests US 01.05.04: Know how many entrants are on the waiting list.
     */
    @Test
    public void getWaitingCount_returnsCorrectCount() {

        // Checks when requesting the number of entrants with WAITING status
        // for an event, the controller returns the correct count of waiting entrants.

        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user3", WaitStatus.WAITING);

        controller.getWaitingCount("event1", new WaitingListController.CountCallback() {
            @Override public void onCount(int count) { assertEquals(3, count); }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });
    }

    /**
     * Verifies that waiting list count returns zero when no entrants exist with WAITING status.
     * Tests US 01.05.04: Know how many entrants are on the waiting list (edge case).
     */
    @Test
    public void getWaitingCount_emptyList_returnsZero() {

        // Checks if no entrants have WAITING status for an event, the controller returns  zero.

        controller.getWaitingCount("event1", new WaitingListController.CountCallback() {
            @Override public void onCount(int count) { assertEquals(0, count); }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });
    }
}
