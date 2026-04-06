package com.example.eventlottery.admin;

import android.os.Bundle;
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
import com.example.eventlottery.domain.PolicyViolation;
import com.example.eventlottery.ui.EventCommentAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminEventCommentsActivity extends AppCompatActivity
        implements EventCommentAdapter.OnCommentClickListener {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private EventCommentAdapter adapter;
    private FirebaseFirestore db;
    private List<EventComment> commentsList;

    private String eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_comments);

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        if (eventId == null) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        commentsList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadComments();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewComments);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(eventName != null ? eventName : "Event Comments");
        }
    }

    private void setupRecyclerView() {
        adapter = new EventCommentAdapter();
        adapter.setOrganizerMode(true);
        adapter.setOnCommentClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);
        commentsList.clear();

        Log.d("AdminEventComments", "Loading comments for event: " + eventId);

        db.collection("comments")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    Log.d("AdminEventComments", "Found " + querySnapshot.size() + " comments");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        EventComment comment = new EventComment();
                        comment.setId(doc.getId());
                        comment.setEventId(doc.getString("eventId"));
                        comment.setUserId(doc.getString("deviceId"));
                        comment.setUserName(doc.getString("authorName"));
                        comment.setContent(doc.getString("text"));

                        Object createdAt = doc.get("createdAt");
                        if (createdAt instanceof Long) {
                            long timestampMs = (Long) createdAt;
                            com.google.firebase.Timestamp firestoreTimestamp =
                                    new com.google.firebase.Timestamp(timestampMs / 1000, 0);
                            comment.setTimestamp(firestoreTimestamp);
                        }

                        commentsList.add(comment);
                        Log.d("AdminEventComments", "Added comment: " + comment.getContent());
                    }

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("AdminEventComments", "Error loading comments", e);
                    Toast.makeText(this, "Error loading comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void updateUI() {
        if (commentsList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            adapter.setComments(commentsList);
        }
    }

    @Override
    public void onCommentClick(EventComment comment) {
        new AlertDialog.Builder(this)
                .setTitle("Comment by " + comment.getUserName())
                .setMessage(comment.getContent())
                .setPositiveButton("OK", null)
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

    private void deleteComment(EventComment comment, String reason) {
        progressBar.setVisibility(View.VISIBLE);

        Log.d("AdminEventComments", "Deleting comment: " + comment.getId());

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

        db.collection("policy_violations")
                .add(violation)
                .addOnSuccessListener(docRef -> {
                    db.collection("comments")
                            .document(comment.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Comment deleted!", Toast.LENGTH_SHORT).show();
                                commentsList.remove(comment);
                                updateUI();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error deleting comment", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error saving violation", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}