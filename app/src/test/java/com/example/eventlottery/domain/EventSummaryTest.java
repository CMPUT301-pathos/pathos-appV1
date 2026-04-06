package com.example.eventlottery.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventSummaryTest {

    /**
     * Verifies that isUserOrganizer() returns true for the primary organizer
     * device ID.
     */
    @Test
    public void isUserOrganizer_returnsTrue_forPrimaryOrganizer() {
        EventSummary summary = new EventSummary(
                "event1",
                "Test Event",
                "Desc",
                "Edmonton",
                100L,
                "primary123",
                "Community",
                200L,
                300L,
                400L,
                10,
                5,
                null
        );

        assertTrue(summary.isUserOrganizer("primary123"));
    }

    /**
     * Verifies that isUserOrganizer() returns true for co-organizer device IDs
     * listed in the co-organizer list.
     */
    @Test
    public void isUserOrganizer_returnsTrue_forCoOrganizer() {
        EventSummary summary = new EventSummary(
                "event1",
                "Test Event",
                "Desc",
                "Edmonton",
                100L,
                "primary123",
                "Community",
                200L,
                300L,
                400L,
                10,
                5,
                null
        );

        summary.setCoOrganizerIds(Arrays.asList("co1", "co2"));

        assertTrue(summary.isUserOrganizer("co2"));
    }

    /**
     * Verifies that isUserOrganizer() returns false for a device ID that is
     * neither the primary organizer nor a co-organizer.
     */
    @Test
    public void isUserOrganizer_returnsFalse_forNonOrganizer() {
        EventSummary summary = new EventSummary(
                "event1",
                "Test Event",
                "Desc",
                "Edmonton",
                100L,
                "primary123",
                "Community",
                200L,
                300L,
                400L,
                10,
                5,
                null
        );

        summary.setCoOrganizerIds(Arrays.asList("co1", "co2"));

        assertFalse(summary.isUserOrganizer("randomUser"));
    }

    /**
     * Verifies that setCoOrganizerIds(null) converts null to an empty list
     * rather than maintaining null state.
     */
    @Test
    public void setCoOrganizerIds_null_setsEmptyList() {
        EventSummary summary = new EventSummary(
                "event1",
                "Test Event",
                "Desc",
                "Edmonton",
                100L,
                "primary123",
                "Community",
                200L,
                300L,
                400L,
                10,
                5,
                null
        );

        summary.setCoOrganizerIds(null);

        assertNotNull(summary.getCoOrganizerIds());
        assertTrue(summary.getCoOrganizerIds().isEmpty());
    }

    /**
     * Verifies that getCoOrganizerIds() returns a defensive copy that does not
     * allow external modification of the internal co-organizer list.
     */
    @Test
    public void getCoOrganizerIds_returnsDefensiveCopy() {
        EventSummary summary = new EventSummary(
                "event1",
                "Test Event",
                "Desc",
                "Edmonton",
                100L,
                "primary123",
                "Community",
                200L,
                300L,
                400L,
                10,
                5,
                null
        );

        summary.setCoOrganizerIds(new ArrayList<>(Arrays.asList("co1", "co2")));

        List<String> retrieved = summary.getCoOrganizerIds();
        retrieved.add("co3");

        assertEquals(2, summary.getCoOrganizerIds().size());
        assertFalse(summary.getCoOrganizerIds().contains("co3"));
    }
}