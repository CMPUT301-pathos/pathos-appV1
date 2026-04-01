/**
 *  View notification logs (US 03.08.01)
 */
package com.example.eventlottery.admin;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.eventlottery.ui.NotificationLogAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminNotificationLogs extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spinnerFilter;
    private RecyclerView recyclerViewLogs;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private TextView tvTotalLogs;

    private NotificationLogAdapter logAdapter;
    private FirebaseFirestore db;
    private List<NotificationLog> logList;
    private List<NotificationLog> originalLogList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_notification_logs);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        logList = new ArrayList<>();
        originalLogList = new ArrayList<>();

        initViews();
        setupSearch();
        setupFilter();
        setupRecyclerView();
        loadNotificationLogs();
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
                "Event Reminder",
                "Organizer Message",
                "Event Cancelled",
                "Failed Only"
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
                    if (NotificationLog.TYPE_LOTTERY_WON.equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Lottery Lost":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_LOTTERY_LOST.equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Invitation Sent":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_INVITATION_SENT.equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Waitlist Joined":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_WAITLIST_JOINED.equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Event Reminder":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_EVENT_REMINDER.equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Organizer Message":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_ORGANIZER_MESSAGE.equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Event Cancelled":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_EVENT_CANCELLED.equals(log.getNotificationType())) {
                        filteredList.add(log);
                    }
                }
                break;
            case "Failed Only":
                for (NotificationLog log : originalLogList) {
                    if ("failed".equals(log.getStatus())) {
                        filteredList.add(log);
                    }
                }
                break;
        }

        logAdapter.setLogs(filteredList);
        tvTotalLogs.setText("Total: " + filteredList.size() + " logs");
    }

    private void filterLogs(String searchText) {
        if (searchText.isEmpty()) {
            applyFilter(spinnerFilter.getSelectedItem().toString());
            return;
        }

        List<NotificationLog> currentList = new ArrayList<>();
        String currentFilter = spinnerFilter.getSelectedItem().toString();

        // First apply filter
        switch (currentFilter) {
            case "All Notifications":
                currentList.addAll(originalLogList);
                break;
            case "Lottery Won":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_LOTTERY_WON.equals(log.getNotificationType())) {
                        currentList.add(log);
                    }
                }
                break;
            case "Lottery Lost":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_LOTTERY_LOST.equals(log.getNotificationType())) {
                        currentList.add(log);
                    }
                }
                break;
            case "Invitation Sent":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_INVITATION_SENT.equals(log.getNotificationType())) {
                        currentList.add(log);
                    }
                }
                break;
            case "Waitlist Joined":
                for (NotificationLog log : originalLogList) {
                    if (NotificationLog.TYPE_WAITLIST_JOINED.equals(log.getNotificationType())) {
                        currentList.add(log);
                    }
                }
                break;
            default:
                currentList.addAll(originalLogList);
        }

        // Then apply search
        List<NotificationLog> searchedList = new ArrayList<>();
        String lowerSearch = searchText.toLowerCase();

        for (NotificationLog log : currentList) {
            if ((log.getSenderName() != null && log.getSenderName().toLowerCase().contains(lowerSearch)) ||
                    (log.getRecipientName() != null && log.getRecipientName().toLowerCase().contains(lowerSearch)) ||
                    (log.getEventName() != null && log.getEventName().toLowerCase().contains(lowerSearch)) ||
                    (log.getTitle() != null && log.getTitle().toLowerCase().contains(lowerSearch)) ||
                    (log.getMessage() != null && log.getMessage().toLowerCase().contains(lowerSearch))) {
                searchedList.add(log);
            }
        }

        logAdapter.setLogs(searchedList);
        tvTotalLogs.setText("Total: " + searchedList.size() + " logs");
    }

    private void setupRecyclerView() {
        logAdapter = new NotificationLogAdapter();
        logAdapter.setOnLogClickListener(this::showLogDetails);

        recyclerViewLogs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLogs.setAdapter(logAdapter);
    }

    private void loadNotificationLogs() {
        progressBar.setVisibility(View.VISIBLE);

        // Load REAL data from Firestore
        db.collection("notification_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(500)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    logList.clear();
                    originalLogList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        NotificationLog log = doc.toObject(NotificationLog.class);
                        log.setLogId(doc.getId());
                        logList.add(log);
                        originalLogList.add(log);
                    }

                    progressBar.setVisibility(View.GONE);
                    updateUI();

                    if (logList.isEmpty()) {
                        Toast.makeText(this, "No notification logs found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading logs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void showLogDetails(NotificationLog log) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());

        String details = "FROM: " + (log.getSenderName() != null ? log.getSenderName() : "Unknown") +
                " (ID: " + (log.getSenderId() != null ? log.getSenderId() : "N/A") + ")\n\n" +
                "TO: " + (log.getRecipientName() != null ? log.getRecipientName() : "Unknown") +
                " (ID: " + (log.getRecipientId() != null ? log.getRecipientId() : "N/A") + ")\n\n" +
                "EVENT: " + (log.getEventName() != null ? log.getEventName() : "N/A") + "\n\n" +
                "TYPE: " + (log.getNotificationType() != null ? log.getNotificationType() : "N/A") + "\n\n" +
                "TITLE: " + (log.getTitle() != null ? log.getTitle() : "N/A") + "\n\n" +
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