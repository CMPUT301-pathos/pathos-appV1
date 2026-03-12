package com.example.eventlottery;

import com.example.eventlottery.domain.EventSummary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for event filtering and lottery criteria logic.
 *
 * User stories covered:
 * - US 01.01.03: See a list of events to join the waiting list for
 * - US 01.01.04: Filter events based on interests and availability
 * - US 01.05.05: Be informed about lottery selection criteria
 *
 * @author Fawaz Mansoor
 * @version 1.1
 */
public class EventFilterTest {

    // Helper methods
    private EventSummary makeSummary(String id, String name, String category) {
        return new EventSummary(id, name, "desc", "", 0L, "org1", category, 0L, 0L, 0L, 0, 0, null);
    }

    private EventSummary makeSummary(String id, String name, String category, String location) {
        return new EventSummary(id, name, "desc", location, 0L, "org1", category, 0L, 0L, 0L, 0, 0, null);
    }

    // --- Category filter tests ---

    @Test
    public void testFilterByCategory_returnsMatchingEvents() {
        List<EventSummary> events = new ArrayList<>();
        events.add(makeSummary("1", "Sports Event", "Sports"));
        events.add(makeSummary("2", "Music Event", "Music"));
        events.add(makeSummary("3", "Another Sports", "Sports"));

        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : events) {
            if (e.getCategory().equalsIgnoreCase("Sports")) filtered.add(e);
        }

        assertEquals(2, filtered.size());
    }

    @Test
    public void testFilterByCategory_allReturnsEverything() {
        List<EventSummary> events = new ArrayList<>();
        events.add(makeSummary("1", "Sports Event", "Sports"));
        events.add(makeSummary("2", "Music Event", "Music"));

        List<EventSummary> filtered = new ArrayList<>(events);

        assertEquals(2, filtered.size());
    }

    @Test
    public void testFilterByCategory_noMatchReturnsEmpty() {
        List<EventSummary> events = new ArrayList<>();
        events.add(makeSummary("1", "Sports Event", "Sports"));
        events.add(makeSummary("2", "Music Event", "Music"));

        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : events) {
            if (e.getCategory().equalsIgnoreCase("Community")) filtered.add(e);
        }

        assertEquals(0, filtered.size());
    }

    @Test
    public void testFilterByCategory_caseInsensitive() {
        List<EventSummary> events = new ArrayList<>();
        events.add(makeSummary("1", "Sports Event", "sports"));

        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : events) {
            if (e.getCategory().equalsIgnoreCase("SPORTS")) filtered.add(e);
        }

        assertEquals(1, filtered.size());
    }

    // --- Location filter tests ---

    @Test
    public void testFilterByLocation_returnsMatchingEvents() {
        List<EventSummary> events = new ArrayList<>();
        events.add(makeSummary("1", "Edmonton Event", "Sports", "Edmonton"));
        events.add(makeSummary("2", "Calgary Event", "Music", "Calgary"));
        events.add(makeSummary("3", "Edmonton Arts", "Arts", "Edmonton"));

        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : events) {
            if (e.getLocation().toLowerCase().contains("edmonton")) filtered.add(e);
        }

        assertEquals(2, filtered.size());
    }

    @Test
    public void testFilterByLocation_noMatchReturnsEmpty() {
        List<EventSummary> events = new ArrayList<>();
        events.add(makeSummary("1", "Edmonton Event", "Sports", "Edmonton"));

        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : events) {
            if (e.getLocation().toLowerCase().contains("calgary")) filtered.add(e);
        }

        assertEquals(0, filtered.size());
    }

    // --- Availability filter tests ---

    @Test
    public void testIsRegistrationOpen_returnsTrue() {
        long now = System.currentTimeMillis();
        EventSummary openEvent = new EventSummary(
                "1", "Open Event", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 100000, 0, 0, null);

        assertTrue(openEvent.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_returnsFalse() {
        long now = System.currentTimeMillis();
        EventSummary closedEvent = new EventSummary(
                "2", "Closed Event", "desc", "Calgary",
                now, "org1", "Music", now + 100000, now - 200000, now - 100000, 0, 0, null);

        assertFalse(closedEvent.isRegistrationOpen());
    }

    @Test
    public void testFilterByAvailability_returnsOnlyOpenEvents() {
        long now = System.currentTimeMillis();

        EventSummary openEvent = new EventSummary(
                "1", "Open Event", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 100000, 0, 0, null);

        EventSummary closedEvent = new EventSummary(
                "2", "Closed Event", "desc", "Calgary",
                now, "org1", "Music", now + 100000, now - 200000, now - 100000, 0, 0, null);

        List<EventSummary> events = new ArrayList<>();
        events.add(openEvent);
        events.add(closedEvent);

        List<EventSummary> filtered = new ArrayList<>();
        for (EventSummary e : events) {
            if (e.isRegistrationOpen()) filtered.add(e);
        }

        assertEquals(1, filtered.size());
        assertEquals("Open Event", filtered.get(0).getName());
    }

    // --- Lottery criteria tests ---

    @Test
    public void testLotteryCriteria_capacityIsSet() {
        long now = System.currentTimeMillis();
        EventSummary event = new EventSummary(
                "1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 100000, 50, 10, null);

        assertEquals(50, event.getCapacity());
    }

    @Test
    public void testLotteryCriteria_drawSizeIsSet() {
        long now = System.currentTimeMillis();
        EventSummary event = new EventSummary(
                "1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 100000, 50, 10, null);

        assertEquals(10, event.getDrawSize());
    }

    @Test
    public void testLotteryCriteria_unlimitedCapacity() {
        long now = System.currentTimeMillis();
        EventSummary event = new EventSummary(
                "1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 100000, 0, 0, null);

        assertEquals(0, event.getCapacity());
    }
}
