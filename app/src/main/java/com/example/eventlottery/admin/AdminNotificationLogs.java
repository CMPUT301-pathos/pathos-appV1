package com.example.eventlottery.admin;

/**
 *  View notification logs (US 03.08.01)
 */

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.NotificationLog;
import com.example.eventlottery.ui.NotificationLogAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Admin   for reviewing notification logs.
 * Supports US 03.08.01 - Review logs of all notifications sent to entrants.
 * Includes search, filtering by type, and detailed view.
 *
 * @author Hasrat Singh Chauhan
 *  * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 */
public class AdminNotificationLogs  extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spinnerFilter;
    private RecyclerView recyclerViewLogs;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private TextView tvTotalLogs;

    private NotificationLogAdapter logAdapter;
    private FirebaseFirestore db;
    private List<NotificationLog> logList;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_notification_logs);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notification Logs");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        logList = new ArrayList<>();

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
                logAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

  /*  private void setupFilter() {
        // Create filter options
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
    }*/
  private void setupFilter() {
      String[] filterOptions = {
              "All Notifications",
              "Lottery Won",
              "Lottery Lost",
              "Invitation Sent",
              "Waitlist Joined",
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
              applyFilter(selected);  // This now filters the existing list
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
      });
  }

   /* private void applyFilter(String filter) {
        switch (filter) {
            case "All Notifications":
                currentFilter = "all";
                break;
            case "Lottery Won":
                currentFilter = NotificationLog.TYPE_LOTTERY_WON;
                break;
            case "Lottery Lost":
                currentFilter = NotificationLog.TYPE_LOTTERY_LOST;
                break;
            case "Invitation Sent":
                currentFilter = NotificationLog.TYPE_INVITATION_SENT;
                break;
            case "Waitlist Joined":
                currentFilter = NotificationLog.TYPE_WAITLIST_JOINED;
                break;
            case "Event Reminder":
                currentFilter = NotificationLog.TYPE_EVENT_REMINDER;
                break;
            case "Organizer Message":
                currentFilter = NotificationLog.TYPE_ORGANIZER_MESSAGE;
                break;
            case "Event Cancelled":
                currentFilter = NotificationLog.TYPE_EVENT_CANCELLED;
                break;
            case "Failed Only":
                currentFilter = "failed";
                break;
            default:
                currentFilter = "all";
        }
        loadNotificationLogs();
    }*/
   private void applyFilter(String filter) {
       List<NotificationLog> filteredList = new ArrayList<>();

       switch (filter) {
           case "All Notifications":
               filteredList.addAll(logList); // Show all
               break;
           case "Lottery Won":
               for (NotificationLog log : logList) {
                   if (NotificationLog.TYPE_LOTTERY_WON.equals(log.getNotificationType())) {
                       filteredList.add(log);
                   }
               }
               break;
           case "Lottery Lost":
               for (NotificationLog log : logList) {
                   if (NotificationLog.TYPE_LOTTERY_LOST.equals(log.getNotificationType())) {
                       filteredList.add(log);
                   }
               }
               break;
           case "Invitation Sent":
               for (NotificationLog log : logList) {
                   if (NotificationLog.TYPE_INVITATION_SENT.equals(log.getNotificationType())) {
                       filteredList.add(log);
                   }
               }
               break;
           case "Waitlist Joined":
               for (NotificationLog log : logList) {
                   if (NotificationLog.TYPE_WAITLIST_JOINED.equals(log.getNotificationType())) {
                       filteredList.add(log);
                   }
               }
               break;
           case "Failed Only":
               for (NotificationLog log : logList) {
                   if ("failed".equals(log.getStatus())) {
                       filteredList.add(log);
                   }
               }
               break;
           default:
               filteredList.addAll(logList);
       }

       // Update adapter with filtered list
       logAdapter.setLogs(filteredList);
       tvTotalLogs.setText("Total: " + filteredList.size() + " logs");
   }

    private void setupRecyclerView() {
        logAdapter = new NotificationLogAdapter();
        logAdapter.setOnLogClickListener(this::showLogDetails);

        recyclerViewLogs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLogs.setAdapter(logAdapter);
    }

   /* private void loadNotificationLogs() {
        progressBar.setVisibility(View.VISIBLE);

        Query query = db.collection("notification_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        // Apply type filter
        if (!currentFilter.equals("all") && !currentFilter.equals("failed")) {
            query = query.whereEqualTo("notificationType", currentFilter);
        } else if (currentFilter.equals("failed")) {
            query = query.whereEqualTo("status", "failed");
        }

        query.limit(500)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    logList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        NotificationLog log = doc.toObject(NotificationLog.class);
                        log.setLogId(doc.getId());
                        logList.add(log);
                    }
                    progressBar.setVisibility(View.GONE);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading logs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }*/
   private void loadNotificationLogs() {
       progressBar.setVisibility(View.VISIBLE);

       // FOR TESTING: Add sample data
       addSampleLogs();
       progressBar.setVisibility(View.GONE);
       updateUI();
       return;

    /* REAL FIREBASE CODE - Uncomment when ready
    Query query = db.collection("notification_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(500);

    if (!currentFilter.equals("all") && !currentFilter.equals("failed")) {
        query = query.whereEqualTo("notificationType", currentFilter);
    } else if (currentFilter.equals("failed")) {
        query = query.whereEqualTo("status", "failed");
    }

    query.get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                logList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    NotificationLog log = doc.toObject(NotificationLog.class);
                    log.setLogId(doc.getId());
                    logList.add(log);
                }
                progressBar.setVisibility(View.GONE);
                updateUI();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error loading logs: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                updateUI();
            });
    */
   }

    private void showLogDetails(NotificationLog log) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());

        String details = "FROM: " + log.getSenderName() + " (ID: " + log.getSenderId() + ")\n\n" +
                "TO: " + log.getRecipientName() + " (ID: " + log.getRecipientId() + ")\n\n" +
                "EVENT: " + (log.getEventName() != null ? log.getEventName() : "N/A") + "\n\n" +
                "TYPE: " + log.getNotificationType() + "\n\n" +
                "TITLE: " + log.getTitle() + "\n\n" +
                "MESSAGE: " + log.getMessage() + "\n\n" +
                "STATUS: " + log.getStatus() + "\n\n" +
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
    private void addSampleLogs() {
       //the bleow is for demo purposes, i will delte towards the end. pls do not edit it our w/o letting me know.
        // Clear existing list
        logList.clear();

        // Sample organizers
        String[] organizers = {"Kenneth Joseph", "Fawaz Mansoor", "Dmitriy Limanets", "Sarah Chen"};
        String[] entrants = {"John Doe", "Jane Smith", "Bob Johnson", "Alice Brown", "Charlie Wilson"};
        String[] events = {"Swimming Lessons", "Piano Class", "Dance Workshop", "Cooking Class"};

        // Current time for timestamps
        long now = System.currentTimeMillis();

        // Sample 1 - Lottery Won
        NotificationLog log1 = new NotificationLog();
        log1.setLogId("sample1");
        log1.setSenderName(organizers[0]);
        log1.setSenderId("org_001");
        log1.setRecipientName(entrants[0]);
        log1.setRecipientId("user_001");
        log1.setEventName(events[0]);
        log1.setEventId("event_001");
        log1.setNotificationType(NotificationLog.TYPE_LOTTERY_WON);
        log1.setTitle("You won the lottery!");
        log1.setMessage("Congratulations! You've been selected for Swimming Lessons");
        log1.setStatus("delivered");
        log1.setTimestamp(new com.google.firebase.Timestamp(new Date(now - 86400000))); // 1 day ago
        logList.add(log1);

        // Sample 2 - Lottery Lost
        NotificationLog log2 = new NotificationLog();
        log2.setLogId("sample2");
        log2.setSenderName(organizers[1]);
        log2.setSenderId("org_002");
        log2.setRecipientName(entrants[1]);
        log2.setRecipientId("user_002");
        log2.setEventName(events[1]);
        log2.setEventId("event_002");
        log2.setNotificationType(NotificationLog.TYPE_LOTTERY_LOST);
        log2.setTitle("Not selected this time");
        log2.setMessage("Sorry, you weren't selected for Piano Class");
        log2.setStatus("sent");
        log2.setTimestamp(new com.google.firebase.Timestamp(new Date(now - 172800000))); // 2 days ago
        logList.add(log2);

        // Sample 3 - Invitation Sent
        NotificationLog log3 = new NotificationLog();
        log3.setLogId("sample3");
        log3.setSenderName(organizers[2]);
        log3.setSenderId("org_003");
        log3.setRecipientName(entrants[2]);
        log3.setRecipientId("user_003");
        log3.setEventName(events[2]);
        log3.setEventId("event_003");
        log3.setNotificationType(NotificationLog.TYPE_INVITATION_SENT);
        log3.setTitle("You're invited!");
        log3.setMessage("Please confirm your attendance for Dance Workshop");
        log3.setStatus("delivered");
        log3.setTimestamp(new com.google.firebase.Timestamp(new Date(now - 3600000))); // 1 hour ago
        logList.add(log3);

        // Sample 4 - Failed Notification
        NotificationLog log4 = new NotificationLog();
        log4.setLogId("sample4");
        log4.setSenderName(organizers[3]);
        log4.setSenderId("org_004");
        log4.setRecipientName(entrants[3]);
        log4.setRecipientId("user_004");
        log4.setEventName(events[3]);
        log4.setEventId("event_004");
        log4.setNotificationType(NotificationLog.TYPE_EVENT_REMINDER);
        log4.setTitle("Event tomorrow!");
        log4.setMessage("Don't forget your Cooking Class tomorrow at 2 PM");
        log4.setStatus("failed");
        log4.setTimestamp(new com.google.firebase.Timestamp(new Date(now)));
        logList.add(log4);

        // Sample 5 - Waitlist Joined
        NotificationLog log5 = new NotificationLog();
        log5.setLogId("sample5");
        log5.setSenderName("System");
        log5.setSenderId("system");
        log5.setRecipientName(entrants[4]);
        log5.setRecipientId("user_005");
        log5.setEventName(events[0]);
        log5.setEventId("event_001");
        log5.setNotificationType(NotificationLog.TYPE_WAITLIST_JOINED);
        log5.setTitle("Joined waitlist");
        log5.setMessage("You've been added to the waiting list for Swimming Lessons");
        log5.setStatus("delivered");
        log5.setTimestamp(new com.google.firebase.Timestamp(new Date(now - 43200000))); // 12 hours ago
        logList.add(log5);
    }
}
