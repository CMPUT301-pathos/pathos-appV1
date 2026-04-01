package com.example.eventlottery;

import com.example.eventlottery.controller.EventController;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.domain.EventSummary;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for keyword search and capacity filtering in EventController.
 *
 * User stories covered:
 * - US 01.01.04: Filter events based on interests and availability
 * - US 01.01.05: Search for events by keyword
 * - US 01.01.06: Use keyword search with filtering
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see EventController
 */
public class EventSearchFilterTest {

    private EventController controller;
    private long now;

    @Before
    public void setUp() {
        now = System.currentTimeMillis();

        controller = new EventController(new EventRepository() {
            @Override
            public void createEvent(Object event, CreateCallback callback) {}

            @Override
            public void getAllEvents(ListCallback callback) {
                callback.onSuccess(Arrays.asList(
                        new EventSummary("1", "Swimming Lessons", "Learn to swim",
                                "Edmonton", now, "org1", "Sports",
                                now + 100000, now - 100000, now + 200000, 50, 10, null),
                        new EventSummary("2", "Yoga Class", "Relax and stretch",
                                "Calgary", now, "org2", "Sports",
                                now + 100000, now - 100000, now + 200000, 20, 5, null),
                        new EventSummary("3", "Piano Lessons", "Learn piano basics",
                                "Edmonton", now, "org3", "Music",
                                now + 100000, now - 100000, now + 200000, 10, 3, null),
                        new EventSummary("4", "Art Workshop", "Paint and create",
                                "Edmonton", now, "org4", "Arts",
                                now + 100000, now - 100000, now + 200000, 100, 20, null),
                        new EventSummary("5", "Community Cleanup", "Help clean the park",
                                "Edmonton", now, "org5", "Community",
                                now + 100000, now - 100000, now + 200000, 0, 50, null)
                ));
            }

            @Override
            public void getEventsByOrganizer(String organizerDeviceId, ListCallback callback) {}

            @Override
            public void deleteEvent(String eventId, OperationCallback callback) {}

            public void getManageableEvents(String deviceId, ListCallback callback) {}
        });



        controller.loadAllEvents(new EventRepository.ListCallback() {
            @Override
            public void onSuccess(List<EventSummary> events) {}
            @Override
            public void onFailure(Exception e) {}
        });
    }

    // ── Keyword search tests ──────────────────────────────────────

    @Test
    public void testSearch_byEventName_returnsMatch() {
        List<EventSummary> results = controller.searchByKeyword("Swimming");
        assertEquals(1, results.size());
        assertEquals("Swimming Lessons", results.get(0).getName());
    }

    @Test
    public void testSearch_byDescription_returnsMatch() {
        List<EventSummary> results = controller.searchByKeyword("stretch");
        assertEquals(1, results.size());
        assertEquals("Yoga Class", results.get(0).getName());
    }

    @Test
    public void testSearch_byCategory_returnsAllMatches() {
        List<EventSummary> results = controller.searchByKeyword("Sports");
        assertEquals(2, results.size());
    }

    @Test
    public void testSearch_caseInsensitive() {
        List<EventSummary> results = controller.searchByKeyword("swimming");
        assertEquals(1, results.size());
    }

    @Test
    public void testSearch_emptyKeyword_returnsAll() {
        List<EventSummary> results = controller.searchByKeyword("");
        assertEquals(5, results.size());
    }

    @Test
    public void testSearch_nullKeyword_returnsAll() {
        List<EventSummary> results = controller.searchByKeyword(null);
        assertEquals(5, results.size());
    }

    @Test
    public void testSearch_noMatch_returnsEmpty() {
        List<EventSummary> results = controller.searchByKeyword("xyznotfound");
        assertEquals(0, results.size());
    }

    // ── Capacity filter tests ─────────────────────────────────────

    @Test
    public void testCapacityFilter_returnsEventsUnderLimit() {
        List<EventSummary> results = controller.filterByMaxCapacity(25);
        assertEquals(3, results.size());
    }

    @Test
    public void testCapacityFilter_unlimitedEventsAlwaysIncluded() {
        List<EventSummary> results = controller.filterByMaxCapacity(5);
        assertTrue(results.stream().anyMatch(e -> e.getName().equals("Community Cleanup")));
    }

    @Test
    public void testCapacityFilter_zeroMeansNoFilter() {
        List<EventSummary> results = controller.filterByMaxCapacity(0);
        assertEquals(5, results.size());
    }

    @Test
    public void testCapacityFilter_exactMatch_isIncluded() {
        List<EventSummary> results = controller.filterByMaxCapacity(50);
        assertTrue(results.stream().anyMatch(e -> e.getName().equals("Swimming Lessons")));
    }

    @Test
    public void testCapacityFilter_belowAllCapacities_returnsOnlyUnlimited() {
        List<EventSummary> results = controller.filterByMaxCapacity(5);
        for (EventSummary e : results) {
            assertTrue(e.getCapacity() == 0 || e.getCapacity() <= 5);
        }
    }

    // ── Combined search + filter tests ───────────────────────────

    @Test
    public void testApplyAllFilters_keywordAndCategory() {
        List<EventSummary> results = controller.applyAllFilters(
                "Lessons", "Sports", "", false, 0, 0);
        assertEquals(1, results.size());
        assertEquals("Swimming Lessons", results.get(0).getName());
    }

    @Test
    public void testApplyAllFilters_keywordAndCapacity() {
        List<EventSummary> results = controller.applyAllFilters(
                "Lessons", "All", "", false, 30, 0);
        assertEquals(1, results.size());
        assertEquals("Piano Lessons", results.get(0).getName());
    }

    @Test
    public void testApplyAllFilters_noFilters_returnsAll() {
        List<EventSummary> results = controller.applyAllFilters(
                "", "All", "", false, 0, 0);
        assertEquals(5, results.size());
    }

    @Test
    public void testApplyAllFilters_keywordAndLocation() {
        List<EventSummary> results = controller.applyAllFilters(
                "Relax", "All", "Calgary", false, 0, 0);
        assertEquals(1, results.size());
        assertEquals("Yoga Class", results.get(0).getName());
    }

    @Test
    public void testApplyAllFilters_categoryOnly() {
        List<EventSummary> results = controller.applyAllFilters(
                "", "Music", "", false, 0, 0);
        assertEquals(1, results.size());
        assertEquals("Piano Lessons", results.get(0).getName());
    }

    @Test
    public void testApplyAllFilters_openOnlyWithNoOpenEvents() {
        // All events have registration open (regStart < now < regEnd)
        List<EventSummary> results = controller.applyAllFilters(
                "", "All", "", true, 0, 0);
        assertEquals(5, results.size());
    }

    @Test
    public void testApplyAllFilters_keywordCategoryAndCapacity() {
        List<EventSummary> results = controller.applyAllFilters(
                "Learn", "All", "", false, 15, 0);
        // "Learn to swim" (Swimming, cap 50 > 15 excluded)
        // "Learn piano basics" (Piano, cap 10 <= 15 included)
        assertEquals(1, results.size());
        assertEquals("Piano Lessons", results.get(0).getName());
    }
}
