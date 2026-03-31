package com.example.eventlottery.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventSummaryTest {

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