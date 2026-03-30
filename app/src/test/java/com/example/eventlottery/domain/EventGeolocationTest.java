package com.example.eventlottery.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Map;

/**
 * Unit tests for Event geolocation support.
 *
 * User stories covered:
 * - US 02.02.03: Organizer enables or disables geolocation requirement
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class EventGeolocationTest {

    @Test
    public void constructor_defaultsGeolocationRequirementToFalse() {
        Event event = new Event("Swim Lessons", "Beginner class", "organizer1");

        assertFalse(event.isGeolocationRequired());
    }

    @Test
    public void setRequiresGeolocation_true_updatesFlag() {
        Event event = new Event("Swim Lessons", "Beginner class", "organizer1");

        event.setRequiresGeolocation(true);

        assertTrue(event.isGeolocationRequired());
    }

    @Test
    public void setRequiresGeolocation_false_updatesFlag() {
        Event event = new Event("Swim Lessons", "Beginner class", "organizer1");
        event.setRequiresGeolocation(true);

        event.setRequiresGeolocation(false);

        assertFalse(event.isGeolocationRequired());
    }

    @Test
    public void toMap_includesRequiresGeolocationField() {
        Event event = new Event("Swim Lessons", "Beginner class", "organizer1");
        event.setRequiresGeolocation(true);

        Map<String, Object> map = event.toMap();

        assertTrue(map.containsKey("requiresGeolocation"));
        assertEquals(true, map.get("requiresGeolocation"));
    }

    @Test
    public void registrationOpen_trueWhenNowWithinWindow() {
        Event event = new Event("Swim Lessons", "Beginner class", "organizer1");
        long now = System.currentTimeMillis();

        event.setRegistrationStart(now - 1000L);
        event.setRegistrationEnd(now + 1000L);

        assertTrue(event.isRegistrationOpen());
    }

    @Test
    public void registrationOpen_falseWhenNowOutsideWindow() {
        Event event = new Event("Swim Lessons", "Beginner class", "organizer1");
        long now = System.currentTimeMillis();

        event.setRegistrationStart(now + 10000L);
        event.setRegistrationEnd(now + 20000L);

        assertFalse(event.isRegistrationOpen());
    }
}