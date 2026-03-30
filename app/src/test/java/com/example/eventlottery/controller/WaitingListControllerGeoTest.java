package com.example.eventlottery.controller;

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
 * Unit tests for WaitingListController geolocation support.
 *
 * User stories covered:
 * - US 01.06.02: Sign up for an event from the event details
 * - US 02.02.02: Organizer sees on a map where entrants joined from
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class WaitingListControllerGeoTest {

    private FakeWaitListRepository fakeRepo;
    private WaitingListController controller;

    private static class FakeWaitListRepository implements WaitListRepository {
        private final List<WaitListRecord> records = new ArrayList<>();

        @Override
        public void addToWaitList(WaitListRecord r) {
            records.add(r);
        }

        @Override
        public void addToWaitList(WaitListRecord r, OperationCallback cb) {
            records.add(r);
            cb.onSuccess();
        }

        @Override
        public void removeFromWaitList(String eId, String dId) {
            records.removeIf(r -> r.getEventId().equals(eId) && r.getDeviceId().equals(dId));
        }

        @Override
        public void removeFromWaitList(String eId, String dId, OperationCallback cb) {
            removeFromWaitList(eId, dId);
            cb.onSuccess();
        }

        @Override
        public void updateStatus(String eId, String dId, WaitStatus s) {
            WaitListRecord record = getRecord(eId, dId);
            if (record != null) {
                record.setStatus(s);
            }
        }

        @Override
        public WaitListRecord getRecord(String eId, String dId) {
            return records.stream()
                    .filter(r -> r.getEventId().equals(eId) && r.getDeviceId().equals(dId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void getRecordAsync(String eId, String dId, SingleRecordCallback cb) {
            cb.onSuccess(getRecord(eId, dId));
        }

        @Override
        public List<WaitListRecord> getRecordsByEvent(String eId) {
            return records.stream()
                    .filter(r -> r.getEventId().equals(eId))
                    .collect(Collectors.toList());
        }

        @Override
        public void getRecordsByEventAsync(String eId, WaitListCallBack cb) {
            cb.onSuccess(new ArrayList<>(getRecordsByEvent(eId)));
        }

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

    @Test
    public void joinWaitingList_withGeoRecord_preservesLocationData() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setJoinLocation(53.5461, -113.4938, 10.0f, 123L);

        controller.joinWaitingList(record);

        WaitListRecord saved = fakeRepo.getRecord("event1", "device1");
        assertNotNull(saved);
        assertEquals(Double.valueOf(53.5461), saved.getJoinLatitude());
        assertEquals(Double.valueOf(-113.4938), saved.getJoinLongitude());
        assertEquals(Float.valueOf(10.0f), saved.getJoinAccuracyMeters());
        assertEquals(Long.valueOf(123L), saved.getJoinLocationTimestampMs());
        assertTrue(saved.hasJoinLocation());
    }

    @Test
    public void joinWaitingList_asyncWithGeoRecord_preservesLocationData() {
        WaitListRecord record = new WaitListRecord("event1", "device1");
        record.setJoinLocation(51.0447, -114.0719, 5.0f, 999L);

        final boolean[] successCalled = {false};

        controller.joinWaitingList(record, new WaitListRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                successCalled[0] = true;
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail");
            }
        });

        assertTrue(successCalled[0]);
        WaitListRecord saved = fakeRepo.getRecord("event1", "device1");
        assertNotNull(saved);
        assertEquals(Double.valueOf(51.0447), saved.getJoinLatitude());
        assertEquals(Double.valueOf(-114.0719), saved.getJoinLongitude());
    }

    @Test
    public void joinWaitingList_withIds_addsWaitingRecordWithoutLocation() {
        controller.joinWaitingList("event1", "device1");

        WaitListRecord saved = fakeRepo.getRecord("event1", "device1");
        assertNotNull(saved);
        assertEquals(WaitStatus.WAITING, saved.getStatus());
        assertFalse(saved.hasJoinLocation());
    }

    @Test
    public void getWaitingCount_countsOnlyWaitingEntrants() {
        WaitListRecord r1 = new WaitListRecord("event1", "u1");
        WaitListRecord r2 = new WaitListRecord("event1", "u2");
        WaitListRecord r3 = new WaitListRecord("event1", "u3");
        r3.setStatus(WaitStatus.INVITED);

        fakeRepo.addToWaitList(r1);
        fakeRepo.addToWaitList(r2);
        fakeRepo.addToWaitList(r3);

        final int[] count = {-1};

        controller.getWaitingCount("event1", new WaitingListController.CountCallback() {
            @Override
            public void onCount(int c) {
                count[0] = c;
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail");
            }
        });

        assertEquals(2, count[0]);
    }
}