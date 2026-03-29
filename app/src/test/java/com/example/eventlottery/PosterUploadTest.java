package com.example.eventlottery;

import com.example.eventlottery.domain.EventSummary;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for poster upload logic using Base64 encoding.
 *
 * Verifies that poster data is correctly stored and retrieved
 * as a Base64-encoded string in the EventSummary domain model.
 *
 * User stories covered:
 * - US 02.04.02: Organizer can update an event poster
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see EventSummary
 */
public class PosterUploadTest {

    private EventSummary makeEventWithPoster(String posterUrl) {
        long now = System.currentTimeMillis();
        return new EventSummary(
                "event1", "Test Event", "desc", "Edmonton",
                now, "org1", "Sports",
                now + 100000, now - 100000, now + 200000,
                50, 10, posterUrl);
    }

    @Test
    public void testPosterUrl_nullByDefault() {
        EventSummary event = makeEventWithPoster(null);
        assertNull(event.getPosterUrl());
    }

    @Test
    public void testPosterUrl_storesBase64String() {
        String fakeBase64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgAB";
        EventSummary event = makeEventWithPoster(fakeBase64);
        assertEquals(fakeBase64, event.getPosterUrl());
    }

    @Test
    public void testPosterUrl_isBase64Format() {
        String base64Poster = "data:image/jpeg;base64,/9j/4AAQSkZJRgAB";
        EventSummary event = makeEventWithPoster(base64Poster);
        assertNotNull(event.getPosterUrl());
        assertTrue(event.getPosterUrl().startsWith("data:image"));
    }

    @Test
    public void testPosterUrl_containsBase64Prefix() {
        String base64Poster = "data:image/jpeg;base64,ABC123";
        EventSummary event = makeEventWithPoster(base64Poster);
        assertTrue(event.getPosterUrl().contains("base64,"));
    }

    @Test
    public void testPosterUrl_regularUrlStillWorks() {
        String regularUrl = "https://example.com/poster.jpg";
        EventSummary event = makeEventWithPoster(regularUrl);
        assertEquals(regularUrl, event.getPosterUrl());
        assertFalse(event.getPosterUrl().startsWith("data:image"));
    }

    @Test
    public void testPosterUrl_emptyStringStoredCorrectly() {
        EventSummary event = makeEventWithPoster("");
        assertNotNull(event.getPosterUrl());
        assertTrue(event.getPosterUrl().isEmpty());
    }
}
