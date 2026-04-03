
package com.example.eventlottery.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.EventComment;
import com.example.eventlottery.ui.CommentModerationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminEventCommentsActivity extends AppCompatActivity {

    private static final String TAG = "EventComments";
    private TextView tvEventName, tvEventId;
    private RecyclerView recyclerViewComments;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;

    private CommentModerationAdapter commentAdapter;
    private List<EventComment> commentList;
    private String eventId;
    private String eventName;
    private String adminDeviceId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_comments);

        // Get data from intent
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Event Comments");
        }

        adminDeviceId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("deviceId", "");

        db = FirebaseFirestore.getInstance();
        commentList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadCommentsForEvent();
    }

    private void initViews() {
        tvEventName = findViewById(R.id.tvEventName);
        tvEventId = findViewById(R.id.tvEventId);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);

        tvEventName.setText(eventName != null ? eventName : "Unknown Event");
        tvEventId.setText("Event ID: " + eventId);
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentModerationAdapter();
        commentAdapter.setOnCommentDeleteListener(this::showDeleteConfirmation);

        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void loadCommentsForEvent() {
        progressBar.setVisibility(View.VISIBLE);
        commentList.clear();

        Log.d(TAG, "Loading comments for event: " + eventId);

        db.collection("events")
                .document(eventId)
                .collection("comments")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    Log.d(TAG, "Found " + querySnapshot.size() + " comments");

                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "No comments found for this event", Toast.LENGTH_SHORT).show();
                        updateUI();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        EventComment comment = new EventComment();
                        comment.setId(doc.getId());
                        comment.setEventId(eventId);

                        // Parse fields
                        if (doc.contains("deviceId")) {
                            comment.setUserId(doc.getString("deviceId"));
                        } else if (doc.contains("userId")) {
                            comment.setUserId(doc.getString("userId"));
                        }

                        if (doc.contains("authorName")) {
                            comment.setUserName(doc.getString("authorName"));
                        } else if (doc.contains("userName")) {
                            comment.setUserName(doc.getString("userName"));
                        }

                        if (doc.contains("text")) {
                            comment.setContent(doc.getString("text"));
                        } else if (doc.contains("content")) {
                            comment.setContent(doc.getString("content"));
                        }

                        if (doc.contains("createdAt")) {
                            comment.setTimestamp(doc.getTimestamp("createdAt"));
                        } else if (doc.contains("timestamp")) {
                            comment.setTimestamp(doc.getTimestamp("timestamp"));
                        }

                        commentList.add(comment);
                    }

                    updateUI();
                    Toast.makeText(this, "Loaded " + commentList.size() + " comments", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading comments", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    updateUI();
                });
    }

    private void showDeleteConfirmation(EventComment comment) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_comment, null);
        TextView tvCommentContent = dialogView.findViewById(R.id.tvCommentContent);
        TextView tvCommentAuthor = dialogView.findViewById(R.id.tvCommentAuthor);
        android.widget.EditText etDeletionReason = dialogView.findViewById(R.id.etDeletionReason);
        android.widget.CheckBox cbPolicyViolation = dialogView.findViewById(R.id.cbPolicyViolation);

        tvCommentContent.setText(comment.getContent());
        tvCommentAuthor.setText("By: " + comment.getUserName() + " (ID: " + comment.getUserId() + ")");

        new AlertDialog.Builder(this)
                .setTitle("Delete Comment")
                .setView(dialogView)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String reason = etDeletionReason.getText().toString().trim();
                    if (TextUtils.isEmpty(reason)) {
                        reason = "No reason provided";
                    }
                    if (cbPolicyViolation.isChecked()) {
                        reason = "Policy violation: " + reason;
                    }
                    deleteComment(comment, reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteComment(EventComment comment, String reason) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("events")
                .document(comment.getEventId())
                .collection("comments")
                .document(comment.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                    commentList.remove(comment);
                    updateUI();
                    logDeletion(comment, reason);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error deleting comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logDeletion(EventComment comment, String reason) {
        Map<String, Object> log = new HashMap<>();
        log.put("eventId", comment.getEventId());
        log.put("commentId", comment.getId());
        log.put("commentContent", comment.getContent());
        log.put("userId", comment.getUserId());
        log.put("userName", comment.getUserName());
        log.put("adminId", adminDeviceId);
        log.put("reason", reason);
        log.put("deletedAt", System.currentTimeMillis());
        log.put("type", "comment_deletion");

        db.collection("moderation_logs").add(log);
    }

    private void updateUI() {
        if (commentList.isEmpty()) {
            recyclerViewComments.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewComments.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            commentAdapter.setComments(commentList);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}