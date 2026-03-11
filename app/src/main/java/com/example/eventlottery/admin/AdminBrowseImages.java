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
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
public class AdminBrowseImages extends AppCompatActivity {

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
    public static class ImageData {
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
    /*private void addSampleImages() {
        // Only add if empty
        if (!imageList.isEmpty()) return;

        // Sample image URLs (replace with actual image URLs or use placeholders)
        String[] sampleUrls = {
                "https://picsum.photos/200/300?random=1",
                "https://picsum.photos/200/300?random=2",
                "https://picsum.photos/200/300?random=3",
                "https://picsum.photos/200/300?random=4"
        };

        for (int i = 0; i < sampleUrls.length; i++) {
            ImageData sample = new ImageData(
                    "sample_" + i,
                    sampleUrls[i],
                    "test_organizer",
                    i % 2 == 0 ? "event_poster" : "profile_picture"
            );
            sample.setStoragePath("samples/image_" + i + ".jpg");
            imageList.add(sample);
        }

        updateUI();
    }*/
    /*private void loadImages() {
        progressBar.setVisibility(View.VISIBLE);
        imageList.clear();

        // FOR TESTING: Add sample images
        addSampleImages();  // Add this line
        progressBar.setVisibility(View.GONE);
        updateUI();
        return;
    */
    /* Your existing Firebase code (comment out temporarily)
    loadFromStorageFolder("event_posters", "event_poster");
    loadFromStorageFolder("profile_pictures", "profile_picture");
    */

}
