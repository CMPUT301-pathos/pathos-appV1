package com.example.eventlottery.data.memory;
import com.example.eventlottery.domain.UserProfile;

/**
 * Interface for managing user profile data operations.
 * Defines methods for retrieving, saving, and deleting user profiles.
 *
 * @author Dmitriy Limanets
 * @version 1.0
 * @see UserProfile
 * @see com.example.eventlottery.firebase.FirestoreProfileRepository
 */




public interface ProfileRepository {
    /**
     * Retrieves a user profile by device ID.
     *
     * @param deviceId the unique device identifier
     * @param callback the callback to handle success or failure
     */
    void getProfile(String deviceId, ProfileCallback callback);

    /**
     * Saves a user profile to the database.
     *
     * @param profile  the user profile to save
     * @param callback the callback to handle success or failure
     */
    void saveProfile(UserProfile profile, ProfileCallback callback);

    /**
     * Deletes a user profile from the database.
     *
     * @param deviceId the unique device identifier of the profile to delete
     * @param callback the callback to handle success or failure
     */
    void deleteProfile(String deviceId, ProfileCallback callback);
    /**
     * Callback interface for handling asynchronous profile operations.
     */
    interface ProfileCallback {
        /**
         * Called when the operation completes successfully.
         *
         * @param profile the resulting user profile, or null for delete operations
         */
        void onSuccess(UserProfile profile);
        /**
         * Called when the operation fails.
         *
         * @param e the exception that caused the failure
         */
        void onFailure(Exception e);
    }
}
