package com.example.eventlottery.firebase;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Firestore implementation of ProfileRepository.
 * Handles reading, saving, and deleting user profiles from Firestore.
 * Profiles are stored in the "users" collection with the device ID as the document key.
 *
 * Stored profile data includes:
 * - deviceId
 * - name
 * - email
 * - phoneNumber
 * - role
 * - profileCompleted
 * - notificationsEnabled
 * - eventHistory
 *
 * @author Dmitriy Limanets, Kenneth Joseph
 * @version 1.1
 * @see ProfileRepository
 * @see UserProfile
 */

public class FirestoreProfileRepository implements ProfileRepository {

    private final FirebaseFirestore db;

    /** Firestore collection name for user profiles. */
    private static final String COLLECTION_NAME = "users";
    /**
     * Creates a new FirestoreProfileRepository with a Firestore database instance.
     */

    public FirestoreProfileRepository(){
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves a user profile from Firestore by device ID.
     * Returns null via callback if the profile does not exist.
     *
     * @param deviceId the unique device identifier
     * @param callback the callback to handle success or failure
     */
    @Override
    public void getProfile(String deviceId, ProfileCallback callback){
        db.collection(COLLECTION_NAME).document(deviceId)
                .get()
                .addOnSuccessListener(document->{
                    if(document.exists()){
                        UserProfile profile = document.toObject(UserProfile.class);
                        callback.onSuccess(profile);
                    }else{
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Saves a user profile to Firestore.
     * Uses the profile's device ID as the document key.
     *
     * @param profile  the user profile to save
     * @param callback the callback to handle success or failure
     */
    @Override
    public void saveProfile(UserProfile profile, ProfileCallback callback){
        if (profile == null || profile.getDeviceId() == null || profile.getDeviceId().trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Profile or deviceId cannot be null/empty"));
            return;
        }

        db.collection(COLLECTION_NAME).document(profile.getDeviceId())
                .set(profile)
                .addOnSuccessListener(aVoid -> callback.onSuccess(profile))
                .addOnFailureListener(callback::onFailure);
    }
    /**
     * Deletes a user profile from Firestore by device ID.
     * Returns null via onSuccess callback upon successful deletion.
     *
     * @param deviceId the unique device identifier of the profile to delete
     * @param callback the callback to handle success or failure
     */
    @Override
    public void deleteProfile(String deviceId, ProfileCallback callback){
        db.collection(COLLECTION_NAME).document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}
