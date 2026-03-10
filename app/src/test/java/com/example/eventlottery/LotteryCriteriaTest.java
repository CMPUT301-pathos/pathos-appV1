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
        // Use a fake repository so Firebase is never touched
        controller = new EventController(new EventRepository() {
            @Override
            public void createEvent(Object event, CreateCallback callback) {}

            @Override
            public void getAllEvents(ListCallback callback) {}

            @Override
            public void getEventsByOrganizer(String organizerDeviceId, ListCallback callback) {}
        });
    }

    private EventSummary makeEvent(int capacity, int drawSize,
                                   long regStart, long regEnd) {
        long now = System.currentTimeMillis();
        return new EventSummary(
                "1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports",
                now + 100000, regStart, regEnd, capacity, drawSize);
    }

    @Test
    public void testCriteria_containsLotteryCriteriaHeader() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("Lottery Criteria"));
    }

    @Test
    public void testCriteria_showsDrawSize() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("10"));
    }

    @Test
    public void testCriteria_showsCapacity() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("50"));
    }

    @Test
    public void testCriteria_unlimitedCapacity_showsUnlimited() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(0, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("Unlimited"));
    }

    @Test
    public void testCriteria_containsSelectionProcessExplanation() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("randomly"));
    }

    @Test
    public void testCriteria_showsRegistrationDates() {
        long now = System.currentTimeMillis();
        EventSummary event = makeEvent(50, 10, now - 100000, now + 100000);
        String criteria = controller.getLotteryCriteria(event);
        assertTrue(criteria.contains("Registration Opens"));
        assertTrue(criteria.contains("Registration Closes"));
    }
}
