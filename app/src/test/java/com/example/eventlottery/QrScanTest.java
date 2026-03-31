package com.example.eventlottery;

import com.example.eventlottery.controller.EventController;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.service.BeaconQrService;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for QR scan resolution logic.
 *
 * User stories covered:
 * - US 01.06.01: View event details by scanning the promotional QR code
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class QrScanTest {

    private BeaconQrService beaconQrService;
    private EventController controller;

    @Before
    public void setUp() {
        controller = new EventController(new EventRepository() {
            @Override
            public void createEvent(Object event, CreateCallback callback) {}

            @Override
            public void getAllEvents(ListCallback callback) {}

            @Override
            public void getEventsByOrganizer(String organizerDeviceId, ListCallback callback) {}

            @Override
            public void getManageableEvents(String organizerDeviceId, ListCallback callback) {}

            @Override
            public void deleteEvent(String eventId, OperationCallback callback) {}
        });
    }

    @Test
    public void testDecodeEventId_eventIdPrefix() {
        String payload = "eventId:n0luv6NL4JoAuL0J4ZkE";
        String eventId = beaconQrService.decodeEventId(payload);
        assertEquals("n0luv6NL4JoAuL0J4ZkE", eventId);
    }

    @Test
    public void testDecodeEventId_pathosPrefix() {
        String payload = "pathos://event/abc123";
        String eventId = beaconQrService.decodeEventId(payload);
        assertEquals("abc123", eventId);
    }

    @Test
    public void testDecodeEventId_fallback() {
        String payload = "abc123";
        String eventId = beaconQrService.decodeEventId(payload);
        assertEquals("abc123", eventId);
    }

    @Test
    public void testDecodeEventId_nullReturnsNull() {
        String eventId = beaconQrService.decodeEventId(null);
        assertNull(eventId);
    }

    @Test
    public void testDecodeEventId_emptyReturnsNull() {
        String eventId = beaconQrService.decodeEventId("");
        assertNull(eventId);
    }

    @Test
    public void testDecodeEventId_whitespaceReturnsNull() {
        String eventId = beaconQrService.decodeEventId("   ");
        assertNull(eventId);
    }
}
