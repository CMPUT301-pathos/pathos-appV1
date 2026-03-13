package com.example.eventlottery;

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
 * Unit tests for organizer removing entrants from the waitlist.
 *
 * User stories covered:
 * - US 02.06.03: Remove entrant from waiting list
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class OrganizerRemoveEntrantTest {

    private FakeRemoveWaitListRepository fakeRepo;

    private static class FakeRemoveWaitListRepository implements WaitListRepository {
        private final List<WaitListRecord> records = new ArrayList<>();

        void addRecord(String eventId, String deviceId, WaitStatus status) {
            WaitListRecord record = new WaitListRecord(eventId, deviceId);
            record.setStatus(status);
            records.add(record);
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
        public void addToWaitList(WaitListRecord r) { records.add(r); }

        @Override
        public void addToWaitList(WaitListRecord r, OperationCallback callback) {
            records.add(r);
            callback.onSuccess();
        }

        @Override
        public void removeFromWaitList(String eId, String dId) {
            records.removeIf(r -> r.getEventId().equals(eId) && r.getDeviceId().equals(dId));
        }

        @Override
        public void removeFromWaitList(String eId, String dId, OperationCallback callback) {
            records.removeIf(r -> r.getEventId().equals(eId) && r.getDeviceId().equals(dId));
            callback.onSuccess();
        }

        @Override
        public void getRecordAsync(String eId, String dId, SingleRecordCallback callback) {
            callback.onSuccess(getRecord(eId, dId));
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
            callback.onSuccess(new ArrayList<>(getRecordsByStatus(eventId, status)));
        }

        @Override
        public void getRecordsByEventAsync(String eventId, WaitListCallBack callback) {
            callback.onSuccess(new ArrayList<>(getRecordsByEvent(eventId)));
        }
    }

    @Before
    public void setUp() {
        fakeRepo = new FakeRemoveWaitListRepository();
    }

    @Test
    public void removeEntrant_setsStatusToCancelled() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.updateStatus("event1", "user1", WaitStatus.CANCELLED);
        WaitListRecord record = fakeRepo.getRecord("event1", "user1");
        assertEquals(WaitStatus.CANCELLED, record.getStatus());
    }

    @Test
    public void removeEntrant_doesNotAffectOtherEntrants() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.WAITING);
        fakeRepo.updateStatus("event1", "user1", WaitStatus.CANCELLED);
        WaitListRecord user2 = fakeRepo.getRecord("event1", "user2");
        assertEquals(WaitStatus.WAITING, user2.getStatus());
    }

    @Test
    public void removeEntrant_doesNotAffectOtherEvents() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event2", "user1", WaitStatus.WAITING);
        fakeRepo.updateStatus("event1", "user1", WaitStatus.CANCELLED);
        WaitListRecord event2Record = fakeRepo.getRecord("event2", "user1");
        assertEquals(WaitStatus.WAITING, event2Record.getStatus());
    }

    @Test
    public void removeInvitedEntrant_setsStatusToCancelled() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.INVITED);
        fakeRepo.updateStatus("event1", "user1", WaitStatus.CANCELLED);
        WaitListRecord record = fakeRepo.getRecord("event1", "user1");
        assertEquals(WaitStatus.CANCELLED, record.getStatus());
    }

    @Test
    public void cancelledEntrant_doesNotAppearInWaitingList() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.WAITING);
        fakeRepo.updateStatus("event1", "user1", WaitStatus.CANCELLED);
        List<WaitListRecord> waiting = fakeRepo.getRecordsByStatus("event1", WaitStatus.WAITING);
        assertEquals(1, waiting.size());
        assertEquals("user2", waiting.get(0).getDeviceId());
    }

    @Test
    public void cancelledEntrant_appearsInCancelledList() {
        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.updateStatus("event1", "user1", WaitStatus.CANCELLED);
        List<WaitListRecord> cancelled = fakeRepo.getRecordsByStatus("event1", WaitStatus.CANCELLED);
        assertEquals(1, cancelled.size());
        assertEquals("user1", cancelled.get(0).getDeviceId());
    }
}
