package com.example.eventlottery;

import com.example.eventlottery.controller.EventController;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.domain.EventSummary;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Unit tests for lottery criteria logic.
 *
 * User stories covered:
 * - US 01.05.05: Be informed about the criteria or guidelines for the lottery selection process
 *
 * @author Fawaz Mansoor
 * @version 1.1
 */
public class LotteryCriteriaTest {

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
    private EventSummary makeEvent(int capacity, int drawSize,
                                   long regStart, long regEnd) {
        long now = System.currentTimeMillis();
        return new EventSummary(
                "1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports",
                now + 100000, regStart, regEnd, capacity, drawSize, null);
    }

    /**
     * Verifies that the criteria summary contains the standard header
     * "Lottery Criteria".
     */
    @Test
    public void testCriteria_containsLotteryCriteriaHeader() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("Lottery Criteria"));
    }

    /**
     * Verifies that the criteria summary includes the draw size
     * (number of lottery winners).
     */
    @Test
    public void testCriteria_showsDrawSize() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("10"));
    }

    /**
     * Verifies that the criteria summary includes the event capacity.
     */
    @Test
    public void testCriteria_showsCapacity() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("50"));
    }

    /**
     * Verifies that the criteria summary displays "Unlimited" for events
     * with zero capacity.
     */
    @Test
    public void testCriteria_unlimitedCapacity_showsUnlimited() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(0, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("Unlimited"));
    }

    /**
     * Verifies that the criteria summary explains the selection process
     * uses random draw.
     */
    @Test
    public void testCriteria_containsSelectionProcessExplanation() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("randomly"));
    }

    /**
     * Verifies that the criteria summary includes registration start
     * and end dates.
     */
    @Test
    public void testCriteria_showsRegistrationDates() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("Registration Opens"));
        assertTrue(criteria.contains("Registration Closes"));
    }
}
