package com.example.eventlottery.domain;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link UserProfile}.
 *
 * Tests the model behavior required for:
 * - US 01.02.01 (Profile fields are stored correctly)
 * - US 01.07.01 (Device-based identity stored in profile)
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class UserProfileTest {

    /**
     * Verifies that the default constructor creates an instance that allows
     * setting all profile fields via setters.
     */
    @Test
    public void defaultConstructor_allowsSetters() {
        UserProfile p = new UserProfile();
        p.setDeviceId("device_1");
        p.setName("Kenneth");
        p.setEmail("kenneth@example.com");
        p.setPhoneNumber("123");

        assertEquals("device_1", p.getDeviceId());
        assertEquals("Kenneth", p.getName());
        assertEquals("kenneth@example.com", p.getEmail());
        assertEquals("123", p.getPhoneNumber());
    }

    /**
     * Verifies that the parameterized constructor correctly initializes all fields
     * and defaults the role to "entrant".
     */
    @Test
    public void constructor_defaultRoleIsEntrant() {
        UserProfile p = new UserProfile("device_123", "Test User", "test@example.com", "555");

        assertEquals("device_123", p.getDeviceId());
        assertEquals("Test User", p.getName());
        assertEquals("test@example.com", p.getEmail());
        assertEquals("555", p.getPhoneNumber());

        // Default behavior in your UserProfile implementation:
        assertEquals("entrant", p.getRole());
    }

    /**
     * Verifies that the five-parameter constructor correctly initializes all fields
     * including the role parameter, for both organizer and admin roles.
     */
    @Test
    public void constructor_withRole_setsRoleCorrectly() {
        UserProfile organizer = new UserProfile("dev_org", "Org", "org@example.com", "", "organizer");
        UserProfile admin = new UserProfile("dev_admin", "Admin", "admin@example.com", "", "admin");

        assertEquals("organizer", organizer.getRole());
        assertEquals("admin", admin.getRole());
    }

    /**
     * Verifies that getEventHistory() never returns null, always returning
     * an empty list for new profiles.
     */
    @Test
    public void eventHistory_getEventHistory_neverNull() {
        UserProfile p = new UserProfile("device_9", "History User", "h@example.com", "");
        assertNotNull(p.getEventHistory());
        assertEquals(0, p.getEventHistory().size());
    }

//    @Test
//    public void eventHistory_addToHistory_addsToFront() {
//        UserProfile p = new UserProfile("device_9", "History User", "h@example.com", "");
//
//        EventHistoryRecord r1 = new EventHistoryRecord("event1", "Event One", "WIN");
//        EventHistoryRecord r2 = new EventHistoryRecord("event2", "Event Two", "LOSE");
//
//        p.addToHistory(r1);
//        p.addToHistory(r2);
//
//        assertEquals(2, p.getEventHistory().size());
//        // Newest goes first (per your implementation)
//        assertEquals("event2", p.getEventHistory().get(0).getEventId());
//        assertEquals("event1", p.getEventHistory().get(1).getEventId());
//    }

//
//    @Test
//    public void eventHistory_clearHistory_emptiesList() {
//        UserProfile p = new UserProfile("device_9", "History User", "h@example.com", "");
//
//        p.addToHistory(new EventHistoryRecord("event1", "Event One", "WIN"));
//        assertTrue(p.getEventHistory().size() > 0);
//
//        p.clearHistory();
//        assertEquals(0, p.getEventHistory().size());
//    }
}