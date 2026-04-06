package com.example.eventlottery.service;

import static org.junit.Assert.*;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.domain.NotificationRecord;
import com.example.eventlottery.domain.NotificationType;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PathosNotifyServiceTest {

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

    /**
     * Verifies that notifyWin() creates a WIN-type notification record with
     * the correct recipient, event, message, and initial unread status.
     */
    @Test
    public void notifyWin_addsWinNotificationRecord() {
        FakeNotificationLogRepository repo = new FakeNotificationLogRepository();
        PathosNotifyService service = new PathosNotifyService(repo);

        String recipientId = "entrant_123";
        String eventId = "event_swim_001";
        String message = "You have won the lottery for this event!";

        Task<Void> t = service.notifyWin(recipientId, eventId, message);

        assertNotNull(t);
        assertTrue(t.isComplete());
        assertFalse(t.isCanceled());
        assertNull(t.getException());

        assertEquals(1, repo.added.size());
        NotificationRecord r = repo.added.get(0);

        assertEquals(recipientId, getField(r, "recipientId"));
        assertEquals(eventId, getField(r, "eventId"));
        assertEquals(NotificationType.WIN.name(), getField(r, "type"));
        assertEquals(message, getField(r, "message"));

        Object readVal = getField(r, "read");
        assertNotNull(readVal);
        assertTrue(readVal instanceof Boolean);
        assertFalse((Boolean) readVal);

        assertNotNull(getField(r, "createdAt"));
    }
    /**
     * Verifies that notifyNotSelected() creates a LOSE-type notification record
     * with the correct recipient, event, message, and initial unread status.
     */
    @Test
    public void notifyNotSelected_addsLoseNotificationRecord() {
        FakeNotificationLogRepository repo = new FakeNotificationLogRepository();
        PathosNotifyService service = new PathosNotifyService(repo);

        String recipientId = "entrant_456";
        String eventId = "event_music_002";
        String message = "You were not selected for this event.";

        Task<Void> t = service.notifyNotSelected(recipientId, eventId, message);

        assertNotNull(t);
        assertTrue(t.isComplete());
        assertFalse(t.isCanceled());
        assertNull(t.getException());

        assertEquals(1, repo.added.size());
        NotificationRecord r = repo.added.get(0);

        assertEquals(recipientId, getField(r, "recipientId"));
        assertEquals(eventId, getField(r, "eventId"));
        assertEquals(NotificationType.LOSE.name(), getField(r, "type"));
        assertEquals(message, getField(r, "message"));

        Object readVal = getField(r, "read");
        assertNotNull(readVal);
        assertTrue(readVal instanceof Boolean);
        assertFalse((Boolean) readVal);

        assertNotNull(getField(r, "createdAt"));
    }

    /**
     * Verifies that notifyCoOrganizerAdded() creates a CO_ORGANIZER_ADDED-type
     * notification record with the correct recipient, event, message, and initial unread status.
     */
    @Test
    public void notifyCoOrganizerAdded_addsCoOrganizerNotificationRecord() {
        FakeNotificationLogRepository repo = new FakeNotificationLogRepository();
        PathosNotifyService service = new PathosNotifyService(repo);

        String recipientId = "coorg_001";
        String eventId = "event_hackathon_003";
        String message = "You were added as a co-organizer for Hackathon Night";

        Task<Void> t = service.notifyCoOrganizerAdded(recipientId, eventId, message);

        assertNotNull(t);
        assertTrue(t.isComplete());
        assertFalse(t.isCanceled());
        assertNull(t.getException());

        assertEquals(1, repo.added.size());
        NotificationRecord r = repo.added.get(0);

        assertEquals(recipientId, getField(r, "recipientId"));
        assertEquals(eventId, getField(r, "eventId"));
        assertEquals(NotificationType.CO_ORGANIZER_ADDED.name(), getField(r, "type"));
        assertEquals(message, getField(r, "message"));

        Object readVal = getField(r, "read");
        assertNotNull(readVal);
        assertTrue(readVal instanceof Boolean);
        assertFalse((Boolean) readVal);

        assertNotNull(getField(r, "createdAt"));
    }

    private static Object getField(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            throw new AssertionError(
                    "Missing/renamed field '" + fieldName + "' in " + obj.getClass().getSimpleName()
                            + ". Update the test or add a getter.",
                    e
            );
        }
    }
}