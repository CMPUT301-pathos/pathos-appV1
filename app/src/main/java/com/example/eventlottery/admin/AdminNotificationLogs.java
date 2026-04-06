package com.example.eventlottery.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.NotificationLog;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.ui.NotificationLogAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminNotificationLogs extends AppCompatActivity {

    private static final String TAG = "AdminNotifLogs";
    private EditText etSearch;
    private Spinner spinnerFilter;
    private RecyclerView recyclerViewLogs;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private TextView tvTotalLogs;

    private NotificationLogAdapter logAdapter;
    private FirebaseFirestore db;
    private FirestoreProfileRepository profileRepository;
    private List<NotificationLog> logList;
    private List<NotificationLog> originalLogList;
    private Map<String, String> userNamesCache; // Cache user names by device ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_notification_logs);
        
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        profileRepository = new FirestoreProfileRepository();
        logList = new ArrayList<>();
        originalLogList = new ArrayList<>();
        userNamesCache = new HashMap<>();

        initViews();
        setupSearch();
        setupFilter();
        setupRecyclerView();
        loadNotificationLogs();
        startRealtimeNotificationListener();
    }
    private void startRealtimeNotificationListener() {
        db.collectionGroup("notifications")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Real-time listener error", error);
                        return;
                    }

                    if (snapshots == null) return;

                    Log.d(TAG, "Real-time update: " + snapshots.size() + " notifications changed");
                    // Reload all notifications
                    loadNotificationLogs();
                });
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearchLogs);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        recyclerViewLogs = findViewById(R.id.recyclerViewLogs);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        tvTotalLogs = findViewById(R.id.tvTotalLogs);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLogs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilter() {
        String[] filterOptions = {
                "All Notifications",
                "Lottery Won",
                "Lottery Lost",
                "Invitation Sent",
                "Waitlist Joined",
                "Event Cancelled"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filterOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = filterOptions[position];
                applyFilter(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyFilter(String filter) {
        List<NotificationLog> filteredList = new ArrayList<>();

        switch (filter) {
            case "All Notifications":
                filteredList.addAll(originalLogList);
                break;
            case "Lottery Won":
                for (NotificationLog log : originalLogList) {
                    if ("WIN".equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Lottery Lost":
                for (NotificationLog log : originalLogList) {
                    if ("LOST".equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Invitation Sent":
                for (NotificationLog log : originalLogList) {
                    if ("INVITE".equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Waitlist Joined":
                for (NotificationLog log : originalLogList) {
                    if ("JOIN".equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Event Cancelled":
                for (NotificationLog log : originalLogList) {
                    if ("CANCELLED".equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            default:
                filteredList.addAll(originalLogList);
        }

        logAdapter.setLogs(filteredList);
        tvTotalLogs.setText("Total: " + filteredList.size() + " logs");
    }

    private void filterLogs(String searchText) {
        if (searchText.isEmpty()) {
            applyFilter(spinnerFilter.getSelectedItem().toString());
            return;
        }

        List<NotificationLog> filteredList = new ArrayList<>();
        String lowerSearch = searchText.toLowerCase();
        
        for (NotificationLog log : originalLogList) {
            if ((log.getRecipientId() != null && log.getRecipientId().toLowerCase().contains(lowerSearch)) ||
                (log.getRecipientName() != null && log.getRecipientName().toLowerCase().contains(lowerSearch)) ||
                (log.getMessage() != null && log.getMessage().toLowerCase().contains(lowerSearch))) {
                filteredList.add(log);
            }
        }

        logAdapter.setLogs(filteredList);
        tvTotalLogs.setText("Total: " + filteredList.size() + " logs");
    }

    private void setupRecyclerView() {
        logAdapter = new NotificationLogAdapter();
        logAdapter.setOnLogClickListener(this::showLogDetails);

        recyclerViewLogs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLogs.setAdapter(logAdapter);
    }

    /*private void loadNotificationLogs() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    logList.clear();
                    originalLogList.clear();
                    
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " notifications");
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            NotificationLog log = new NotificationLog();
                            log.setLogId(doc.getId());
                            
                            // Try multiple possible field names for recipient
                            String recipientId = null;
                            String recipientName = null;
                            
                            // Check for recipientID field
                            if (doc.contains("recipientID")) {
                                recipientId = doc.getString("recipientID");
                                Log.d(TAG, "Found recipientID: " + recipientId);
                            } 
                            // Check for recipientId field
                            else if (doc.contains("recipientId")) {
                                recipientId = doc.getString("recipientId");
                                Log.d(TAG, "Found recipientId: " + recipientId);
                            }
                            // Check for deviceId field
                            else if (doc.contains("deviceId")) {
                                recipientId = doc.getString("deviceId");
                                Log.d(TAG, "Found deviceId: " + recipientId);
                            }
                            // Check for userId field
                            else if (doc.contains("userId")) {
                                recipientId = doc.getString("userId");
                                Log.d(TAG, "Found userId: " + recipientId);
                            }
                            else {
                                Log.d(TAG, "No recipient field found in document: " + doc.getId());
                                recipientId = "Unknown";
                            }
                            
                            log.setRecipientId(recipientId);
                            
                            // Try to get user name from cache or fetch from users collection
                            if (recipientId != null && !recipientId.equals("Unknown") && !recipientId.equals("null")) {
                                if (userNamesCache.containsKey(recipientId)) {
                                    recipientName = userNamesCache.get(recipientId);
                                    log.setRecipientName(recipientName);
                                } else {
                                    // Fetch user name from users collection
                                    fetchUserName(recipientId, log);
                                    log.setRecipientName("Loading...");
                                }
                            } else {
                                log.setRecipientName("Unknown User");
                            }
                            
                            log.setEventId(doc.getString("eventId"));
                            log.setMessage(doc.getString("message"));
                            
                            String type = doc.getString("type");
                            log.setNotificationType(type);
                            
                            if ("WIN".equals(type)) {
                                log.setTitle("Lottery Won");
                            } else if ("LOST".equals(type)) {
                                log.setTitle("Lottery Lost");
                            } else if ("CANCELLED".equals(type)) {
                                log.setTitle("Event Cancelled");
                            } else if ("INVITE".equals(type)) {
                                log.setTitle("Invitation Sent");
                            } else if ("JOIN".equals(type)) {
                                log.setTitle("Waitlist Joined");
                            } else {
                                log.setTitle(type);
                            }
                            
                            Boolean read = doc.getBoolean("read");
                            log.setStatus(read != null && read ? "read" : "unread");
                            
                            // Try to get timestamp
                            Object timestampObj = doc.get("createdAt");
                            if (timestampObj == null) {
                                timestampObj = doc.get("timestamp");
                            }
                            
                            if (timestampObj instanceof Long) {
                                Long timestampMs = (Long) timestampObj;
                                log.setTimestamp(new com.google.firebase.Timestamp(timestampMs / 1000, 0));
                            } else if (timestampObj instanceof com.google.firebase.Timestamp) {
                                log.setTimestamp((com.google.firebase.Timestamp) timestampObj);
                            }
                            
                            log.setSenderName("System");
                            log.setSenderId("system");
                            
                            logList.add(log);
                            originalLogList.add(log);
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing notification: " + doc.getId(), e);
                        }
                    }
                    
                    progressBar.setVisibility(View.GONE);
                    updateUI();
                    
                    if (logList.isEmpty()) {
                        Toast.makeText(this, "No notification logs found", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Loaded " + logList.size() + " logs", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading notifications", e);
                    Toast.makeText(this, "Error loading logs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }
    */
    private void loadNotificationLogs() {
        progressBar.setVisibility(View.VISIBLE);
        logList.clear();
        originalLogList.clear();
        userNamesCache.clear();

        Log.d(TAG, "Loading notifications from ALL locations using collectionGroup...");

        // Use collectionGroup to find ALL notifications anywhere in Firestore
        db.collectionGroup("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " notifications across all collections");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            NotificationLog log = new NotificationLog();
                            log.setLogId(doc.getId());

                            // Get the path to see where this notification came from
                            String path = doc.getReference().getPath();
                            Log.d(TAG, "Notification from: " + path);

                            // Try multiple possible field names for recipient
                            String recipientId = null;

                            if (doc.contains("recipientID")) {
                                recipientId = doc.getString("recipientID");
                            } else if (doc.contains("recipientId")) {
                                recipientId = doc.getString("recipientId");
                            } else if (doc.contains("deviceId")) {
                                recipientId = doc.getString("deviceId");
                            } else if (doc.contains("userId")) {
                                recipientId = doc.getString("userId");
                            } else {
                                // Try to extract from path: users/{userId}/notifications/...
                                if (path.contains("/users/") && path.contains("/notifications/")) {
                                    String[] parts = path.split("/");
                                    for (int i = 0; i < parts.length; i++) {
                                        if (parts[i].equals("users") && i + 1 < parts.length) {
                                            recipientId = parts[i + 1];
                                            break;
                                        }
                                    }
                                }
                            }

                            log.setRecipientId(recipientId != null ? recipientId : "Unknown");

                            // Fetch user name
                            if (recipientId != null && !recipientId.equals("Unknown")) {
                                if (userNamesCache.containsKey(recipientId)) {
                                    log.setRecipientName(userNamesCache.get(recipientId));
                                } else {
                                    fetchUserName(recipientId, log);
                                    log.setRecipientName("Loading...");
                                }
                            } else {
                                log.setRecipientName("Unknown User");
                            }

                            log.setEventId(doc.getString("eventId"));
                            log.setMessage(doc.getString("message"));

                            String type = doc.getString("type");
                            if (type == null) type = doc.getString("notificationType");
                            log.setNotificationType(type);

                            if ("WIN".equals(type)) {
                                log.setTitle("Lottery Won");
                            } else if ("LOST".equals(type)) {
                                log.setTitle("Lottery Lost");
                            } else if ("CANCELLED".equals(type)) {
                                log.setTitle("Event Cancelled");
                            } else if ("INVITE".equals(type)) {
                                log.setTitle("Invitation Sent");
                            } else if ("JOIN".equals(type)) {
                                log.setTitle("Waitlist Joined");
                            } else {
                                log.setTitle(type != null ? type : "Notification");
                            }

                            Boolean read = doc.getBoolean("read");
                            log.setStatus(read != null && read ? "read" : "unread");

                            // Try to get timestamp
                            Object timestampObj = doc.get("createdAt");
                            if (timestampObj == null) timestampObj = doc.get("timestamp");
                            if (timestampObj == null) timestampObj = doc.get("sentAt");

                            if (timestampObj instanceof Long) {
                                Long timestampMs = (Long) timestampObj;
                                log.setTimestamp(new com.google.firebase.Timestamp(timestampMs / 1000, 0));
                            } else if (timestampObj instanceof com.google.firebase.Timestamp) {
                                log.setTimestamp((com.google.firebase.Timestamp) timestampObj);
                            } else {
                                log.setTimestamp(com.google.firebase.Timestamp.now());
                            }

                            log.setSenderName("System");
                            log.setSenderId("system");

                            logList.add(log);
                            originalLogList.add(log);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing notification: " + doc.getId(), e);
                        }
                    }

                    // Sort by timestamp (newest first)
                    logList.sort((a, b) -> {
                        if (a.getTimestamp() == null) return 1;
                        if (b.getTimestamp() == null) return -1;
                        return b.getTimestamp().compareTo(a.getTimestamp());
                    });
                    originalLogList.sort((a, b) -> {
                        if (a.getTimestamp() == null) return 1;
                        if (b.getTimestamp() == null) return -1;
                        return b.getTimestamp().compareTo(a.getTimestamp());
                    });

                    progressBar.setVisibility(View.GONE);
                    updateUI();

                    if (logList.isEmpty()) {
                        Toast.makeText(this, "No notification logs found anywhere", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Loaded " + logList.size() + " logs from all locations", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading notifications", e);
                    Toast.makeText(this, "Error loading logs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }
    private void fetchUserName(String userId, NotificationLog log) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            userNamesCache.put(userId, name);
                            log.setRecipientName(name);
                            
                            // Refresh the display for this specific log
                            runOnUiThread(() -> {
                                int index = logList.indexOf(log);
                                if (index >= 0) {
                                    logAdapter.notifyItemChanged(index);
                                }
                            });
                        } else {
                            log.setRecipientName(userId); // Use ID if no name found
                        }
                    } else {
                        log.setRecipientName(userId); // Use ID if user not found
                    }
                })
                .addOnFailureListener(e -> {
                    log.setRecipientName(userId); // Use ID on error
                });
    }

    private void showLogDetails(NotificationLog log) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());

        String details = "FROM: " + (log.getSenderName() != null ? log.getSenderName() : "System") + "\n\n" +
                "TO: " + (log.getRecipientName() != null ? log.getRecipientName() : log.getRecipientId()) + "\n\n" +
                "TO ID: " + (log.getRecipientId() != null ? log.getRecipientId() : "N/A") + "\n\n" +
                "EVENT ID: " + (log.getEventId() != null ? log.getEventId() : "N/A") + "\n\n" +
                "TYPE: " + (log.getNotificationType() != null ? log.getNotificationType() : "N/A") + "\n\n" +
                "MESSAGE: " + (log.getMessage() != null ? log.getMessage() : "N/A") + "\n\n" +
                "STATUS: " + (log.getStatus() != null ? log.getStatus() : "N/A") + "\n\n" +
                "TIME: " + (log.getTimestamp() != null ? sdf.format(log.getTimestamp().toDate()) : "N/A");

        new AlertDialog.Builder(this)
                .setTitle("Notification Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateUI() {
        if (logList.isEmpty()) {
            recyclerViewLogs.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            tvTotalLogs.setText("Total: 0 logs");
        } else {
            recyclerViewLogs.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            tvTotalLogs.setText("Total: " + logList.size() + " logs");
            logAdapter.setLogs(logList);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
