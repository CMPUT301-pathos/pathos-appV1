package com.example.eventlottery.domain;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Event model - Admin related functionality.
 * Covers US 03.01.01, US 03.04.01
 * @author hasratsinghchauhan
 * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 * whats being tested? Event getters/setters, registration logic, constructor, map conversion
 */
public class EventAdminTest {

    private Event event;

    @Before
    public void setUp() {
        event = new Event();
    }

    /**
     * Verifies that setting and getting event ID works correctly.
     */
    @Test
    public void testEventId() {
        event.setId("event123");
        assertEquals("event123", event.getId());
    }

    /**
     * Verifies that setting and getting event name works correctly.
     */
    @Test
    public void testEventName() {
        event.setName("Swimming Lessons");
        assertEquals("Swimming Lessons", event.getName());
    }

    /**
     * Verifies that setting and getting event description works correctly.
     */
    @Test
    public void testEventDescription() {
        event.setDescription("Beginner swimming class");
        assertEquals("Beginner swimming class", event.getDescription());
    }

    /**
     * Verifies that setting and getting organizer device ID works correctly.
     */
    @Test
    public void testOrganizerDeviceId() {
        event.setOrganizerDeviceId("device456");
        assertEquals("device456", event.getOrganizerDeviceId());
    }

    /**
     * Verifies that setting and getting poster URL works correctly.
     */
    @Test
    public void testPosterUrl() {
        event.setPosterUrl("https://example.com/poster.jpg");
        assertEquals("https://example.com/poster.jpg", event.getPosterUrl());
    }

    /**
     * Verifies that setting and getting event category works correctly.
     */
    @Test
    public void testCategory() {
        event.setCategory("Sports");
        assertEquals("Sports", event.getCategory());
    }

    /**
     * Verifies that setting and getting event location works correctly.
     */
    @Test
    public void testLocation() {
        event.setLocation("Pool A");
        assertEquals("Pool A", event.getLocation());
    }

    /**
     * Verifies that setting and getting event date (timestamp) works correctly.
     */
    @Test
    public void testEventDate() {
        long testDate = 1700000000000L;
        event.setEventDate(testDate);
        assertEquals(testDate, event.getEventDate());
    }

    /**
     * Verifies that setting and getting registration start time works correctly.
     */
    @Test
    public void testRegistrationStart() {
        long testStart = 1690000000000L;
        event.setRegistrationStart(testStart);
        assertEquals(testStart, event.getRegistrationStart());
    }

    /**
     * Verifies that setting and getting registration end time works correctly.
     */
    @Test
    public void testRegistrationEnd() {
        long testEnd = 1710000000000L;
        event.setRegistrationEnd(testEnd);
        assertEquals(testEnd, event.getRegistrationEnd());
    }

    /**
     * Verifies that setting and getting event capacity works correctly.
     */
    @Test
    public void testCapacity() {
        event.setCapacity(50);
        assertEquals(50, event.getCapacity());
    }

    /**
     * Verifies that setting and getting draw size (number of winners) works correctly.
     */
    @Test
    public void testDrawSize() {
        event.setDrawSize(10);
        assertEquals(10, event.getDrawSize());
    }

    /**
     * Verifies that registration is not open when the current time is before
     * the registration start time.
     */
    @Test
    public void testRegistrationOpen_WhenBeforeStart() {
        long now = System.currentTimeMillis();
        event.setRegistrationStart(now + 86400000); // Tomorrow
        event.setRegistrationEnd(now + 172800000);  // Day after
        assertFalse(event.isRegistrationOpen());
    }

    /**
     * Verifies that registration is open when the current time falls within
     * the registration window.
     */
    @Test
    public void testRegistrationOpen_WhenDuringRegistration() {
        long now = System.currentTimeMillis();
        event.setRegistrationStart(now - 86400000); // Yesterday
        event.setRegistrationEnd(now + 86400000);   // Tomorrow
        assertTrue(event.isRegistrationOpen());
    }

    /**
     * Verifies that registration is not open when the current time is after
     * the registration end time.
     */
    @Test
    public void testRegistrationOpen_WhenAfterEnd() {
        long now = System.currentTimeMillis();
        event.setRegistrationStart(now - 172800000); // Two days ago
        event.setRegistrationEnd(now - 86400000);    // Yesterday
        assertFalse(event.isRegistrationOpen());
    }

    /**
     * Verifies that the parameterized Event constructor correctly initializes
     * name, description, and organizer device ID fields.
     */
    @Test
    public void testConstructorWithParameters() {
        Event newEvent = new Event("Test Event", "Description", "org123");
        assertEquals("Test Event", newEvent.getName());
        assertEquals("Description", newEvent.getDescription());
        assertEquals("org123", newEvent.getOrganizerDeviceId());
        assertNull(newEvent.getPosterUrl());
    }

    /**
     * Verifies that toMap() serializes all event properties correctly
     * for Firestore storage.
     */
    @Test
    public void testToMap() {
        event.setId("event123");
        event.setName("Test Event");
        event.setDescription("Description");
        event.setOrganizerDeviceId("org123");
        event.setCategory("Category");
        event.setLocation("Location");
        event.setEventDate(1700000000000L);
        event.setRegistrationStart(1690000000000L);
        event.setRegistrationEnd(1710000000000L);
        event.setCapacity(100);
        event.setDrawSize(20);

        var map = event.toMap();

        assertEquals("Test Event", map.get("name"));
        assertEquals("Description", map.get("description"));
        assertEquals("org123", map.get("organizerDeviceId"));
        assertEquals("Category", map.get("category"));
        assertEquals("Location", map.get("location"));
        assertEquals(1700000000000L, map.get("eventDate"));
        assertEquals(1690000000000L, map.get("registrationStart"));
        assertEquals(1710000000000L, map.get("registrationEnd"));
        assertEquals(100, map.get("capacity"));
        assertEquals(20, map.get("drawSize"));
    }
}
