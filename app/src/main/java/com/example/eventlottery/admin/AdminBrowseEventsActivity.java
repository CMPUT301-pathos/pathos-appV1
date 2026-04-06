package com.example.eventlottery.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.Event;
import com.example.eventlottery.ui.AdminEventAdapter;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Admin activity for browsing and managing events.
 * Supports:
 * - View all events (US 03.04.01)
 * - Search events by name
 * - Delete events (US 03.01.01)
 */
public class AdminBrowseEventsActivity extends AppCompatActivity {
    private EditText etSearch;
    private String currentStatusFilter = "all";
    private String currentSortOption = "name";
    private RecyclerView recyclerViewEvents;
    private LinearLayout emptyStateLayout;
    private AdminEventAdapter eventAdapter;
    private FirebaseFirestore db;
    private List<Event> eventList;

    /**
     * Called when the activity is first created.
     * Sets up the layout, action bar, Firestore instance, event list, and UI components.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_events);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Browse Events");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();

        initViews();
        setupSearch();
        setupRecyclerView();
        setupFilters();
        loadEvents();
    }
    /**
     * Handles the action bar back button by closing the activity.
     * @return true when navigation up has been handled.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to previous activity (AdminMainActivity)
        return true;
    }
    /**
     * Finds and stores references to the key UI views used by this activity.
     */
    private void initViews() {
        etSearch = findViewById(R.id.searchEvents);
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    /**
     * Configures the search input to filter the event adapter as the user types.
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                eventAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /*private void setupRecyclerView() {
        eventAdapter = new AdminEventAdapter();
        eventAdapter.setOnEventClickListener(new AdminEventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                showEventDetails(event);
            }

            @Override
            public void onDeleteClick(Event event) {
                showDeleteConfirmation(event);
            }
        });

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventAdapter);
    }*/
    /**
     * Sets up the RecyclerView and attaches the AdminEventAdapter.
     * Also binds click listeners for opening details and confirming deletion.
     */
    private void setupRecyclerView() {
        eventAdapter = new AdminEventAdapter();
        eventAdapter.setOnEventClickListener(new AdminEventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                showEventDetails(event);
            }

            @Override
            public void onDeleteClick(Event event) {
                showDeleteConfirmation(event);
            }
        });

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventAdapter);
    }


    /*private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setId(doc.getId());
                        eventList.add(event);
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }*/
    /**
     * Loads events from Firestore and converts each document into an Event object.
     * Uses safe conversion helpers for text, numeric, and date values before updating the UI.
     */
    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            // Manually create Event object from document data
                            Event event = new Event();
                            event.setId(doc.getId());

                            // Safely get each field
                            event.setName(getStringSafe(doc, "name"));
                            event.setDescription(getStringSafe(doc, "description"));
                            event.setOrganizerDeviceId(getStringSafe(doc, "organizerDeviceId"));
                            event.setPosterUrl(getStringSafe(doc, "posterUrl"));
                            event.setCategory(getStringSafe(doc, "category"));
                            event.setLocation(getStringSafe(doc, "location"));

                            // Handle date fields that might be strings or numbers
                            event.setEventDate(convertToLong(doc.get("eventDate")));
                            event.setRegistrationStart(convertToLong(doc.get("registrationStart")));
                            event.setRegistrationEnd(convertToLong(doc.get("registrationEnd")));

                            // Handle capacity and drawSize
                            event.setCapacity(convertToInt(doc.get("capacity")));
                            event.setDrawSize(convertToInt(doc.get("drawSize")));

                            eventList.add(event);
                        } catch (Exception e) {
                            Log.e("AdminEvents", "Error parsing event: " + doc.getId(), e);
                            // Continue with next event even if one fails
                        }
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    // Helper methods for safe conversion
    /**
     * Converts a Firestore document field into a non-null string.
     * Returns an empty string when the field is missing or null.
     */
    private String getStringSafe(QueryDocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        return value != null ? value.toString() : "";
    }

    /**
     * Converts a Firestore field value into a long.
     * Accepts Long, Integer, and numeric string representations.
     */
    private long convertToLong(Object value) {
        if (value == null) return 0;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Converts a Firestore field value into an int.
     * Accepts Integer, Long, and numeric string representations.
     */
    private int convertToInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Attaches the event status filter chips and the sort button.
     * Updates the displayed event list whenever the filter or sort selection changes.
     */
    private void setupFilters() {
        ChipGroup chipGroupStatus = findViewById(R.id.chipGroupStatus);

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentStatusFilter = "all";
            } else if (checkedId == R.id.chipActive) {
                currentStatusFilter = "active";
            } else if (checkedId == R.id.chipInactive) {
                currentStatusFilter = "inactive";
            } else if (checkedId == R.id.chipCompleted) {
                currentStatusFilter = "completed";
            } else if (checkedId == R.id.chipCancelled) {
                currentStatusFilter = "cancelled";
            } else if (checkedId == R.id.chipFlagged) {
                currentStatusFilter = "flagged";
            }

            applyFiltersAndSort();
        });

        // Setup sort button
        Button btnSort = findViewById(R.id.btnSort);
        btnSort.setOnClickListener(v -> showSortDialog());
    }
    /**
     * Opens a dialog with sort options for name and date ordering.
     * Stores the selected sort mode and refreshes the displayed list.
     */
    private void showSortDialog() {
        String[] sortOptions = {"Name (A-Z)", "Name (Z-A)", "Date (Newest)", "Date (Oldest)"};

        new AlertDialog.Builder(this)
                .setTitle("Sort Events")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: currentSortOption = "name_asc"; break;
                        case 1: currentSortOption = "name_desc"; break;
                        case 2: currentSortOption = "date_desc"; break;
                        case 3: currentSortOption = "date_asc"; break;
                    }
                    applyFiltersAndSort();
                })
                .show();
    }
    /**
     * Filters the current event list by selected status and applies the active sort.
     * Then pushes the filtered list to the adapter and updates the result count UI.
     */
    private void applyFiltersAndSort() {
        List<Event> filteredList = new ArrayList<>();

        // Apply status filter
        for (Event event : eventList) {
            if (matchesStatusFilter(event)) {
                filteredList.add(event);
            }
        }

        // Apply sorting
        sortEvents(filteredList);

        // Update adapter
        eventAdapter.setEvents(filteredList);

        // Update results count
        TextView tvResultsCount = findViewById(R.id.tvResultsCount);
        tvResultsCount.setText("Showing " + filteredList.size() + " events");
        tvResultsCount.setVisibility(View.VISIBLE);
    }

    /**
     * Determines whether a given event matches the currently selected status filter.
     * Supports active, inactive, completed, and all states.
     */
    private boolean matchesStatusFilter(Event event) {
        if (currentStatusFilter.equals("all")) return true;

        long now = System.currentTimeMillis();
        String status;

        if (now < event.getRegistrationStart()) {
            status = "inactive";
        } else if (now <= event.getRegistrationEnd()) {
            status = "active";
        } else {
            status = "completed";
        }

        // For flagged - you'd need to implement this logic
        if (currentStatusFilter.equals("flagged")) {
            return false; // No events flagged yet
        }

        return status.equals(currentStatusFilter);
    }

    /**
     * Sorts the provided list of events based on the currently selected sort option.
     */
    private void sortEvents(List<Event> events) {
        switch (currentSortOption) {
            case "name_asc":
                Collections.sort(events, (e1, e2) ->
                        e1.getName().compareToIgnoreCase(e2.getName()));
                break;
            case "name_desc":
                Collections.sort(events, (e1, e2) ->
                        e2.getName().compareToIgnoreCase(e1.getName()));
                break;
            case "date_desc":
                Collections.sort(events, (e1, e2) ->
                        Long.compare(e2.getEventDate(), e1.getEventDate()));
                break;
            case "date_asc":
                Collections.sort(events, (e1, e2) ->
                        Long.compare(e1.getEventDate(), e2.getEventDate()));
                break;
        }
    }
    /**
     * Builds and displays a dialog containing the full details for the selected event.
     */
    private void showEventDetails(Event event) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        String details = "Name: " + event.getName() + "\n\n" +
                "Description: " + event.getDescription() + "\n\n" +
                "Category: " + (event.getCategory() != null ? event.getCategory() : "Not set") + "\n" +
                "Location: " + (event.getLocation() != null ? event.getLocation() : "Not set") + "\n\n" +
                "Event Date: " + (event.getEventDate() > 0 ? sdf.format(new Date(event.getEventDate())) : "Not set") + "\n" +
                "Registration: " + sdf.format(new Date(event.getRegistrationStart())) +
                " to " + sdf.format(new Date(event.getRegistrationEnd())) + "\n\n" +
                "Capacity: " + (event.getCapacity() > 0 ? event.getCapacity() : "Unlimited") + "\n" +
                "Draw Size: " + event.getDrawSize() + "\n" +
                "Organizer Device ID: " + event.getOrganizerDeviceId() + "\n" +
                "Event ID: " + event.getId();

        new AlertDialog.Builder(this)
                .setTitle("Event Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Shows a confirmation dialog before deleting an event.
     * If the user confirms, the event is removed from Firestore.
     */
    private void showDeleteConfirmation(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Delete event \"" + event.getName() + "\"?\n\nThis cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the specified event document from Firestore and refreshes the UI.
     */
    private void deleteEvent(Event event) {
        db.collection("events")
                .document(event.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    eventList.remove(event);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the recycler view visibility and empty-state UI based on current event data.
     */
    private void updateUI() {
        if (eventList.isEmpty()) {
            recyclerViewEvents.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewEvents.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            eventAdapter.setEvents(eventList);
        }
    }


}