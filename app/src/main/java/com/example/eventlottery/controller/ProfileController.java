package com.example.eventlottery.controller;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.domain.UserProfile;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for managing user profile operations.
 * Acts as an intermediary between the UI and repositories.
 *
 * Supports:
 * - profile retrieval and updates
 * - profile deletion
 * - notification preference toggling
 * - deletion of organizer-owned events during account deletion
 *
 * User stories supported:
 * - US 01.02.01 / US 01.02.02: Provide/update personal info
 * - US 01.02.04: Delete profile
 * - US 01.04.03: Opt out of receiving notifications
 *
 * @author Dmitriy Limanets, Fawaz Mansoor, Kenneth Joseph
 * @version 1.3
 * @see ProfileRepository
 * @see EventRepository
 * @see UserProfile
 */
public class ProfileController {

    private final ProfileRepository profileRepository;
    private final EventRepository eventRepository;

    /**
     * Creates a new ProfileController with the given repositories.
     *
     * @param profileRepository repository for profile operations
     * @param eventRepository repository for event operations
     */
    public ProfileController(ProfileRepository profileRepository, EventRepository eventRepository) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Retrieves a user profile by device ID.
     *
     * @param deviceId the unique device identifier
     * @param callback the callback to handle success or failure
     */
    public void getProfile(String deviceId, ProfileRepository.ProfileCallback callback) {
        profileRepository.getProfile(deviceId, callback);
    }

    /**
     * Saves a user profile.
     *
     * @param profile the user profile to save
     * @param callback the callback to handle success or failure
     */
    public void saveProfile(UserProfile profile, ProfileRepository.ProfileCallback callback) {
        if (profile != null) {
            profile.refreshProfileCompleted();
        }
        profileRepository.saveProfile(profile, callback);
    }

    /**
     * Deletes a user profile by device ID only.
     *
     * @param deviceId the unique device identifier of the profile to delete
     * @param callback the callback to handle success or failure
     */
    public void deleteProfile(String deviceId, ProfileRepository.ProfileCallback callback) {
        profileRepository.deleteProfile(deviceId, callback);
    }

    /**
     * Deletes the user profile and all events organized by that user.
     *
     * Flow:
     * - find events owned by the organizer device ID
     * - delete each owned event
     * - delete the profile last
     *
     * @param deviceId the unique organizer device identifier
     * @param callback the callback to handle success or failure
     */
    public void deleteProfileAndOwnedEvents(String deviceId, ProfileRepository.ProfileCallback callback) {
        eventRepository.getEventsByOrganizer(deviceId, new EventRepository.ListCallback() {
            @Override
            public void onSuccess(List<EventSummary> events) {
                if (events == null || events.isEmpty()) {
                    profileRepository.deleteProfile(deviceId, callback);
                    return;
                }

                AtomicInteger remaining = new AtomicInteger(events.size());
                final boolean[] failed = {false};

                for (EventSummary event : events) {
                    eventRepository.deleteEvent(event.getId(), new EventRepository.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (remaining.decrementAndGet() == 0 && !failed[0]) {
                                profileRepository.deleteProfile(deviceId, callback);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (!failed[0]) {
                                failed[0] = true;
                                callback.onFailure(e);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Updates an existing user profile or creates a new one.
     *
     * @param deviceId the unique device identifier
     * @param name the updated name
     * @param email the updated email
     * @param phone the updated phone number
     * @param callback the callback to handle success or failure
     */
    public void updateProfile(String deviceId, String name, String email, String phone,
                              ProfileRepository.ProfileCallback callback) {
        getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile != null) {
                    profile.setDeviceId(deviceId);
                    profile.setName(name);
                    profile.setEmail(email);
                    profile.setPhoneNumber(phone);
                    profile.refreshProfileCompleted();
                    saveProfile(profile, callback);
                } else {
                    UserProfile newProfile = new UserProfile(deviceId, name, email, phone, "user");
                    newProfile.refreshProfileCompleted();
                    saveProfile(newProfile, callback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Updates the profile photo URI for a user.
     *
     * @param deviceId the unique device identifier
     * @param photoUri the URI string of the selected photo
     * @param callback the callback to handle success or failure
     */
    public void updateProfilePhoto(String deviceId, String photoUri,
                                   ProfileRepository.ProfileCallback callback) {
        getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile != null) {
                    profile.setProfilePhotoUri(photoUri);
                    saveProfile(profile, callback);
                } else {
                    callback.onFailure(
                            new Exception("Profile not found for deviceId: " + deviceId));
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Toggles notification preferences for a user profile.
     *
     * @param deviceId the unique device identifier
     * @param notificationsEnabled true to opt in, false to opt out
     * @param callback the callback to handle success or failure
     */
    public void setNotificationsEnabled(String deviceId, boolean notificationsEnabled,
                                        ProfileRepository.ProfileCallback callback) {
        getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile != null) {
                    profile.setNotificationsEnabled(notificationsEnabled);
                    saveProfile(profile, callback);
                } else {
                    callback.onFailure(new Exception("Profile not found for deviceId: " + deviceId));
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}