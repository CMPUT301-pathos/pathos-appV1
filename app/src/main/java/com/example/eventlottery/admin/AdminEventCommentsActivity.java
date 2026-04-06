package com.example.eventlottery.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.EventComment;
import com.example.eventlottery.ui.EventCommentAdapter;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin activity for viewing and moderating event comments in real-time.
 * Uses Firestore snapshot listeners for automatic updates.
 *
 * Features:
 * - Real-time comment updates (no manual refresh needed)
 * - Delete comments with violation reasons
 * - Track policy violations
 * - Manual refresh option
 *
 * User stories supported:
 * - US 03.10.01: Remove event comments that violate app policy
 *
 * @author Original: [Author Name]
 * @author Real-time Updates: Claude
 * @version 2.2 - Enhanced Debug + Delete UI
 */
public class AdminEventCommentsActivity extends AppCompatActivity
        implements EventCommentAdapter.OnCommentClickListener {

    private static final String TAG = "AdminEventComments";

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private EventCommentAdapter adapter;
    private List<EventComment> commentsList = new ArrayList<>();

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    // Real-time listener registration
    private ListenerRegistration commentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_comments);

        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        if (eventId == null || eventName == null) {
            Toast.makeText(this, "Missing event information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Viewing comments for event: " + eventId + " (" + eventName + ")");

        initViews();
        setupToolbar();
        setupRecyclerView();
        testQuery();

        // Start listening for real-time updates
        startRealtimeCommentListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop listening when activity is destroyed
        stopRealtimeCommentListener();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewComments);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(eventName != null ? eventName : "Event Comments");
        }
    }

    private void setupRecyclerView() {
        adapter = new EventCommentAdapter();
        adapter.setOnCommentClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_comments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refreshComments();
            return true;
        } else if (item.getItemId() == R.id.action_delete_all) {
            showDeleteAllConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Manually refresh comments
     */
    private void refreshComments() {
        Log.d(TAG, "Manual refresh triggered");
        Toast.makeText(this, "Refreshing comments...", Toast.LENGTH_SHORT).show();

        stopRealtimeCommentListener();
        commentsList.clear();
        updateUI();
        startRealtimeCommentListener();
    }

    /**
     * Show confirmation dialog for deleting all comments
     */
    private void showDeleteAllConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Comments?")
                .setMessage("This will delete all " + commentsList.size() +
                        " comments for this event. This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> deleteAllComments())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete all comments for this event
     */
    private void deleteAllComments() {
        if (commentsList.isEmpty()) {
            Toast.makeText(this, "No comments to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        int totalComments = commentsList.size();
        final int[] deletedCount = {0};

        for (EventComment comment : new ArrayList<>(commentsList)) {
            db.collection("comments")
                    .document(comment.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        deletedCount[0]++;
                        if (deletedCount[0] == totalComments) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this,
                                    "Deleted " + deletedCount[0] + " comments",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Error deleting some comments",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Start real-time listener for comments.
     * This method uses addSnapshotListener to get automatic updates
     * whenever comments are added, modified, or deleted.
     */
    /*private void startRealtimeCommentListener() {
        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "=== STARTING COMMENT LISTENER ===");
        Log.d(TAG, "Event ID: " + eventId);

        commentsListener = db.collection("comments")
                .whereEqualTo("eventId", eventId)
                .addSnapshotListener((snapshots, error) -> {
                    // Hide progress bar after first load
                    progressBar.setVisibility(View.GONE);

                    Log.d(TAG, "=== COMMENT LISTENER CALLBACK TRIGGERED ===");

                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error.getMessage(), error);
                        Toast.makeText(this,
                                "Error listening for comments: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots == null) {
                        Log.w(TAG, "Snapshots are null");
                        return;
                    }

                    // Skip cache updates to avoid duplicate processing
                    if (snapshots.getMetadata().isFromCache()) {
                        Log.d(TAG, "Skipping cached data");
                        return;
                    }

                    Log.d(TAG, "Received snapshot with " + snapshots.size() + " documents");

                    if (snapshots.isEmpty()) {
                        Log.w(TAG, "No comments found for this event");
                    }

                    // Process document changes for efficient updates
                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = change.getDocument();
                        EventComment comment = documentToComment(doc);

                        switch (change.getType()) {
                            case ADDED:
                                Log.d(TAG, "Comment ADDED: " + comment.getId());
                                // Add new comment if not already in list
                                if (!containsCommentId(comment.getId())) {
                                    commentsList.add(comment);
                                }
                                break;

                            case MODIFIED:
                                Log.d(TAG, "Comment MODIFIED: " + comment.getId());
                                // Update existing comment
                                updateCommentInList(comment);
                                break;

                            case REMOVED:
                                Log.d(TAG, "Comment REMOVED: " + comment.getId());
                                // Remove deleted comment
                                removeCommentFromList(comment.getId());
                                break;
                        }
                    }

                    // Update UI with new data
                    updateUI();
                });

        Log.d(TAG, "Comment listener registered");
    }*/
    private void testQuery() {
        db.collection("comments")
                .whereEqualTo("eventId", eventId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    Log.d(TAG, "✅ Query found " + snapshots.size() + " comments");
                    for (DocumentSnapshot doc : snapshots) {
                        Log.d(TAG, "  - " + doc.getString("text"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Query failed: " + e.getMessage());
                });
    }
    private void startRealtimeCommentListener() {
        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "Looking in: events/" + eventId + "/comments");

        commentsListener = db.collection("events")
                .document(eventId)
                .collection("comments")
                .addSnapshotListener((snapshots, error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error.getMessage(), error);
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots == null) {
                        Log.w(TAG, "Snapshots are null");
                        return;
                    }

                    Log.d(TAG, "Comments found: " + snapshots.size());

                    commentsList.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        EventComment comment = new EventComment();
                        comment.setId(doc.getId());
                        comment.setEventId(eventId);
                        comment.setUserId(doc.getString("userId"));
                        comment.setUserName(doc.getString("userName"));
                        comment.setContent(doc.getString("content"));

                        Object timestamp = doc.get("timestamp");
                        if (timestamp instanceof com.google.firebase.Timestamp) {
                            comment.setTimestamp((com.google.firebase.Timestamp) timestamp);
                        }

                        commentsList.add(comment);
                        Log.d(TAG, "Added: " + comment.getContent());
                    }

                    updateUI();
                });
    }
    /*private void startRealtimeCommentListener() {
        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "=== STARTING COMMENT LISTENER ===");
        Log.d(TAG, "Event ID being queried: " + eventId);

        commentsListener = db.collection("comments")
                .whereEqualTo("eventId", eventId)
                .addSnapshotListener((snapshots, error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error.getMessage(), error);
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots == null) {
                        Log.w(TAG, "Snapshots are null");
                        Toast.makeText(this, "No data received", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "=== COMMENTS RECEIVED: " + snapshots.size() + " ===");

                    // Log each comment found
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Log.d(TAG, "Comment found - ID: " + doc.getId());
                        Log.d(TAG, "  eventId: " + doc.getString("eventId"));
                        Log.d(TAG, "  authorName: " + doc.getString("authorName"));
                        Log.d(TAG, "  text: " + doc.getString("text"));
                    }

                    commentsList.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        EventComment comment = documentToComment(doc);
                        commentsList.add(comment);
                        Log.d(TAG, "Added comment: " + comment.getContent());
                    }

                    updateUI();
                });
    }*/
    /**
     * Stop the real-time listener to prevent memory leaks.
     * Call this in onDestroy() or when you no longer need updates.
     */
    private void stopRealtimeCommentListener() {
        if (commentsListener != null) {
            Log.d(TAG, "Stopping real-time listener");
            commentsListener.remove();
            commentsListener = null;
        }
    }

    /**
     * Convert Firestore document to EventComment object
     */
    /*private EventComment documentToComment(DocumentSnapshot doc) {
        EventComment comment = new EventComment();
        comment.setId(doc.getId());
        comment.setEventId(doc.getString("eventId"));
        comment.setUserId(doc.getString("deviceId"));
        comment.setUserName(doc.getString("authorName"));
        comment.setContent(doc.getString("text"));

        // Handle timestamp conversion
        Object createdAt = doc.get("createdAt");
        if (createdAt instanceof Long) {
            long timestampMs = (Long) createdAt;
            com.google.firebase.Timestamp firestoreTimestamp =
                    new com.google.firebase.Timestamp(timestampMs / 1000, 0);
            comment.setTimestamp(firestoreTimestamp);
        } else if (createdAt instanceof com.google.firebase.Timestamp) {
            comment.setTimestamp((com.google.firebase.Timestamp) createdAt);
        }

        return comment;
    }*/
    private EventComment documentToComment(DocumentSnapshot doc) {
        EventComment comment = new EventComment();
        comment.setId(doc.getId());
        comment.setEventId(eventId);
        comment.setUserId(doc.getString("userId"));
        comment.setUserName(doc.getString("userName"));
        comment.setContent(doc.getString("content"));

        Object timestamp = doc.get("timestamp");
        if (timestamp instanceof com.google.firebase.Timestamp) {
            comment.setTimestamp((com.google.firebase.Timestamp) timestamp);
        } else if (timestamp instanceof Long) {
            long ts = (Long) timestamp;
            comment.setTimestamp(new com.google.firebase.Timestamp(ts / 1000, 0));
        }

        return comment;
    }

    /**
     * Check if comment with given ID exists in list
     */
    private boolean containsCommentId(String commentId) {
        for (EventComment c : commentsList) {
            if (c.getId().equals(commentId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update an existing comment in the list
     */
    private void updateCommentInList(EventComment updatedComment) {
        for (int i = 0; i < commentsList.size(); i++) {
            if (commentsList.get(i).getId().equals(updatedComment.getId())) {
                commentsList.set(i, updatedComment);
                return;
            }
        }
    }

    /**
     * Remove a comment from the list by ID
     */
    private void removeCommentFromList(String commentId) {
        for (int i = commentsList.size() - 1; i >= 0; i--) {
            if (commentsList.get(i).getId().equals(commentId)) {
                commentsList.remove(i);
                break;
            }
        }
    }

    private void updateUI() {
        Log.d(TAG, "Updating UI - " + commentsList.size() + " comments");

        if (commentsList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            Log.d(TAG, "Showing empty state");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            adapter.setComments(commentsList);
            Log.d(TAG, "Showing " + commentsList.size() + " comments");
        }
    }

    @Override
    public void onCommentClick(EventComment comment) {
        // Show comment details with delete option
        new AlertDialog.Builder(this)
                .setTitle("Comment by " + comment.getUserName())
                .setMessage(comment.getContent())
                .setPositiveButton("Delete", (dialog, which) -> showDeleteDialog(comment))
                .setNegativeButton("Close", null)
                .show();
    }

    @Override
    public void onCommentLongClick(EventComment comment) {
        showDeleteDialog(comment);
    }

    @Override
    public void onDeleteClick(EventComment comment) {
        showDeleteDialog(comment);
    }

    private void showDeleteDialog(EventComment comment) {
        String[] reasons = {
                "Spam or advertising",
                "Harassment or hate speech",
                "Inappropriate content",
                "Misinformation",
                "Personal attack",
                "Offensive language",
                "Other policy violation"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Violation Reason");
        builder.setItems(reasons, (dialog, which) -> {
            String reason = reasons[which];
            deleteComment(comment, reason);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /*private void deleteComment(EventComment comment, String reason) {
        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "Deleting comment: " + comment.getId() + " for reason: " + reason);

        // Create violation record
        Map<String, Object> violation = new HashMap<>();
        violation.put("userId", comment.getUserId());
        violation.put("userName", comment.getUserName());
        violation.put("userEmail", "");
        violation.put("deletedBy", "Admin");
        violation.put("reason", reason);
        violation.put("violationType", "Comment Violation");
        violation.put("content", comment.getContent());
        violation.put("eventId", eventId);
        violation.put("eventName", eventName);
        violation.put("deletedAt", System.currentTimeMillis());

        // Save violation record first
        db.collection("policy_violations")
                .add(violation)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Violation logged: " + docRef.getId());

                    // Then delete the comment
                    db.collection("comments")
                            .document(comment.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Comment deleted!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Comment deleted successfully");
                                // No need to manually update list - listener will handle it
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Log.e(TAG, "Error deleting comment", e);
                                Toast.makeText(this, "Error deleting comment: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error saving violation", e);
                    Toast.makeText(this, "Error saving violation: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }*/
    private void deleteComment(EventComment comment, String reason) {
        progressBar.setVisibility(View.VISIBLE);

        // Create violation record
        Map<String, Object> violation = new HashMap<>();
        violation.put("userId", comment.getUserId());
        violation.put("userName", comment.getUserName());
        violation.put("userEmail", "");
        violation.put("deletedBy", "Admin");
        violation.put("reason", reason);
        violation.put("violationType", "Comment Violation");
        violation.put("content", comment.getContent());
        violation.put("eventId", eventId);
        violation.put("eventName", eventName);
        violation.put("deletedAt", System.currentTimeMillis());

        // Save violation record first
        db.collection("policy_violations")
                .add(violation)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Violation logged: " + docRef.getId());

                    // Delete comment from the SUBCOLLECTION
                    db.collection("events")
                            .document(eventId)
                            .collection("comments")
                            .document(comment.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Comment deleted!", Toast.LENGTH_SHORT).show();
                                // Listener will auto-update the UI
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Log.e(TAG, "Error deleting comment", e);
                                Toast.makeText(this, "Error deleting comment: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error saving violation", e);
                    Toast.makeText(this, "Error saving violation: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}