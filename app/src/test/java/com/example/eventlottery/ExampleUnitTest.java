package com.example.eventlottery;

import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    // US 01.05.02 - Entrant can accept an invitation
    @Test
    public void testAcceptInvitation_changesStatusToAccepted() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.acceptInvitation();
        assertEquals(WaitStatus.ACCEPTED, record.getStatus());
    }

    @Test
    public void testAcceptInvitation_failsIfNotInvited() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        // Status is WAITING by default, not INVITED
        assertThrows(IllegalStateException.class, () -> {
            record.acceptInvitation();
        });
    }

    @Test
    public void testNewRecord_defaultStatusIsWaiting() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        assertEquals(WaitStatus.WAITING, record.getStatus());
    }

    @Test
    public void testDeclineInvitation_changesStatusToDeclined() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        assertEquals(WaitStatus.DECLINED, record.getStatus());
    }

    // ===== US 01.02.02 - Edit Profile Tests =====
// Added by Hasrat for Edit Profile functionality

    @Test
    public void testUserProfile_updateName_updatesSuccessfully() {
        UserProfile profile = new UserProfile("device123", "Old Name", "old@email.com", "555-9999", "entrant");
        profile.setName("New Name");
        assertEquals("New Name", profile.getName());
    }

    @Test
    public void testUserProfile_updateEmail_updatesSuccessfully() {
        UserProfile profile = new UserProfile("device123", "John", "old@email.com", "555-9999", "entrant");
        profile.setEmail("new@email.com");
        assertEquals("new@email.com", profile.getEmail());
    }

    @Test
    public void testUserProfile_updatePhone_updatesSuccessfully() {
        UserProfile profile = new UserProfile("device123", "John", "john@email.com", "555-9999", "entrant");
        profile.setPhoneNumber("555-0000");
        assertEquals("555-0000", profile.getPhoneNumber());
    }

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

    @Test
    public void testUserProfile_constructorWithRole_setsCorrectValues() {
        UserProfile profile = new UserProfile("device123", "John", "john@email.com", "555-9999", "entrant");

        assertEquals("device123", profile.getDeviceId());
        assertEquals("John", profile.getName());
        assertEquals("john@email.com", profile.getEmail());
        assertEquals("555-9999", profile.getPhoneNumber());
        assertEquals("entrant", profile.getRole());
    }

    @Test
    public void testUserProfile_defaultRoleIsEntrant() {
        // Using constructor that defaults to entrant
        UserProfile profile = new UserProfile("device123", "John", "john@email.com", "555-9999");
        assertEquals("entrant", profile.getRole());
    }


}