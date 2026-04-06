package com.example.eventlottery.data;

import com.example.eventlottery.domain.UserProfile;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Contract-style unit test for {@link ProfileRepository} behavior.
 *
 * We don't hit Firestore here (unit tests should be fast and offline).
 * Instead we test expected repository semantics using an in-memory fake:
 * - saveProfile upserts
 * - getProfile returns saved profile or null
 * - deleteProfile removes profile
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class ProfileRepositoryContractTest {

    /**
     * Simple in-memory fake repository for contract testing.
     */
    private static class InMemoryProfileRepository implements ProfileRepository {

        private final Map<String, UserProfile> store = new HashMap<>();

        @Override
        public void getProfile(String deviceId, ProfileCallback callback) {
            callback.onSuccess(store.get(deviceId));
        }

        @Override
        public void saveProfile(UserProfile profile, ProfileCallback callback) {
            store.put(profile.getDeviceId(), profile);
            callback.onSuccess(profile);
        }

        @Override
        public void deleteProfile(String deviceId, ProfileCallback callback) {
            store.remove(deviceId);
            callback.onSuccess(null);
        }
    }

    /**
     * Verifies that a profile saved to the repository can be retrieved
     * with all original fields intact.
     */
    @Test
    public void saveThenGet_returnsProfile() {
        ProfileRepository repo = new InMemoryProfileRepository();

        UserProfile profile = new UserProfile("device_1", "Ken", "k@example.com", "555");

        repo.saveProfile(profile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                // after save, get should return same object data
                repo.getProfile("device_1", new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(UserProfile loaded) {
                        assertNotNull(loaded);
                        assertEquals("device_1", loaded.getDeviceId());
                        assertEquals("Ken", loaded.getName());
                        assertEquals("k@example.com", loaded.getEmail());
                        assertEquals("555", loaded.getPhoneNumber());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("getProfile should not fail in memory repo");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("saveProfile should not fail in memory repo");
            }
        });
    }

    /**
     * Verifies that deleting a profile removes it from the repository
     * and subsequent retrieval returns null.
     */
    @Test
    public void delete_removesProfile() {
        ProfileRepository repo = new InMemoryProfileRepository();

        UserProfile profile = new UserProfile("device_2", "Delete Me", "d@example.com", "");
        repo.saveProfile(profile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                repo.deleteProfile("device_2", new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(UserProfile ignored) {
                        repo.getProfile("device_2", new ProfileRepository.ProfileCallback() {
                            @Override
                            public void onSuccess(UserProfile loaded) {
                                assertNull(loaded);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("getProfile should not fail in memory repo");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("deleteProfile should not fail in memory repo");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("saveProfile should not fail in memory repo");
            }
        });
    }

    /**
     * Verifies that saving a profile with the same device ID overwrites
     * the previous profile (upsert behavior).
     */
    @Test
    public void saveProfile_isUpsert_overwritesExisting() {
        ProfileRepository repo = new InMemoryProfileRepository();

        UserProfile p1 = new UserProfile("device_3", "First", "first@example.com", "");
        UserProfile p2 = new UserProfile("device_3", "Second", "second@example.com", "");

        repo.saveProfile(p1, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                repo.saveProfile(p2, new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(UserProfile ignored) {
                        repo.getProfile("device_3", new ProfileRepository.ProfileCallback() {
                            @Override
                            public void onSuccess(UserProfile loaded) {
                                assertNotNull(loaded);
                                assertEquals("Second", loaded.getName());
                                assertEquals("second@example.com", loaded.getEmail());
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("getProfile should not fail");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("saveProfile should not fail");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("saveProfile should not fail");
            }
        });
    }
}