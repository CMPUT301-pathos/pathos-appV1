package com.example.eventlottery.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;

/**
 * Service for managing event poster images using Base64 encoding stored in Firestore.
 *
 * Responsibilities:
 * - Encode poster image to Base64 string
 * - Store/update poster data in Firestore event document
 * - Delete poster from event document
 *
 * User stories supported:
 * - US 02.04.02: Organizer can update an event poster
 *
 * @author Fawaz Mansoor
 * @version 2.0
 */
public class PosterService {

    private final FirebaseFirestore db;
    private static final String EVENTS_COLLECTION = "events";
    private static final int MAX_WIDTH = 600;
    private static final int JPEG_QUALITY = 70;

    /**
     * Creates the poster service backed by the default Firestore instance.
     */
    public PosterService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface PosterCallback {
        /**
         * Called when the poster operation succeeds.
         *
         * @param posterUrl the stored poster URL, or null after deletion
         */
        void onSuccess(String posterUrl);

        /**
         * Called when the poster operation fails.
         *
         * @param e exception describing the failure
         */
        void onFailure(Exception e);
    }

    /**
     * Encodes the image as Base64 and updates the event document in Firestore.
     */
    public void updatePoster(String eventId, String organizerDeviceId, Uri imageUri,
                             Context context, PosterCallback callback) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), imageUri);

            // Scale down to stay under Firestore 1MB document limit
            if (bitmap.getWidth() > MAX_WIDTH) {
                float scale = (float) MAX_WIDTH / bitmap.getWidth();
                int newHeight = Math.round(bitmap.getHeight() * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, MAX_WIDTH, newHeight, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            String posterData = "data:image/jpeg;base64," + base64Image;

            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .update("posterUrl", posterData)
                    .addOnSuccessListener(unused -> callback.onSuccess(posterData))
                    .addOnFailureListener(callback::onFailure);

        } catch (Exception e) {
            callback.onFailure(e);
        }
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
