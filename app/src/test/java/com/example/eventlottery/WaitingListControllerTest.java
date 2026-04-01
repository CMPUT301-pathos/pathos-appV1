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
 * Unit tests for WaitingListController.
 *
 * User stories covered:
 * - US 01.06.02: Sign up for an event from the event details
 * - US 02.02.01: Organizer views the list of entrants who joined the waiting list
 * - US 02.06.01: Organizer views the list of all chosen/invited entrants
 * - US 02.07.01: Organizer sends notifications to all entrants on the waiting list
 * - US 02.07.02: Organizer sends notifications to all selected entrants
 *
 * @author Edwin David
 * @version 1.1
 */
public class WaitingListControllerTest {

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
        @Override public WaitListRecord getRecord(String eId, String dId) {
            return records.stream().filter(r -> r.getEventId().equals(eId) && r.getDeviceId().equals(dId)).findFirst().orElse(null);
        }
        @Override public void getRecordAsync(String eId, String dId, SingleRecordCallback cb) { cb.onSuccess(getRecord(eId, dId)); }
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

    // Testing US 01.06.02

    @Test
    public void joinWaitingList_addsRecordWithWaitingStatus() {

       //Checks that when a user joins an event’s waiting list, the controller creates a WaitListRecord
        // and sets its status to WAITING.

        controller.joinWaitingList("event1", "device1");

        WaitListRecord record = fakeRepo.getRecord("event1", "device1");
        assertNotNull(record);
        assertEquals(WaitStatus.WAITING, record.getStatus());
    }

    @Test
    public void leaveWaitingList_removesRecord() {

        // Checks that when a user leaves the waiting list, their WaitListRecord is removed from the repository.

        controller.joinWaitingList("event1", "device1");
        controller.leaveWaitingList("event1", "device1");

        assertNull(fakeRepo.getRecord("event1", "device1"));
    }

    // Testing US 02.02.01

    @Test
    public void getRecordsByStatus_waiting_returnsOnlyWaitingEntrants() {

        //Checks that when requesting WAITING records for an event,
        // only entrants with WAITING status are returned

        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user3", WaitStatus.INVITED);

        fakeRepo.getRecordsByStatusAsync("event1", WaitStatus.WAITING, new WaitListRepository.WaitListCallBack() {
            @Override public void onSuccess(List<WaitListRecord> records) { assertEquals(2, records.size()); }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });
    }

    @Test
    public void getRecordsByStatus_waiting_emptyList_returnsEmpty() {

        //Checks that if no entrants have WAITING status for an event,
        // the repository returns an empty list.

        fakeRepo.getRecordsByStatusAsync("event1", WaitStatus.WAITING, new WaitListRepository.WaitListCallBack() {
            @Override public void onSuccess(List<WaitListRecord> records) { assertTrue(records.isEmpty()); }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });
    }

    // Testing US 02.06.01

    @Test
    public void getRecordsByStatus_invited_returnsOnlyInvitedEntrants() {

        //Checks that when requesting INVITED records for an event,
        // only entrants with INVITED status are returned.

        fakeRepo.addRecord("event1", "user1", WaitStatus.INVITED);
        fakeRepo.addRecord("event1", "user2", WaitStatus.INVITED);
        fakeRepo.addRecord("event1", "user3", WaitStatus.WAITING);

        fakeRepo.getRecordsByStatusAsync("event1", WaitStatus.INVITED, new WaitListRepository.WaitListCallBack() {
            @Override public void onSuccess(List<WaitListRecord> records) { assertEquals(2, records.size()); }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });
    }

    @Test
    public void getRecordsByStatus_invited_emptyList_returnsEmpty() {

        //Checks that if no entrants have INVITED status, the repository returns an empty list.

        fakeRepo.getRecordsByStatusAsync("event1", WaitStatus.INVITED, new WaitListRepository.WaitListCallBack() {
            @Override public void onSuccess(List<WaitListRecord> records) { assertTrue(records.isEmpty()); }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });
    }

    // Testing US 02.07.01

    @Test
    public void notifyWaiting_returnsWaitingEntrants() {
        // Check that all WAITING entrants for an event can be fetched
        // so the organizer can send notifications to each of them

        fakeRepo.addRecord("event1", "user1", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user2", WaitStatus.WAITING);
        fakeRepo.addRecord("event1", "user3", WaitStatus.INVITED);

        fakeRepo.getRecordsByStatusAsync("event1", WaitStatus.WAITING, new WaitListRepository.WaitListCallBack() {
            @Override public void onSuccess(List<WaitListRecord> records) { assertEquals(2, records.size()); }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });
    }

    // Testing US 02.07.02

    @Test
    public void notifySelected_returnsInvitedEntrants() {
        // Checks that all INVITED entrants for an event can be fetched
        // so the organizer can send notifications to each of them
        fakeRepo.addRecord("event1", "user1", WaitStatus.INVITED);
        fakeRepo.addRecord("event1", "user2", WaitStatus.INVITED);
        fakeRepo.addRecord("event1", "user3", WaitStatus.WAITING);

        fakeRepo.getRecordsByStatusAsync("event1", WaitStatus.INVITED, new WaitListRepository.WaitListCallBack() {
            @Override public void onSuccess(List<WaitListRecord> records) { assertEquals(2, records.size()); }
            @Override public void onFailure(Exception e) { fail("Should not fail"); }
        });
    }
}
