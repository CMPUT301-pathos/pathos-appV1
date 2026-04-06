package com.example.eventlottery;

import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for core domain logic across multiple user stories.
 *
 * User stories covered:
 * - US 01.05.02: Entrant accepts or declines an invitation from the waiting list
 * - US 01.02.02: Entrant can update their personal profile information
 * - US 01.07.01: Entrant is identified by their device
 *
 * @author Fawaz Mansoor, Hasrat Singh Chauhan
 * @version 1.2
 * @see WaitListRecord
 * @see UserProfile
 */

public class ExampleUnitTest {

    /**
     * Verifies basic arithmetic addition as a sanity check.
     */
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    // US 01.05.02 - Entrant can accept an invitation
    /**
     * Verifies that acceptInvitation() on an INVITED record transitions
     * the status to ACCEPTED.
     */
    @Test
    public void testAcceptInvitation_changesStatusToAccepted() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.acceptInvitation();
        assertEquals(WaitStatus.ACCEPTED, record.getStatus());
    }

    /**
     * Verifies that acceptInvitation() on a non-INVITED record
     * (e.g., WAITING status) throws IllegalStateException.
     */
    @Test
    public void testAcceptInvitation_failsIfNotInvited() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        // Status is WAITING by default, not INVITED
        assertThrows(IllegalStateException.class, () -> {
            record.acceptInvitation();
        });
    }

    /**
     * Verifies that a new WaitListRecord is created with WAITING status by default.
     */
    @Test
    public void testNewRecord_defaultStatusIsWaiting() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        assertEquals(WaitStatus.WAITING, record.getStatus());
    }

    /**
     * Verifies that declineInvitation() on an INVITED record transitions
     * the status to DECLINED.
     */
    @Test
    public void testDeclineInvitation_changesStatusToDeclined() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        assertEquals(WaitStatus.DECLINED, record.getStatus());
    }

    // ===== US 01.02.02 - Edit Profile Tests =====
// Added by Hasrat for Edit Profile functionality

    /**
     * Verifies that a UserProfile's name can be updated via the setter.
     */
    @Test
    public void testUserProfile_updateName_updatesSuccessfully() {
        UserProfile profile = new UserProfile("device123", "Old Name", "old@email.com", "555-9999", "entrant");
        profile.setName("New Name");
        assertEquals("New Name", profile.getName());
    }

    /**
     * Verifies that a UserProfile's email can be updated via the setter.
     */
    @Test
    public void testUserProfile_updateEmail_updatesSuccessfully() {
        UserProfile profile = new UserProfile("device123", "John", "old@email.com", "555-9999", "entrant");
        profile.setEmail("new@email.com");
        assertEquals("new@email.com", profile.getEmail());
    }

    /**
     * Verifies that a UserProfile's phone number can be updated via the setter.
     */
    @Test
    public void testUserProfile_updatePhone_updatesSuccessfully() {
        UserProfile profile = new UserProfile("device123", "John", "john@email.com", "555-9999", "entrant");
        profile.setPhoneNumber("555-0000");
        assertEquals("555-0000", profile.getPhoneNumber());
    }

    /**
     * Verifies that multiple fields of a UserProfile can be updated
     * simultaneously and all changes are reflected.
     */
    @Test
    public void testUserProfile_updateMultipleFields_updatesAll() {
        UserProfile profile = new UserProfile("device123", "John", "john@email.com", "555-9999", "entrant");

        profile.setName("Jane Doe");
        profile.setEmail("jane@email.com");
        profile.setPhoneNumber("555-1234");

        assertEquals("Jane Doe", profile.getName());
        assertEquals("jane@email.com", profile.getEmail());
        assertEquals("555-1234", profile.getPhoneNumber());
    }

    /**
     * Verifies that the five-parameter UserProfile constructor correctly
     * initializes all fields including the device ID and role.
     */
    @Test
    public void testUserProfile_constructorWithRole_setsCorrectValues() {
        UserProfile profile = new UserProfile("device123", "John", "john@email.com", "555-9999", "entrant");

        assertEquals("device123", profile.getDeviceId());
        assertEquals("John", profile.getName());
        assertEquals("john@email.com", profile.getEmail());
        assertEquals("555-9999", profile.getPhoneNumber());
        assertEquals("entrant", profile.getRole());
    }

    /**
     * Verifies that the four-parameter UserProfile constructor defaults
     * the role to "entrant".
     */
    @Test
    public void testUserProfile_defaultRoleIsEntrant() {
        // Using constructor that defaults to entrant
        UserProfile profile = new UserProfile("device123", "John", "john@email.com", "555-9999");
        assertEquals("entrant", profile.getRole());
    }


}