package com.example.eventlottery.admin;

/**
 * Browse/delete images (US 03.06.01, 03.03.01)
 * @author hasratsinghchauhan
 *  * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 */


import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.ui.AdminImageAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for administrators to browse and manage uploaded images.
 * Supports:
 * - US 03.06.01: Browse uploaded images
 * - US 03.03.01: Remove images
 */
/*public class AdminBrowseImages extends AppCompatActivity {

    private RecyclerView recyclerViewImages;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private AdminImageAdapter imageAdapter;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private List<ImageData> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_images);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Browse Images");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        imageList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadImages();
    }

    private void initViews() {
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        imageAdapter = new AdminImageAdapter();
        imageAdapter.setOnImageClickListener(new AdminImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(ImageData image) {
                showImageDetails(image);
            }

            @Override
            public void onDeleteClick(ImageData image) {
                showDeleteConfirmation(image);
            }
        });

        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewImages.setAdapter(imageAdapter);
    }

    private void loadImages() {
        progressBar.setVisibility(View.VISIBLE);
        imageList.clear();

        // Load from event_posters folder
        loadFromStorageFolder("event_posters", "event_poster");
        // Load from profile_pictures folder
        loadFromStorageFolder("profile_pictures", "profile_picture");
    }

    private void loadFromStorageFolder(String folderPath, String imageType) {
        StorageReference folderRef = storage.getReference().child(folderPath);

        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            ImageData imageData = new ImageData(
                                    item.getName(),
                                    uri.toString(),
                                    "unknown",
                                    imageType
                            );
                            imageData.setStoragePath(folderPath + "/" + item.getName());
                            imageList.add(imageData);
                            updateUI();
                        });
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading images: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void showImageDetails(ImageData image) {
        String details = "Type: " + image.getType() + "\n" +
                "File Name: " + image.getImageId() + "\n" +
                "Storage Path: " + image.getStoragePath();

        new AlertDialog.Builder(this)
                .setTitle("Image Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .setNeutralButton("Delete", (dialog, which) -> showDeleteConfirmation(image))
                .show();
    }

    private void showDeleteConfirmation(ImageData image) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Delete this image?\n\nThis cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(image))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage(ImageData image) {
        progressBar.setVisibility(View.VISIBLE);

        StorageReference imageRef = storage.getReference().child(image.getStoragePath());
        imageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                    imageList.remove(image);
                    updateUI();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void updateUI() {
        runOnUiThread(() -> {
            if (imageList.isEmpty()) {
                recyclerViewImages.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
            } else {
                recyclerViewImages.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
                imageAdapter.setImages(new ArrayList<>(imageList));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Simple model class for image data
     */
    /*public static class ImageData {
        private String imageId;
        private String imageUrl;
        private String uploadedBy;
        private String type;
        private String storagePath;

        public ImageData(String imageId, String imageUrl, String uploadedBy, String type) {
            this.imageId = imageId;
            this.imageUrl = imageUrl;
            this.uploadedBy = uploadedBy;
            this.type = type;
        }

        // Getters and setters
        public String getImageId() { return imageId; }
        public String getImageUrl() { return imageUrl; }
        public String getUploadedBy() { return uploadedBy; }
        public String getType() { return type; }
        public String getStoragePath() { return storagePath; }
        public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    }

}*/
/**
 * Browse/delete images (US 03.06.01, 03.03.01)
 * @author hasratsinghchauhan
 */

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.ui.AdminImageAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for administrators to browse and manage uploaded images.
 * Supports:
 * - US 03.06.01: Browse uploaded images
 * - US 03.03.01: Remove images
 *
 * Updated to load images from Firestore (where they're stored as Base64 in events and users)
 */
public class AdminBrowseImages extends AppCompatActivity {

    private RecyclerView recyclerViewImages;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private AdminImageAdapter imageAdapter;
    private FirebaseFirestore db;
    private List<ImageData> imageList;

    /**
     * Activity entry point. Initializes UI components, sets up the toolbar, and starts image loading.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_images);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Browse Images");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        imageList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadImages();
    }

    /**
     * Gets references to the main layout views for the image browser.
     */
    private void initViews() {
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Sets up the RecyclerView adapter and click handlers for image items.
     */
    private void setupRecyclerView() {
        imageAdapter = new AdminImageAdapter();
        imageAdapter.setOnImageClickListener(new AdminImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(ImageData image) {
                showImageDetails(image);
            }

            @Override
            public void onDeleteClick(ImageData image) {
                showDeleteConfirmation(image);
            }
        });

        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewImages.setAdapter(imageAdapter);
    }

    /**
     * Begins loading images for both event posters and user profile pictures.
     * Shows a progress indicator while loading and clears any existing image data.
     */
    private void loadImages() {
        progressBar.setVisibility(View.VISIBLE);
        imageList.clear();

        // Load event posters from Firestore events collection
        loadEventPosters();

        // Load profile pictures from Firestore users collection
        loadProfilePictures();
    }

    /**
     * Loads event poster images from the Firestore events collection.
     * Adds each found poster to the shared image list and refreshes the UI.
     */
    private void loadEventPosters() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String posterUrl = doc.getString("posterUrl");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            ImageData imageData = new ImageData(
                                    doc.getId(),
                                    posterUrl,
                                    doc.getString("organizerDeviceId"),
                                    "event_poster"
                            );
                            imageData.setEventName(doc.getString("name"));
                            imageData.setStoragePath("events/" + doc.getId());
                            imageList.add(imageData);
                        }
                    }
                    updateUI();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading event posters: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    /**
     * Loads user profile pictures from the Firestore users collection.
     * Adds each found profile image to the shared image list and refreshes the UI.
     */
    private void loadProfilePictures() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String profilePhotoUri = doc.getString("profilePhotoUri");
                        if (profilePhotoUri != null && !profilePhotoUri.isEmpty()) {
                            ImageData imageData = new ImageData(
                                    doc.getId(),
                                    profilePhotoUri,
                                    doc.getString("deviceId"),
                                    "profile_picture"
                            );
                            imageData.setUserName(doc.getString("name"));
                            imageData.setStoragePath("users/" + doc.getId());
                            imageList.add(imageData);
                        }
                    }
                    updateUI();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading profile pictures: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    /**
     * Shows a details dialog for the selected image and allows the admin to delete it.
     */
    private void showImageDetails(ImageData image) {
        String details;
        if ("event_poster".equals(image.getType())) {
            details = "Type: Event Poster\n" +
                    "Event: " + image.getEventName() + "\n" +
                    "Event ID: " + image.getImageId() + "\n" +
                    "Organizer: " + image.getUploadedBy() + "\n" +
                    "Storage: " + image.getStoragePath();
        } else {
            details = "Type: Profile Picture\n" +
                    "User: " + image.getUserName() + "\n" +
                    "User ID: " + image.getImageId() + "\n" +
                    "Email: " + image.getUploadedBy() + "\n" +
                    "Storage: " + image.getStoragePath();
        }

        new AlertDialog.Builder(this)
                .setTitle("Image Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .setNeutralButton("Delete", (dialog, which) -> showDeleteConfirmation(image))
                .show();
    }

    /**
     * Prompts the admin to confirm deletion of the selected image.
     */
    private void showDeleteConfirmation(ImageData image) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Delete this " +
                        ("event_poster".equals(image.getType()) ? "event poster" : "profile picture") +
                        "?\n\nThis cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(image))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Removes the selected image reference from Firestore and updates the UI.
     * Event posters are cleared from events documents, profile pictures are cleared from users documents.
     */
    private void deleteImage(ImageData image) {
        progressBar.setVisibility(View.VISIBLE);

        if ("event_poster".equals(image.getType())) {
            // Delete event poster - set posterUrl to null
            db.collection("events")
                    .document(image.getImageId())
                    .update("posterUrl", null)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Event poster removed", Toast.LENGTH_SHORT).show();
                        imageList.remove(image);
                        updateUI();
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        } else {
            // Delete profile picture - set profilePhotoUri to null
            db.collection("users")
                    .document(image.getImageId())
                    .update("profilePhotoUri", null)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show();
                        imageList.remove(image);
                        updateUI();
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        }
    }

    /**
     * Refreshes the displayed image list and empty-state view on the UI thread.
     */
    private void updateUI() {
        runOnUiThread(() -> {
            if (imageList.isEmpty()) {
                recyclerViewImages.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
            } else {
                recyclerViewImages.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
                imageAdapter.setImages(new ArrayList<>(imageList));
            }
        });
    }

    /**
     * Handles the action bar up button by closing the activity.
     * @return true when navigation up has been handled.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Simple model class for image list entries.
     */
    public static class ImageData {
        private String imageId;
        private String imageUrl;
        private String uploadedBy;
        private String type;
        private String storagePath;
        private String eventName;
        private String userName;

        /**
         * Creates a new ImageData object to represent an image entry.
         */
        public ImageData(String imageId, String imageUrl, String uploadedBy, String type) {
            this.imageId = imageId;
            this.imageUrl = imageUrl;
            this.uploadedBy = uploadedBy;
            this.type = type;
        }

        // Getters and setters
        public String getImageId() { return imageId; }
        public String getImageUrl() { return imageUrl; }
        public String getUploadedBy() { return uploadedBy; }
        public String getType() { return type; }
        public String getStoragePath() { return storagePath; }
        public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

        public String getEventName() { return eventName; }
        public void setEventName(String eventName) { this.eventName = eventName; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }
}