package com.example.eventlottery.controller;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
/**
 * Controller for managing user profile operations.
 * Acts as an intermediary between the UI and the ProfileRepository.
 *
 * @author Dmitriy Limanets
 * @version 1.0
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
     * Saves a user profile.
     *
     * @param profile  the user profile to save
     * @param callback the callback to handle success or failure
     */
    public void saveProfile(UserProfile profile, ProfileRepository.ProfileCallback callback) {
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
     * author : hasrat
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
        // First try to get existing profile
        getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile != null) {
                    // Update existing profile
                    profile.setName(name);
                    profile.setEmail(email);
                    profile.setPhoneNumber(phone);
                    saveProfile(profile, callback);
                } else {
                    // Create new profile if it doesn't exist
                    UserProfile newProfile = new UserProfile(deviceId, name, email, phone, "entrant");
                    saveProfile(newProfile, callback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}
