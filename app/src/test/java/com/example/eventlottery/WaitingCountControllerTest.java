package com.example.eventlottery;

import static org.junit.Assert.*;

import com.example.eventlottery.controller.WaitingListController;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for US 01.05.04 - Know how many entrants are on the waiting list.
 *
 * @author Edwin
 * @version 1.0
 */
public class WaitingCountControllerTest {

    // ---- Fake repository (no Firebase, callbacks fired synchronously) ----

    static class FakeWaitListRepository implements WaitListRepository {

        private final List<WaitListRecord> records;

        FakeWaitListRepository(List<WaitListRecord> records) {
            this.records = records;
        }

        @Override
        public void getRecordsByStatusAsync(String eventId, WaitStatus status, WaitListCallBack callback) {
            callback.onSuccess(records);
        }

        @Override public void addToWaitList(WaitListRecord r) {}
        @Override public void removeFromWaitList(String eId, String dId) {}
        @Override public void updateStatus(String eId, String dId, WaitStatus s) {}
        @Override public WaitListRecord getRecord(String eId, String dId) { return null; }
        @Override public List<WaitListRecord> getRecordsByEvent(String eId) { return new ArrayList<>(); }
        @Override public List<WaitListRecord> getRecordsByStatus(String eId, WaitStatus s) { return new ArrayList<>(); }
    }

    // ---- Tests ----

    @Test
    public void getWaitingCount_returnsCorrectCount() {
        List<WaitListRecord> fakeRecords = Arrays.asList(
                new WaitListRecord("event1", "device1"),
                new WaitListRecord("event1", "device2"),
                new WaitListRecord("event1", "device3")
        );

        WaitingListController controller =
                new WaitingListController(new FakeWaitListRepository(fakeRecords));

        int[] result = {-1};
        controller.getWaitingCount("event1", new WaitingListController.CountCallback() {
            @Override public void onCount(int count) { result[0] = count; }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });

        assertEquals(3, result[0]);
    }

    @Test
    public void getWaitingCount_emptyList_returnsZero() {
        WaitingListController controller =
                new WaitingListController(new FakeWaitListRepository(new ArrayList<>()));

        int[] result = {-1};
        controller.getWaitingCount("event1", new WaitingListController.CountCallback() {
            @Override public void onCount(int count) { result[0] = count; }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });

        assertEquals(0, result[0]);
    }

}
