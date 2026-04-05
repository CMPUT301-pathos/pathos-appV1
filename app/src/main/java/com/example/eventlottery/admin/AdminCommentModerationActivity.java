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

public class AdminCommentModerationActivity extends AppCompatActivity
        implements EventsWithCommentsAdapter.OnEventClickListener {

    private static final String TAG = "CommentModeration";

    private RecyclerView recyclerViewEvents;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private TextView tvEmptyMessage;

    private EventsWithCommentsAdapter eventsAdapter;
    private List<Map<String, Object>> eventsWithCommentsList;
    private FirebaseFirestore db;
    private boolean isLoading = false;  // Prevent multiple simultaneous loads

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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from detail screen
        Log.d(TAG, "onResume - Refreshing events with comments");
        loadEventsWithComments();
    }

    private void initViews() {
        recyclerViewEvents = findViewById(R.id.recyclerViewComments);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
    }

    private void setupRecyclerView() {
        eventsAdapter = new EventsWithCommentsAdapter();
        eventsAdapter.setOnEventClickListener(this);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventsAdapter);
    }

    private void loadEventsWithComments() {
        // Prevent multiple simultaneous loads
        if (isLoading) {
            Log.d(TAG, "Already loading, skipping...");
            return;
        }

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        // CRITICAL: Clear the list BEFORE starting the query
        eventsWithCommentsList.clear();

        // Also clear the adapter to prevent showing old data
        eventsAdapter.setEvents(new ArrayList<>());

        Log.d(TAG, "Loading events with comments...");

        // Get all events and check their comments
        db.collection("events")
                .get()
                .addOnSuccessListener(eventsSnapshot -> {
                    Log.d(TAG, "Total events found: " + eventsSnapshot.size());

                    if (eventsSnapshot.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                        updateUI();
                        return;
                    }

                    AtomicInteger processedCount = new AtomicInteger(0);
                    int totalEvents = eventsSnapshot.size();

                    // Use a Set to track unique event IDs to prevent duplicates
                    Map<String, Map<String, Object>> uniqueEvents = new HashMap<>();

                    for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                        final String eventId = eventDoc.getId();
                        String eventNameRaw = eventDoc.getString("name");
                        if (eventNameRaw == null) eventNameRaw = eventDoc.getString("title");
                        if (eventNameRaw == null) eventNameRaw = "Unnamed Event";
                        final String eventName = eventNameRaw;

                        // Count comments from top-level comments collection
                        db.collection("comments")
                                .whereEqualTo("eventId", eventId)
                                .get()
                                .addOnSuccessListener(commentsSnapshot -> {
                                    final int commentCount = commentsSnapshot.size();
                                    Log.d(TAG, "Event " + eventName + " has " + commentCount + " comments");

                                    if (commentCount > 0) {
                                        // Use eventId as key to prevent duplicates
                                        Map<String, Object> eventInfo = new HashMap<>();
                                        eventInfo.put("eventId", eventId);
                                        eventInfo.put("eventName", eventName);
                                        eventInfo.put("commentCount", commentCount);
                                        uniqueEvents.put(eventId, eventInfo);
                                    }

                                    if (processedCount.incrementAndGet() == totalEvents) {
                                        // Clear and add all unique events
                                        eventsWithCommentsList.clear();
                                        eventsWithCommentsList.addAll(uniqueEvents.values());

                                        progressBar.setVisibility(View.GONE);
                                        isLoading = false;
                                        updateUI();
                                        Log.d(TAG, "Found " + eventsWithCommentsList.size() + " unique events with comments");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error getting comments for event: " + eventId, e);
                                    if (processedCount.incrementAndGet() == totalEvents) {
                                        eventsWithCommentsList.clear();
                                        eventsWithCommentsList.addAll(uniqueEvents.values());

                                        progressBar.setVisibility(View.GONE);
                                        isLoading = false;
                                        updateUI();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    updateUI();
                });
    }

    @Override
    public void onEventClick(String eventId, String eventName) {
        Intent intent = new Intent(this, AdminEventCommentsActivity.class);
        intent.putExtra("eventId", eventId);
        intent.putExtra("eventName", eventName);
        startActivity(intent);
    }

    private void updateUI() {
        if (eventsWithCommentsList.isEmpty()) {
            recyclerViewEvents.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setText("No events with comments found");
            }
        } else {
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