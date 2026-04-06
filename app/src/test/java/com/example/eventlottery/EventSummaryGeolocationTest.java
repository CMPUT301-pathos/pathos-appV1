package com.example.eventlottery.domain;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for EventSummary geolocation support.
 *
 * User stories covered:
 * - US 02.02.03: Organizer enables or disables geolocation requirement
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class EventSummaryGeolocationTest {

    /**
     * Verifies that EventSummary constructor defaults the geolocation requirement
     * flag to false and all accessor methods return false.
     */
    @Test
    public void constructor_defaultsGeolocationRequirementToFalse() {
        EventSummary summary = new EventSummary(
                "event1",
                "Swim Lessons",
                "Beginner class",
                "Edmonton",
                100L,
                "organizer1",
                "Sports",
                200L,
                300L,
                400L,
                20,
                10,
                null
        );

        assertFalse(summary.isRequiresGeolocation());
        assertFalse(summary.isGeolocationRequired());
        assertFalse(summary.getRequiresGeolocation());
    }

    /**
     * Verifies that setRequiresGeolocation(true) updates the flag so that
     * all accessor methods return true.
     */
    @Test
    public void setRequiresGeolocation_true_updatesAllAccessors() {
        EventSummary summary = new EventSummary(
                "event1",
                "Swim Lessons",
                "Beginner class",
                "Edmonton",
                100L,
                "organizer1",
                "Sports",
                200L,
                300L,
                400L,
                20,
                10,
                null
        );

        summary.setRequiresGeolocation(true);

        assertTrue(summary.isRequiresGeolocation());
        assertTrue(summary.isGeolocationRequired());
        assertTrue(summary.getRequiresGeolocation());
    }

    /**
     * Verifies that setGeolocationRequired(true) updates the geolocation flag.
     */
    @Test
    public void setGeolocationRequired_true_updatesFlag() {
        EventSummary summary = new EventSummary(
                "event1",
                "Swim Lessons",
                "Beginner class",
                "Edmonton",
                100L,
                "organizer1",
                "Sports",
                200L,
                300L,
                400L,
                20,
                10,
                null
        );

        summary.setGeolocationRequired(true);

        assertTrue(summary.isRequiresGeolocation());
    }

    /**
     * Verifies that isRegistrationOpen() returns true when the current time
     * falls within the registration window.
     */
    @Test
    public void registrationOpen_trueWhenNowWithinWindow() {
        long now = System.currentTimeMillis();

        EventSummary summary = new EventSummary(
                "event1",
                "Swim Lessons",
                "Beginner class",
                "Edmonton",
                100L,
                "organizer1",
                "Sports",
                200L,
                now - 1000L,
                now + 1000L,
                20,
                10,
                null
        );

        assertTrue(summary.isRegistrationOpen());
    }

    /**
     * Verifies that isRegistrationOpen() returns false when registration dates
     * are not set (both are 0).
     */
    @Test
    public void registrationOpen_falseWhenWindowMissing() {
        EventSummary summary = new EventSummary(
                "event1",
                "Swim Lessons",
                "Beginner class",
                "Edmonton",
                100L,
                "organizer1",
                "Sports",
                200L,
                0L,
                0L,
                20,
                10,
                null
        );

        assertFalse(summary.isRegistrationOpen());
    }
}