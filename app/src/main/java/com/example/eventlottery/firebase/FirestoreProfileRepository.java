package com.example.eventlottery.firebase;

import android.util.Log;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirestoreProfileRepository implements ProfileRepository {
    
    private static final String TAG = "FirestoreProfileRepo";
    private final FirebaseFirestore db;
    private static final String USERS_COLLECTION = "users";
    
    public FirestoreProfileRepository() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    @Override
    public void getProfile(String deviceId, ProfileCallback callback) {
        if (deviceId == null || deviceId.isEmpty()) {
            callback.onFailure(new Exception("Device ID is null or empty"));
            return;
        }
        
        db.collection(USERS_COLLECTION)
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            profile.setDeviceId(deviceId);
                        }
                        callback.onSuccess(profile);
                    } else {
                        // User doesn't exist yet - return null
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
    
    @Override
    public void saveProfile(UserProfile profile, ProfileCallback callback) {
        if (profile == null) {
            callback.onFailure(new Exception("Profile is null"));
            return;
        }
        
        String deviceId = profile.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            callback.onFailure(new Exception("Device ID is null or empty"));
            return;
        }
        
        db.collection(USERS_COLLECTION)
                .document(deviceId)
                .set(profile)
                .addOnSuccessListener(aVoid -> callback.onSuccess(profile))
                .addOnFailureListener(callback::onFailure);
    }
    
    @Override
    public void deleteProfile(String deviceId, ProfileCallback callback) {
        if (deviceId == null || deviceId.isEmpty()) {
            callback.onFailure(new Exception("Device ID is null or empty"));
            return;
        }
        
        db.collection(USERS_COLLECTION)
                .document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
    
    public interface SearchCallback {
        void onSuccess(List<UserProfile> profiles);
        void onFailure(Exception e);
    }
    
    public void searchProfiles(String query, SearchCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        
        String searchTerm = query.trim().toLowerCase();
        
        // Search by name or email
        db.collection(USERS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> results = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserProfile profile = doc.toObject(UserProfile.class);
                        if (profile != null) {
                            String name = profile.getName() != null ? profile.getName().toLowerCase() : "";
                            String email = profile.getEmail() != null ? profile.getEmail().toLowerCase() : "";
                            if (name.contains(searchTerm) || email.contains(searchTerm)) {
                                profile.setDeviceId(doc.getId());
                                results.add(profile);
                            }
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
