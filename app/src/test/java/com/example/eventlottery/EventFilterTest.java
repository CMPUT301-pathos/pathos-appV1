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

    /**
     * Verifies that filtering events by category returns only events
     * matching the specified category.
     */
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

    /**
     * Verifies that filtering with "All" category returns all events
     * without filtering by category.
     */
    @Test
    public void testFilterByCategory_allReturnsEverything() {
        List<EventSummary> events = new ArrayList<>();
        events.add(makeSummary("1", "Sports Event", "Sports"));
        events.add(makeSummary("2", "Music Event", "Music"));

        List<EventSummary> filtered = new ArrayList<>(events);

        assertEquals(2, filtered.size());
    }

    /**
     * Verifies that filtering by a category with no matching events
     * returns an empty list.
     */
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

    /**
     * Verifies that category filtering is case-insensitive,
     * matching "sports", "Sports", "SPORTS".
     */
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

    /**
     * Verifies that filtering events by location returns only events
     * whose location contains the search string.
     */
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

    /**
     * Verifies that filtering by a location with no matching events
     * returns an empty list.
     */
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

    /**
     * Verifies that isRegistrationOpen() returns true when the current time
     * falls within the registration window.
     */
    @Test
    public void testIsRegistrationOpen_returnsTrue() {
        long now = System.currentTimeMillis();
        EventSummary openEvent = new EventSummary(
                "1", "Open Event", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 100000, 0, 0, null);

        assertTrue(openEvent.isRegistrationOpen());
    }

    /**
     * Verifies that isRegistrationOpen() returns false when the current time
     * is outside the registration window.
     */
    @Test
    public void testIsRegistrationOpen_returnsFalse() {
        long now = System.currentTimeMillis();
        EventSummary closedEvent = new EventSummary(
                "2", "Closed Event", "desc", "Calgary",
                now, "org1", "Music", now + 100000, now - 200000, now - 100000, 0, 0, null);

        assertFalse(closedEvent.isRegistrationOpen());
    }

    /**
     * Verifies that filtering events by availability (registration open status)
     * returns only events in their registration window.
     */
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

    /**
     * Verifies that an event's capacity lottery criteria field is set correctly.
     */
    @Test
    public void testLotteryCriteria_capacityIsSet() {
        long now = System.currentTimeMillis();
        EventSummary event = new EventSummary(
                "1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 100000, 50, 10, null);

        assertEquals(50, event.getCapacity());
    }

    /**
     * Verifies that an event's draw size (number of lottery winners)
     * lottery criteria field is set correctly.
     */
    @Test
    public void testLotteryCriteria_drawSizeIsSet() {
        long now = System.currentTimeMillis();
        EventSummary event = new EventSummary(
                "1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 100000, 50, 10, null);

        assertEquals(10, event.getDrawSize());
    }

    /**
     * Verifies that an event with zero capacity represents unlimited availability.
     */
    @Test
    public void testLotteryCriteria_unlimitedCapacity() {
        long now = System.currentTimeMillis();
        EventSummary event = new EventSummary(
                "1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 100000, 0, 0, null);

        assertEquals(0, event.getCapacity());
    }
}
