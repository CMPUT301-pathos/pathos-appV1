package com.example.eventlottery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.controller.EventController;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.firebase.FirestoreEventRepository;
import com.example.eventlottery.ui.EventSummaryAdapter;

/**
 * Fragment displaying the list of joinable events for entrants.
 *
 * Responsibilities:
 * - Display all available events in a RecyclerView
 * - Provide filter dialog for category, location, date, and availability
 *
 * User stories supported:
 * - US 01.01.03: See a list of events to join the waiting list for
 * - US 01.01.04: Filter events based on interests and availability
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class EventsFragment extends Fragment {

    private EventController eventController;
    private EventSummaryAdapter adapter;
    private TextView empty;
    private Button btnFilter;

    // Current filter state
    private String selectedCategory = "All";
    private String selectedLocation = "";
    private boolean openOnly = false;
    private long selectedAfterDateMs = 0;

    private static final String[] CATEGORIES = {
            "All", "Sports", "Music", "Arts", "Education", "Community"
    };

    public EventsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_events, container, false);

        Button btnQrScan = root.findViewById(R.id.btnQrScan);
        btnFilter = root.findViewById(R.id.btnFilter);

        btnQrScan.setOnClickListener(v ->
                Toast.makeText(requireContext(), "QR Scan not wired yet", Toast.LENGTH_SHORT).show()
        );

        btnFilter.setOnClickListener(v -> showFilterDialog());

        eventController = new EventController(new FirestoreEventRepository());

        RecyclerView rv = root.findViewById(R.id.recycler_events);
        empty = root.findViewById(R.id.tv_events_empty);

        adapter = new EventSummaryAdapter();
        adapter.setCriteriaClickListener(event -> {
            String criteria = eventController.getLotteryCriteria(event);
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle(event.getName())
                    .setMessage(criteria)
                    .setPositiveButton("Got it", null)
                    .show();
        });
        adapter.setItemClickListener(event -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,
                            EventDetailFragment.newInstance(event.getId(), event.getName(), event.getDescription()))
                    .addToBackStack(null)
                    .commit();
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        loadEvents();

        return root;
    }

    private void loadEvents() {
        eventController.loadAllEvents(new EventRepository.ListCallback() {
            @Override
            public void onSuccess(java.util.List<com.example.eventlottery.domain.EventSummary> events) {
                adapter.setItems(events);
                boolean isEmpty = (events == null || events.isEmpty());
                empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(),
                        "Failed to load events: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showFilterDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        // Category
        TextView tvCategoryLabel = new TextView(requireContext());
        tvCategoryLabel.setText("Category: " + selectedCategory);
        tvCategoryLabel.setTextSize(14);
        layout.addView(tvCategoryLabel);

        Button btnPickCategory = new Button(requireContext());
        btnPickCategory.setText("Pick Category");
        btnPickCategory.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Select Category")
                    .setItems(CATEGORIES, (d, which) -> {
                        selectedCategory = CATEGORIES[which];
                        tvCategoryLabel.setText("Category: " + selectedCategory);
                    })
                    .show();
        });
        layout.addView(btnPickCategory);

        // Location
        TextView tvLocationLabel = new TextView(requireContext());
        tvLocationLabel.setText("Location:");
        tvLocationLabel.setTextSize(14);
        tvLocationLabel.setPadding(0, 24, 0, 4);
        layout.addView(tvLocationLabel);

        EditText etLocation = new EditText(requireContext());
        etLocation.setHint("e.g. Edmonton");
        etLocation.setText(selectedLocation);
        layout.addView(etLocation);

        // Date picker
        TextView tvDateLabel = new TextView(requireContext());
        tvDateLabel.setTextSize(14);
        tvDateLabel.setPadding(0, 24, 0, 4);
        long[] selectedDateMs = {selectedAfterDateMs};
        updateDateLabel(tvDateLabel, selectedDateMs[0]);
        layout.addView(tvDateLabel);

        Button btnPickDate = new Button(requireContext());
        btnPickDate.setText(selectedAfterDateMs == 0 ? "Pick Date" : "Change Date");
        btnPickDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            if (selectedDateMs[0] > 0) cal.setTimeInMillis(selectedDateMs[0]);

            new android.app.DatePickerDialog(requireContext(), (view, year, month, day) -> {
                cal.set(year, month, day, 0, 0, 0);
                selectedDateMs[0] = cal.getTimeInMillis();
                updateDateLabel(tvDateLabel, selectedDateMs[0]);
                btnPickDate.setText("Change Date");
            }, cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(btnPickDate);

        // Clear date button
        Button btnClearDate = new Button(requireContext());
        btnClearDate.setText("Clear Date Filter");
        btnClearDate.setOnClickListener(v -> {
            selectedDateMs[0] = 0;
            updateDateLabel(tvDateLabel, 0);
            btnPickDate.setText("Pick Date");
        });
        layout.addView(btnClearDate);

        // Availability checkbox
        CheckBox cbOpenOnly = new CheckBox(requireContext());
        cbOpenOnly.setText("Show only open registration");
        cbOpenOnly.setChecked(openOnly);
        cbOpenOnly.setPadding(0, 24, 0, 0);
        layout.addView(cbOpenOnly);

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter Events")
                .setView(layout)
                .setPositiveButton("Apply", (dialog, which) -> {
                    selectedLocation = etLocation.getText().toString().trim();
                    openOnly = cbOpenOnly.isChecked();
                    selectedAfterDateMs = selectedDateMs[0];
                    applyFilters();
                })
                .setNegativeButton("Clear Filters", (dialog, which) -> {
                    selectedCategory = "All";
                    selectedLocation = "";
                    openOnly = false;
                    selectedAfterDateMs = 0;
                    adapter.setFilteredItems(eventController.filterByCategoryAndAvailability("All", false));
                    btnFilter.setText("Filter");
                    empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                })
                .show();
    }

    private void updateDateLabel(TextView tv, long dateMs) {
        if (dateMs == 0) {
            tv.setText("Show events after: Any date");
        } else {
            String date = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    .format(new java.util.Date(dateMs));
            tv.setText("Show events after: " + date);
        }
    }

    private void applyFilters() {
        java.util.List<com.example.eventlottery.domain.EventSummary> filtered =
                eventController.filterByCategoryAndAvailability(selectedCategory, openOnly);

        // Location filter
        if (!selectedLocation.isEmpty()) {
            java.util.List<com.example.eventlottery.domain.EventSummary> locationFiltered = new java.util.ArrayList<>();
            for (com.example.eventlottery.domain.EventSummary e : filtered) {
                if (e.getLocation().toLowerCase().contains(selectedLocation.toLowerCase())) {
                    locationFiltered.add(e);
                }
            }
            filtered = locationFiltered;
        }

        // Date filter
        if (selectedAfterDateMs > 0) {
            java.util.List<com.example.eventlottery.domain.EventSummary> dateFiltered = new java.util.ArrayList<>();
            for (com.example.eventlottery.domain.EventSummary e : filtered) {
                if (e.getEventDate() >= selectedAfterDateMs) {
                    dateFiltered.add(e);
                }
            }
            filtered = dateFiltered;
        }

        adapter.setFilteredItems(filtered);

        // Update filter button label
        String filterLabel = "Filter";
        if (!selectedCategory.equals("All")) filterLabel += ": " + selectedCategory;
        if (!selectedLocation.isEmpty()) filterLabel += " 📍" + selectedLocation;
        if (selectedAfterDateMs > 0) {
            String date = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                    .format(new java.util.Date(selectedAfterDateMs));
            filterLabel += " 📅" + date;
        }
        if (openOnly) filterLabel += " ✓Open";
        btnFilter.setText(filterLabel);

        empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}