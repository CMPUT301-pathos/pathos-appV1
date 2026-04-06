package com.example.eventlottery;

import com.example.eventlottery.domain.EventSummary;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for poster update logic.
 *
 * Tests are scoped to pure domain/model logic since Firebase Storage
 * cannot be used in unit tests.
 *
 * User stories covered:
 * - US 02.04.02: Organizer can update an event poster
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class PosterServiceTest {

    private EventSummary makeEvent(String posterUrl) {
        long now = System.currentTimeMillis();
        return new EventSummary(
                "event1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports",
                now + 100000, now - 100000, now + 100000,
                50, 10, posterUrl);
    }

    /**
     * Verifies that an EventSummary can store and retrieve a poster URL.
     */
    @Test
    public void testEventSummary_hasPosterUrl() {
        EventSummary event = makeEvent("https://firebase.com/poster.jpg");
        assertEquals("https://firebase.com/poster.jpg", event.getPosterUrl());
    }

    /**
     * Verifies that an EventSummary with a null poster URL correctly
     * represents an event with no poster.
     */
    @Test
    public void testEventSummary_nullPosterUrl() {
        EventSummary event = makeEvent(null);
        assertNull(event.getPosterUrl());
    }

    /**
     * Verifies that an EventSummary with an empty string poster URL
     * preserves the empty value correctly.
     */
    @Test
    public void testEventSummary_emptyPosterUrl() {
        EventSummary event = makeEvent("");
        assertEquals("", event.getPosterUrl());
    }

    /**
     * Verifies that setting a poster URL does not affect other
     * EventSummary fields (ID, name, location).
     */
    @Test
    public void testEventSummary_posterUrlDoesNotAffectOtherFields() {
        EventSummary event = makeEvent("https://firebase.com/poster.jpg");
        assertEquals("event1", event.getId());
        assertEquals("Test Event", event.getName());
        assertEquals("Edmonton", event.getLocation());
    }

    /**
     * Verifies that a poster URL can be updated by creating a new EventSummary
     * with the new URL (simulating upload flow).
     */
    @Test
    public void testEventSummary_posterUrlIsUpdatable() {
        // Simulates the flow: old URL is replaced with new URL after upload
        String oldUrl = "https://firebase.com/old_poster.jpg";
        String newUrl = "https://firebase.com/new_poster.jpg";

        EventSummary oldEvent = makeEvent(oldUrl);
        EventSummary updatedEvent = makeEvent(newUrl);

        assertNotEquals(oldEvent.getPosterUrl(), updatedEvent.getPosterUrl());
        assertEquals(newUrl, updatedEvent.getPosterUrl());
    }

    /**
     * Verifies that a poster URL can be removed by setting it to null
     * (simulating poster deletion flow).
     */
    @Test
    public void testEventSummary_removePoster_setsNullUrl() {
        // Simulates the flow: poster is removed, URL becomes null
        EventSummary event = makeEvent(null);
        assertNull(event.getPosterUrl());
    }
}
