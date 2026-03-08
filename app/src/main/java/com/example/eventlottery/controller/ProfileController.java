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

}
