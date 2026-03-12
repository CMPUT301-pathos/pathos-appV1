package com.example.eventlottery.service;

import android.net.Uri;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Service for managing event poster images.
 *
 * Responsibilities:
 * - Upload poster image to Firebase Storage
 * - Replace/update poster image for an event
 * - Delete poster image
 * - Update poster URL reference in Firestore
 *
 * User stories supported:
 * - US 02.04.02: Organizer can update an event poster
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class PosterService {

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private static final String EVENTS_COLLECTION = "events";
    private static final String POSTER_FOLDER = "event_posters/";

    public PosterService() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public interface PosterCallback {
        void onSuccess(String posterUrl);
        void onFailure(Exception e);
    }

    /**
     * Uploads a new poster image and updates the event document in Firestore.
     */
    public void updatePoster(String eventId, String organizerDeviceId, Uri imageUri,
                             PosterCallback callback) {
        String fileName = POSTER_FOLDER + organizerDeviceId + "_" + eventId + "_"
                + System.currentTimeMillis() + ".jpg";
        StorageReference posterRef = storage.getReference().child(fileName);

        posterRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        if (e != null) throw e;
                    }
                    return posterRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    String posterUrl = uri.toString();
                    db.collection(EVENTS_COLLECTION)
                            .document(eventId)
                            .update("posterUrl", posterUrl)
                            .addOnSuccessListener(unused -> callback.onSuccess(posterUrl))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes the poster from an event (sets posterUrl to null in Firestore).
     */
    public void deletePoster(String eventId, PosterCallback callback) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .update("posterUrl", null)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}
