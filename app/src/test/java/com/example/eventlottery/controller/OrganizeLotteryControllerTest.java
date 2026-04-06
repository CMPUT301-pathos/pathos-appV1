package com.example.eventlottery.controller;

import static org.junit.Assert.*;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.domain.NotificationType;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.service.PathosNotifyService;
import com.example.eventlottery.service.PathosRaffleService;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for US 02.05.01:
 * "As an organizer I want to send a notification to chosen entrants to sign up for events."
 *
 * Verifies that running a lottery draw triggers WIN notifications
 * for all selected entrants.
 *
 * @version 1.0
 * @author Dmitriy Limanets
 */
public class OrganizeLotteryControllerTest {

    private FakeWaitListRepository waitListRepo;
    private FakeNotificationLogRepository notificationRepo;
    private OrganizeLotteryController controller;

    // ── Fakes ────────────────────────────────────────────────────

    static class FakeWaitListRepository implements WaitListRepository {
        final List<WaitListRecord> records = new ArrayList<>();

        void addRecord(String eventId, String deviceId, WaitStatus status) {
            WaitListRecord r = new WaitListRecord(eventId, deviceId);
            r.setStatus(status);
            records.add(r);
        }

        @Override
        public void addToWaitList(WaitListRecord record) {
            records.add(record);
        }

        @Override
        public void addToWaitList(WaitListRecord record, OperationCallback callback) {
            records.add(record);
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
                }
            }
        }

        @Override
        public void getRecordsByEventAsync(String eventId, WaitListCallBack callback) {
            List<WaitListRecord> matched = new ArrayList<>();
            for (WaitListRecord r : records) {
                if (r.getEventId().equals(eventId)) matched.add(r);
            }
            callback.onSuccess(matched);
        }

        @Override
        public void getRecordsByStatusAsync(String eventId, WaitStatus status, WaitListCallBack callback) {
            List<WaitListRecord> matched = new ArrayList<>();
            for (WaitListRecord r : records) {
                if (r.getEventId().equals(eventId) && r.getStatus() == status) {
                    matched.add(r);
                }
            }
            callback.onSuccess(matched);
        }

        @Override
        public WaitListRecord getRecord(String eventId, String deviceId) {
            for (WaitListRecord r : records) {
                if (r.getEventId().equals(eventId) && r.getDeviceId().equals(deviceId)) return r;
            }
            return null;
        }

        @Override
        public void getRecordAsync(String eventId, String deviceId, SingleRecordCallback callback) {
            WaitListRecord found = getRecord(eventId, deviceId);
            callback.onSuccess(found);
        }

        @Override
        public List<WaitListRecord> getRecordsByEvent(String eventId) {
            List<WaitListRecord> matched = new ArrayList<>();
            for (WaitListRecord r : records) {
                if (r.getEventId().equals(eventId)) matched.add(r);
            }
            return matched;
        }

        @Override
        public List<WaitListRecord> getRecordsByStatus(String eventId, WaitStatus status) {
            List<WaitListRecord> matched = new ArrayList<>();
            for (WaitListRecord r : records) {
                if (r.getEventId().equals(eventId) && r.getStatus() == status) matched.add(r);
            }
            return matched;
        }
    }

    static class FakeNotificationLogRepository implements NotificationLogRepository {
        final List<NotificationRecord> added = new ArrayList<>();

        @Override
        public Task<Void> add(NotificationRecord record) {
            added.add(record);
            return Tasks.forResult(null);
        }

        @Override
        public Task<List<NotificationRecord>> listForUser(String userId, int limit) {
            return Tasks.forResult(new ArrayList<>());
        }
    }

    // ── Setup ────────────────────────────────────────────────────

    @Before
    public void setUp() {
        waitListRepo = new FakeWaitListRepository();
        notificationRepo = new FakeNotificationLogRepository();
        PathosRaffleService raffleService = new PathosRaffleService(waitListRepo);
        PathosNotifyService notifyService = new PathosNotifyService(notificationRepo);
        controller = new OrganizeLotteryController(raffleService, notifyService);
    }

    // ── Tests ────────────────────────────────────────────────────

    /**
     * Verifies that running a lottery draw creates one WIN notification
     * for each entrant selected in the draw.
     *
     * Given 3 waiting entrants with a draw count of 2, expects 2 notifications.
     */
    @Test
    public void testDrawSendsNotificationToEachChosenEntrant() {
        waitListRepo.addRecord("event1", "device1", WaitStatus.WAITING);
        waitListRepo.addRecord("event1", "device2", WaitStatus.WAITING);
        waitListRepo.addRecord("event1", "device3", WaitStatus.WAITING);

        final List<WaitListRecord> result = new ArrayList<>();

        controller.runInitialDraw("event1", "Swimming Lessons", 2,
                new OrganizeLotteryController.LotteryCallback() {
                    @Override
                    public void onSuccess(List<WaitListRecord> selected) {
                        result.addAll(selected);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Should not fail");
                    }
                });

        assertEquals(2, result.size());
        assertEquals(2, notificationRepo.added.size());
    }

    /**
     * Verifies that notifications sent to lottery winners have type WIN.
     */
    @Test
    public void testNotificationTypeIsWin() {
        waitListRepo.addRecord("event1", "device1", WaitStatus.WAITING);

        controller.runInitialDraw("event1", "Piano Lessons", 1,
                new OrganizeLotteryController.LotteryCallback() {
                    @Override
                    public void onSuccess(List<WaitListRecord> selected) { }

                    @Override
                    public void onFailure(Exception e) { fail("Should not fail"); }
                });

        assertEquals(1, notificationRepo.added.size());
        assertEquals(NotificationType.WIN.name(), notificationRepo.added.get(0).type);
    }

    /**
     * Verifies that lottery win notifications include the event name in the message.
     */
    @Test
    public void testNotificationContainsEventName() {
        waitListRepo.addRecord("event1", "device1", WaitStatus.WAITING);

        controller.runInitialDraw("event1", "Dance Class", 1,
                new OrganizeLotteryController.LotteryCallback() {
                    @Override
                    public void onSuccess(List<WaitListRecord> selected) { }

                    @Override
                    public void onFailure(Exception e) { fail("Should not fail"); }
                });

        assertTrue(notificationRepo.added.get(0).message.contains("Dance Class"));
    }

    /**
     * Verifies that notifications are sent to the correct recipient device ID.
     */
    @Test
    public void testNotificationSentToCorrectRecipient() {
        waitListRepo.addRecord("event1", "device_abc", WaitStatus.WAITING);

        controller.runInitialDraw("event1", "Yoga", 1,
                new OrganizeLotteryController.LotteryCallback() {
                    @Override
                    public void onSuccess(List<WaitListRecord> selected) { }

                    @Override
                    public void onFailure(Exception e) { fail("Should not fail"); }
                });

        assertEquals("device_abc", notificationRepo.added.get(0).recipientId);
    }

    /**
     * Verifies that no notifications are created when drawing from an event
     * with no waiting entrants.
     */
    @Test
    public void testNoNotificationWhenNoEntrants() {
        controller.runInitialDraw("event1", "Empty Event", 5,
                new OrganizeLotteryController.LotteryCallback() {
                    @Override
                    public void onSuccess(List<WaitListRecord> selected) {
                        assertTrue(selected.isEmpty());
                    }

                    @Override
                    public void onFailure(Exception e) { fail("Should not fail"); }
                });

        assertEquals(0, notificationRepo.added.size());
    }
}
