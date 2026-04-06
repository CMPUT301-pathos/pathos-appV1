package com.example.eventlottery.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.eventlottery.controller.ProfileController;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.domain.UserProfile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for US 01.02.04 – Delete profile.
 */
public class ProfileDeleteTest {

    private FakeProfileRepository fakeProfileRepo;
    private FakeEventRepository fakeEventRepo;
    private ProfileController controller;

    private static class FakeProfileRepository implements ProfileRepository {
        private final Map<String, UserProfile> profiles = new HashMap<>();

        void addProfile(UserProfile profile) {
            profiles.put(profile.getDeviceId(), profile);
        }

        @Override
        public void getProfile(String deviceId, ProfileCallback callback) {
            callback.onSuccess(profiles.get(deviceId));
        }

        @Override
        public void saveProfile(UserProfile profile, ProfileCallback callback) {
            profiles.put(profile.getDeviceId(), profile);
            callback.onSuccess(profile);
        }

        @Override
        public void deleteProfile(String deviceId, ProfileCallback callback) {
            profiles.remove(deviceId);
            callback.onSuccess(null);
        }

        boolean contains(String deviceId) {
            return profiles.containsKey(deviceId);
        }
    }

    private static class FakeEventRepository implements EventRepository {
        private final List<EventSummary> events = new ArrayList<>();
        private final List<String> deletedEventIds = new ArrayList<>();

        void addEvent(EventSummary event) {
            events.add(event);
        }

        @Override
        public void createEvent(Object event, CreateCallback callback) {}

        @Override
        public void getAllEvents(ListCallback callback) {
            callback.onSuccess(new ArrayList<>(events));
        }

        @Override
        public void getEventsByOrganizer(String organizerDeviceId, ListCallback callback) {
            List<EventSummary> result = new ArrayList<>();
            for (EventSummary e : events) {
                if (e.getOrganizerDeviceId().equals(organizerDeviceId)) {
                    result.add(e);
                }
            }
            callback.onSuccess(result);
        }

        @Override
        public void getManageableEvents(String organizerDeviceId, ListCallback callback) {
            getEventsByOrganizer(organizerDeviceId, callback);
        }

        @Override
        public void deleteEvent(String eventId, OperationCallback callback) {
            events.removeIf(e -> e.getId().equals(eventId));
            deletedEventIds.add(eventId);
            callback.onSuccess();
        }

        boolean wasDeleted(String eventId) {
            return deletedEventIds.contains(eventId);
        }
    }

    @Before
    public void setUp() {
        fakeProfileRepo = new FakeProfileRepository();
        fakeEventRepo = new FakeEventRepository();
        controller = new ProfileController(fakeProfileRepo, fakeEventRepo);
    }

    /**
     * Verifies that deleteProfile() removes the profile from the repository.
     */
    @Test
    public void deleteProfile_removesProfileFromRepository() {
        UserProfile profile = new UserProfile("device1", "Test User", "test@example.com", "123", "user");
        fakeProfileRepo.addProfile(profile);

        assertTrue(fakeProfileRepo.contains("device1"));

        controller.deleteProfile("device1", new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                assertFalse(fakeProfileRepo.contains("device1"));
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that deleteProfile() for a non-existent device succeeds
     * without error and returns null.
     */
    @Test
    public void deleteProfile_nonExistent_stillSucceeds() {
        controller.deleteProfile("no_such_device", new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                assertNull(p);
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that deleteProfileAndOwnedEvents() deletes all events owned by
     * the organizer before deleting the profile itself.
     */
    @Test
    public void deleteProfileAndOwnedEvents_deletesEventsFirst_thenProfile() {
        UserProfile profile = new UserProfile("org1", "Organizer", "org@example.com", "", "organizer");
        fakeProfileRepo.addProfile(profile);

        long now = System.currentTimeMillis();
        EventSummary event1 = new EventSummary("e1", "Event 1", "desc", "Edmonton",
                now, "org1", "Sports", now + 100000, now - 100000, now + 50000, 50, 10, null);
        EventSummary event2 = new EventSummary("e2", "Event 2", "desc", "Edmonton",
                now, "org1", "Music", now + 100000, now - 100000, now + 50000, 30, 5, null);

        fakeEventRepo.addEvent(event1);
        fakeEventRepo.addEvent(event2);

        controller.deleteProfileAndOwnedEvents("org1", new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                assertTrue(fakeEventRepo.wasDeleted("e1"));
                assertTrue(fakeEventRepo.wasDeleted("e2"));
                assertFalse(fakeProfileRepo.contains("org1"));
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }

    /**
     * Verifies that deleteProfileAndOwnedEvents() for a user with no owned events
     * deletes only the profile.
     */
    @Test
    public void deleteProfileAndOwnedEvents_noEvents_deletesProfileOnly() {
        UserProfile profile = new UserProfile("device2", "User", "user@example.com", "", "user");
        fakeProfileRepo.addProfile(profile);

        controller.deleteProfileAndOwnedEvents("device2", new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                assertFalse(fakeProfileRepo.contains("device2"));
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should not fail: " + e.getMessage());
            }
        });
    }
}