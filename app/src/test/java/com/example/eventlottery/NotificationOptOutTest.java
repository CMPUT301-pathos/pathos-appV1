package com.example.eventlottery;

import com.example.eventlottery.domain.UserProfile;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for notification opt-out logic.
 *
 * User stories covered:
 * - US 01.04.03: Opt out of receiving notifications from organizers and admins
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class NotificationOptOutTest {

    // --- Default state tests ---

    @Test
    public void testNotificationsEnabledByDefault() {
        UserProfile profile = new UserProfile("device1", "Fawaz", "fawaz@email.com", "");
        assertTrue(profile.isNotificationsEnabled());
    }

    // --- Opt out tests ---

    @Test
    public void testOptOut_disablesNotifications() {
        UserProfile profile = new UserProfile("device1", "Fawaz", "fawaz@email.com", "");
        profile.setNotificationsEnabled(false);
        assertFalse(profile.isNotificationsEnabled());
    }

    @Test
    public void testOptIn_enablesNotifications() {
        UserProfile profile = new UserProfile("device1", "Fawaz", "fawaz@email.com", "");
        profile.setNotificationsEnabled(false);
        profile.setNotificationsEnabled(true);
        assertTrue(profile.isNotificationsEnabled());
    }

    @Test
    public void testOptOut_doesNotAffectOtherFields() {
        UserProfile profile = new UserProfile("device1", "Fawaz", "fawaz@email.com", "123456789");
        profile.setNotificationsEnabled(false);
        assertEquals("Fawaz", profile.getName());
        assertEquals("fawaz@email.com", profile.getEmail());
        assertEquals("123456789", profile.getPhoneNumber());
    }

    @Test
    public void testToggleNotifications_multipleToggles() {
        UserProfile profile = new UserProfile("device1", "Fawaz", "fawaz@email.com", "");
        assertTrue(profile.isNotificationsEnabled());
        profile.setNotificationsEnabled(false);
        assertFalse(profile.isNotificationsEnabled());
        profile.setNotificationsEnabled(true);
        assertTrue(profile.isNotificationsEnabled());
        profile.setNotificationsEnabled(false);
        assertFalse(profile.isNotificationsEnabled());
    }
}
