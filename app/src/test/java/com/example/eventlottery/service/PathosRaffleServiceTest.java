package com.example.eventlottery.service;

import static org.junit.Assert.*;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit tests for {@link PathosRaffleService}.
 *
 * Uses a fake in-memory WaitListRepository to test lottery draw logic
 * without requiring Firestore.
 *
 * @author Dmitriy Limanets
 * @version 1.0
 */
public class PathosRaffleServiceTest {

    private FakeWaitListRepository fakeRepo;
    private PathosRaffleService raffleService;

    /**
     * Fake in-memory implementation of WaitListRepository for testing.
     */
    private static class FakeWaitListRepository implements WaitListRepository {
        private final List<WaitListRecord> records = new ArrayList<>();

        void addRecord(String eventId, String deviceId, WaitStatus status) {
            WaitListRecord record = new WaitListRecord(eventId, deviceId);
            record.setStatus(status);
            records.add(record);
        }

        @Override
        public void addToWaitList(WaitListRecord record) {
            records.add(record);
        }

        @Override
        public void addToWaitList(WaitListRecord r, OperationCallback callback) {
            records.add(r);
            callback.onSuccess();
        }

        @Override
        public void removeFromWaitList(String eventId, String deviceId) {
            records.removeIf(r -> r.getEventId().equals(eventId) && r.getDeviceId().equals(deviceId));
        }

        @Override
        public void removeFromWaitList(String eventId, String deviceId, OperationCallback callback) {
            records.removeIf(r -> r.getEventId().equals(eventId) && r.getDeviceId().equals(deviceId));
            callback.onSuccess();
        }

        @Override
        public void updateStatus(String eventId, String deviceId, WaitStatus newStatus) {
            for (WaitListRecord r : records) {
                if (r.getEventId().equals(eventId) && r.getDeviceId().equals(deviceId)) {
                    r.setStatus(newStatus);
                    return;
                }
            }
        }

        @Override
        public WaitListRecord getRecord(String eventId, String deviceId) {
            for (WaitListRecord r : records) {
                if (r.getEventId().equals(eventId) && r.getDeviceId().equals(deviceId)) {
                    return r;
                }
            }
            return null;
        }

        @Override
        public void getRecordAsync(String eventId, String deviceId, SingleRecordCallback callback) {
            callback.onSuccess(getRecord(eventId, deviceId));
        }

        @Override
        public List<WaitListRecord> getRecordsByEvent(String eventId) {
            return records.stream()
                    .filter(r -> r.getEventId().equals(eventId))
                    .collect(Collectors.toList());
        }

        @Override
        public List<WaitListRecord> getRecordsByStatus(String eventId, WaitStatus status) {
            return records.stream()
                    .filter(r -> r.getEventId().equals(eventId) && r.getStatus() == status)
                    .collect(Collectors.toList());
        }

        @Override
        public void getRecordsByStatusAsync(String eventId, WaitStatus status, WaitListCallBack callback) {
            List<WaitListRecord> filtered = getRecordsByStatus(eventId, status);
            callback.onSuccess(new ArrayList<>(filtered));
        }

        @Override
        public void getRecordsByEventAsync(String eventId, WaitListCallBack callback) {
            callback.onSuccess(records);
        }
    }

    @Before
    public void setUp() {
        fakeRepo = new FakeWaitListRepository();
        raffleService = new PathosRaffleService(fakeRepo);
    }

    // ========== drawInitial tests ==========

    /**
     * Verifies that drawInitial() selects exactly the requested number of winners
     * from the waiting pool.
     */
    @Test
    public void drawInitial_selectsCorrectCount() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user3", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user4", WaitStatus.WAITING);

        raffleService.drawInitial("event1", 2, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                assertEquals(2, selected.size());
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that drawInitial() transitions selected winners to INVITED status.
     */
    @Test
    public void drawInitial_updatesStatusToInvited() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.WAITING);

        raffleService.drawInitial("event1", 2, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                for (WaitListRecord record : selected) {
                    WaitListRecord updated = fakeRepo.getRecord(record.getEventId(), record.getDeviceId());
                    assertEquals(WaitStatus.INVITED, updated.getStatus());
                }
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that drawInitial() with a draw count larger than the waiting pool
     * size invites all available entrants.
     */
    @Test
    public void drawInitial_fewerEntrantsThanCount_invitesAll() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.WAITING);

        raffleService.drawInitial("event1", 10, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                assertEquals(2, selected.size());
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that drawInitial() with no waiting entrants returns an empty list.
     */
    @Test
    public void drawInitial_emptyPool_returnsEmptyList() {
        raffleService.drawInitial("event1", 5, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                assertTrue(selected.isEmpty());
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that drawInitial() only selects from WAITING entrants,
     * ignoring those with INVITED, ACCEPTED, or DECLINED status.
     */
    @Test
    public void drawInitial_ignoresNonWaitingEntrants() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.INVITED);
        fakeRepo.addRecord("event1", "user3", WaitStatus.ACCEPTED);
        fakeRepo.addRecord("event1", "user4", WaitStatus.DECLINED);

        raffleService.drawInitial("event1", 10, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                assertEquals(1, selected.size());
                assertEquals("user1", selected.get(0).getDeviceId());
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that drawInitial() for one event does not affect waiting entrants
     * in other events.
     */
    @Test
    public void drawInitial_onlyAffectsCorrectEvent() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event2", "user2", WaitStatus.WAITING);

        raffleService.drawInitial("event1", 10, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                assertEquals(1, selected.size());
                assertEquals("event1", selected.get(0).getEventId());
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });

        WaitListRecord event2Record = fakeRepo.getRecord("event2", "user2");
        assertEquals(WaitStatus.WAITING, event2Record.getStatus());
    }

    // ========== drawReplacement tests ==========

    /**
     * Verifies that drawReplacement() selects exactly one replacement from
     * the waiting pool.
     */
    @Test
    public void drawReplacement_selectsExactlyOne() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.WAITING);

        raffleService.drawReplacement("event1", new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                assertEquals(1, selected.size());
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that drawReplacement() with no waiting entrants returns an empty list.
     */
    @Test
    public void drawReplacement_emptyPool_returnsEmptyList() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.INVITED);
        fakeRepo.addRecord("event1", "user2", WaitStatus.DECLINED);

        raffleService.drawReplacement("event1", new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                assertTrue(selected.isEmpty());
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that drawReplacement() transitions the selected replacement
     * to INVITED status.
     */
    @Test
    public void drawReplacement_updatesReplacementToInvited() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);

        raffleService.drawReplacement("event1", new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                assertEquals(1, selected.size());
                WaitListRecord updated = fakeRepo.getRecord("event1", "user1");
                assertEquals(WaitStatus.INVITED, updated.getStatus());
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }
}