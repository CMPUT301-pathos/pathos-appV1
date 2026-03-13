package com.example.eventlottery.controller;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
/**
 * Controller for managing user profile operations.
 * Acts as an intermediary between the UI and the ProfileRepository.
 *
 * Extended to support notification preference toggling.
 *
 * User stories supported:
 * - US 01.02.01/US 01.02.02: Provide/update personal info
 * - US 01.02.04: Delete profile
 * - US 01.04.03: Opt out of receiving notifications
 *
 * @author Dmitriy Limanets, Fawaz Mansoor, Kenneth Joseph
 * @version 1.2
 * @see ProfileRepository
 * @see UserProfile
 */

public class ProfileController {
    private final ProfileRepository profileRepository;
    /**
     * Creates a new ProfileController with the given repository.
     *
     * @param profileRepository the repository to use for profile operations
     */

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
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
     * author: Kenneth
     * Saves a user profile.
     *
     * @param profile  the user profile to save
     * @param callback the callback to handle success or failure
     */
    public void saveProfile(UserProfile profile, ProfileRepository.ProfileCallback callback) {
        if (profile != null) {
            profile.refreshProfileCompleted();
        }
        profileRepository.saveProfile(profile, callback);
    }

    /**
     * Deletes a user profile by device ID.
     *
     * @param deviceId the unique device identifier of the profile to delete
     * @param callback the callback to handle success or failure
     */
    public void deleteProfile(String deviceId, ProfileRepository.ProfileCallback callback) {
        profileRepository.deleteProfile(deviceId, callback);
    }
    /**
     * author : hasrat, Kenneth
     * Verison 1.2
     * Updates an existing user profile or creates a new one.
     * This method supports US 01.02.02 - Update profile information.
     * in conjunction with the Editprofile Fragment
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
                    // Update existing profile
                    profile.setDeviceId(deviceId);
                    profile.setName(name);
                    profile.setEmail(email);
                    profile.setPhoneNumber(phone);
                    profile.refreshProfileCompleted();   // important
                    saveProfile(profile, callback);
                } else {
                    // Create new profile if it doesn't exist
                    UserProfile newProfile = new UserProfile(deviceId, name, email, phone, "entrant");
                    newProfile.refreshProfileCompleted(); // important
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
     * Toggles notification preferences for a user profile.
     * Supports US 01.04.03 - Opt out of receiving notifications.
     *
     * @param deviceId             the unique device identifier
     * @param notificationsEnabled true to opt in, false to opt out
     * @param callback             the callback to handle success or failure
     */
//    public void setNotificationsEnabled(String deviceId, boolean notificationsEnabled,
//                                        ProfileRepository.ProfileCallback callback) {
//        getProfile(deviceId, new ProfileRepository.ProfileCallback() {
//            @Override
//            public void onSuccess(UserProfile profile) {
//                if (profile != null) {
//                    profile.setNotificationsEnabled(notificationsEnabled);
//                    saveProfile(profile, callback);
//                } else {
//                    callback.onFailure(new Exception("Profile not found for deviceId: " + deviceId));
//                }
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                callback.onFailure(e);
//            }
//        });
//    }
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
