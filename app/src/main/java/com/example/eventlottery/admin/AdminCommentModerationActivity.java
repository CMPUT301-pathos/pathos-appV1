package com.example.eventlottery.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.ui.EventsWithCommentsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminCommentModerationActivity extends AppCompatActivity {

    private static final String TAG = "CommentModeration";
    private RecyclerView recyclerViewEvents;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private TextView tvEmptyMessage;

    private EventsWithCommentsAdapter eventsAdapter;
    private List<Map<String, Object>> eventsWithCommentsList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_comment_moderation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Comment Moderation");
        }

        db = FirebaseFirestore.getInstance();
        eventsWithCommentsList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadEventsWithComments();
    }

    private void initViews() {
        recyclerViewEvents = findViewById(R.id.recyclerViewComments);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        if (tvEmptyMessage != null) {
            tvEmptyMessage.setText("No events with comments found");
        }
    }

    private void setupRecyclerView() {
        eventsAdapter = new EventsWithCommentsAdapter();
        eventsAdapter.setOnEventClickListener(this::navigateToEventComments);

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventsAdapter);
    }

    private void loadEventsWithComments() {
        progressBar.setVisibility(View.VISIBLE);
        eventsWithCommentsList.clear();

        Log.d(TAG, "========== STARTING TO LOAD EVENTS WITH COMMENTS ==========");

        // First, let's check if there are ANY comments in the entire database
        db.collectionGroup("comments")
                .limit(1)
                .get()
                .addOnSuccessListener(anyComments -> {
                    if (anyComments.isEmpty()) {
                        Log.e(TAG, "❌ NO COMMENTS FOUND ANYWHERE IN THE DATABASE!");
                        Toast.makeText(this, "No comments found in any event", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "✅ Found at least one comment in the database!");
                    }
                });

        // Get all events
        db.collection("events")
                .get()
                .addOnSuccessListener(eventsSnapshot -> {
                    Log.d(TAG, "📊 Total events found: " + eventsSnapshot.size());

                    if (eventsSnapshot.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        updateUI();
                        Toast.makeText(this, "No events found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AtomicInteger processedCount = new AtomicInteger(0);
                    int totalEvents = eventsSnapshot.size();
                    Log.d(TAG, "📊 Processing " + totalEvents + " events...");

                    for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                        String eventId = eventDoc.getId();
                        String eventName = eventDoc.getString("name");
                        if (eventName == null) eventName = eventDoc.getString("title");
                        if (eventName == null) eventName = "Unnamed Event";

                        Log.d(TAG, "🔍 Checking event: " + eventId + " - " + eventName);

                        final String finalEventId = eventId;
                        final String finalEventName = eventName;

                        // Check comments for this specific event
                        db.collection("events")
                                .document(eventId)
                                .collection("comments")
                                .get()
                                .addOnSuccessListener(commentsSnapshot -> {
                                    int commentCount = commentsSnapshot.size();
                                    Log.d(TAG, "📝 Event " + finalEventId + " has " + commentCount + " comments");

                                    if (commentCount > 0) {
                                        Map<String, Object> eventInfo = new HashMap<>();
                                        eventInfo.put("eventId", finalEventId);
                                        eventInfo.put("eventName", finalEventName);
                                        eventInfo.put("commentCount", commentCount);
                                        eventsWithCommentsList.add(eventInfo);
                                        Log.d(TAG, "✅ Added event to list: " + finalEventName + " (" + commentCount + " comments)");
                                    }

                                    if (processedCount.incrementAndGet() == totalEvents) {
                                        progressBar.setVisibility(View.GONE);
                                        updateUI();
                                        Log.d(TAG, "========== COMPLETE: Found " + eventsWithCommentsList.size() + " events with comments ==========");
                                        Toast.makeText(this, "Found " + eventsWithCommentsList.size() + " events with comments", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ Error getting comments for event: " + finalEventId, e);
                                    if (processedCount.incrementAndGet() == totalEvents) {
                                        progressBar.setVisibility(View.GONE);
                                        updateUI();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "❌ Error loading events", e);
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    updateUI();
                });
    }

    private void navigateToEventComments(String eventId, String eventName) {
        Intent intent = new Intent(this, AdminEventCommentsActivity.class);
        intent.putExtra("eventId", eventId);
        intent.putExtra("eventName", eventName);
        startActivity(intent);
    }

    private void updateUI() {
        if (eventsWithCommentsList.isEmpty()) {
            Log.d(TAG, "📭 No events with comments to display");
            recyclerViewEvents.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "📱 Displaying " + eventsWithCommentsList.size() + " events in RecyclerView");
            recyclerViewEvents.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            eventsAdapter.setEvents(eventsWithCommentsList);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}