package com.example.eventlottery.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.Event;
import com.example.eventlottery.domain.EventComment;
import com.example.eventlottery.ui.CommentModerationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminCommentModerationActivity extends AppCompatActivity {

    private AutoCompleteTextView etEventSearch;
    private TextView tvLoadComments;
    private RecyclerView recyclerViewComments;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private CardView cardEventInfo;
    private TextView tvEventName, tvEventDate, tvEventOrganizer, tvEventId;

    private CommentModerationAdapter commentAdapter;
    private List<EventComment> commentList;
    private List<Event> eventList;
    private String selectedEventId;
    private String adminDeviceId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_comment_moderation);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setTitle("Moderate Comments");

        adminDeviceId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("deviceId", "");

        db = FirebaseFirestore.getInstance();
        commentList = new ArrayList<>();
        eventList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupEventSearch();
        setupLoadButton();
        loadEventsForSearch();
    }

    private void initViews() {
        etEventSearch = findViewById(R.id.etEventSearch);
        tvLoadComments = findViewById(R.id.tvLoadComments);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        cardEventInfo = findViewById(R.id.cardEventInfo);
        tvEventName = findViewById(R.id.tvEventName);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventOrganizer = findViewById(R.id.tvEventOrganizer);
        tvEventId = findViewById(R.id.tvEventId);
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentModerationAdapter();
        commentAdapter.setOnCommentDeleteListener(this::showDeleteConfirmation);

        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void setupEventSearch() {
        ArrayAdapter<Event> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, eventList);
        etEventSearch.setAdapter(adapter);
        etEventSearch.setThreshold(1);
        
        etEventSearch.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventList.get(position);
            selectedEventId = selectedEvent.getId();
            displayEventInfo(selectedEvent);
        });
    }

    private void loadEventsForSearch() {
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    eventList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        Event event = new Event();
                        event.setId(doc.getId());
                        event.setName(doc.getString("name"));
                        event.setEventDate(doc.get("eventDate"));
                        event.setOrganizerDeviceId(doc.getString("organizerDeviceId"));
                        eventList.add(event);
                    }
                    ArrayAdapter<Event> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, eventList);
                    etEventSearch.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show());
    }

    private void displayEventInfo(Event event) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        tvEventName.setText(event.getName());
        if (event.getEventDate() > 0) {
            tvEventDate.setText(sdf.format(new Date(event.getEventDate())));
        } else {
            tvEventDate.setText("Not set");
        }
        tvEventOrganizer.setText(event.getOrganizerDeviceId() != null ? event.getOrganizerDeviceId() : "Unknown");
        tvEventId.setText(event.getId());
        
        cardEventInfo.setVisibility(View.VISIBLE);
    }

    private void setupLoadButton() {
        tvLoadComments.setOnClickListener(v -> {
            if (TextUtils.isEmpty(selectedEventId)) {
                Toast.makeText(this, "Please select an event from the dropdown", Toast.LENGTH_SHORT).show();
                return;
            }
            loadCommentsForEvent(selectedEventId);
        });
    }

    private void loadCommentsForEvent(String eventId) {
        progressBar.setVisibility(View.VISIBLE);
        commentList.clear();

        db.collection("events")
                .document(eventId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        EventComment comment = doc.toObject(EventComment.class);
                        comment.setId(doc.getId());
                        comment.setEventId(eventId);
                        commentList.add(comment);
                    }
                    updateUI();
                    
                    if (commentList.isEmpty()) {
                        Toast.makeText(this, "No comments found for this event", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Loaded " + commentList.size() + " comments", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading comments: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void showDeleteConfirmation(EventComment comment) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_comment, null);
        TextView tvCommentContent = dialogView.findViewById(R.id.tvCommentContent);
        TextView tvCommentAuthor = dialogView.findViewById(R.id.tvCommentAuthor);
        EditText etDeletionReason = dialogView.findViewById(R.id.etDeletionReason);
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
