package com.example.eventlottery.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private boolean isLoading = false;

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
        if (isLoading) return;
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        eventsWithCommentsList.clear();

        db.collection("events")
                .get()
                .addOnSuccessListener(eventsSnapshot -> {
                    if (eventsSnapshot.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                        updateUI();
                        return;
                    }

                    AtomicInteger processedCount = new AtomicInteger(0);
                    int totalEvents = eventsSnapshot.size();
                    Map<String, Map<String, Object>> uniqueEvents = new HashMap<>();

                    for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                        String eventId = eventDoc.getId();
                        String eventName = eventDoc.getString("name");
                        if (eventName == null) eventName = eventDoc.getString("title");
                        if (eventName == null) eventName = "Unnamed Event";

                        // Count comments in the subcollection
                        String finalEventName = eventName;
                        db.collection("events")
                                .document(eventId)
                                .collection("comments")
                                .get()
                                .addOnSuccessListener(commentsSnapshot -> {
                                    int commentCount = commentsSnapshot.size();

                                    if (commentCount > 0) {
                                        Map<String, Object> eventInfo = new HashMap<>();
                                        eventInfo.put("eventId", eventId);
                                        eventInfo.put("eventName", finalEventName);
                                        eventInfo.put("commentCount", commentCount);
                                        uniqueEvents.put(eventId, eventInfo);
                                    }

                                    if (processedCount.incrementAndGet() == totalEvents) {
                                        eventsWithCommentsList.clear();
                                        eventsWithCommentsList.addAll(uniqueEvents.values());
                                        progressBar.setVisibility(View.GONE);
                                        isLoading = false;
                                        updateUI();
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
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
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