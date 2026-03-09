package com.example.eventlottery;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for US 01.02.02 (Update Profile).
 *
 * These tests should validate:
 * 1) updateProfile sends the correct data to the repository
 * 2) phone is optional
 *
 * IMPORTANT: You must adapt the method calls to match your ProfileController/ProfileRepository signatures.
 * Once you re-upload ProfileController.java + ProfileRepository.java, I can rewrite this to compile exactly.
 */
public class ProfileUpdateControllerTest {

    // ---- Fake objects (no Firebase) ----

    static class FakeProfileRepository /* implements ProfileRepository */ {
        String lastDeviceId;
        String lastName;
        String lastEmail;
        String lastPhone;

        boolean updateCalled = false;

        // TODO: rename this method to match YOUR repository method signature
        void updateProfile(String deviceId, String name, String email, String phone) {
            updateCalled = true;
            lastDeviceId = deviceId;
            lastName = name;
            lastEmail = email;
            lastPhone = phone;
        }
    }

    @Test
    public void updateProfile_validInput_callsRepositoryWithCorrectFields() {
        FakeProfileRepository repo = new FakeProfileRepository();

        // TODO: create your controller using your repo
        // Example: ProfileController controller = new ProfileController(repo);

        String deviceId = "device_123";
        String name = "Kenneth Joseph";
        String email = "ken@example.com";
        String phone = "7801234567";

        // TODO: call your controller update method here
        // Example: controller.updateProfile(deviceId, name, email, phone, callback);

        // Simulate repository call for now (replace with controller call above)
        repo.updateProfile(deviceId, name, email, phone);

        assertTrue(repo.updateCalled);
        assertEquals(deviceId, repo.lastDeviceId);
        assertEquals(name, repo.lastName);
        assertEquals(email, repo.lastEmail);
        assertEquals(phone, repo.lastPhone);
    }

    @Test
    public void updateProfile_allowsEmptyPhone() {
        FakeProfileRepository repo = new FakeProfileRepository();

        String deviceId = "device_123";
        String name = "Kenneth Joseph";
        String email = "ken@example.com";
        String phone = ""; // optional

        // Replace with controller call once wired
        repo.updateProfile(deviceId, name, email, phone);

        assertTrue(repo.updateCalled);
        assertEquals("", repo.lastPhone);
    }
}