package com.example.eventlottery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.controller.EventController;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreEventRepository;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.ui.EventSummaryAdapter;
import com.google.android.material.button.MaterialButton;

/**
 * EventsFragment
 *
 * Displays the list of browseable events with live keyword search and filtering.
 *
 * User stories supported:
 * - US 01.01.03: See a list of events to join the waiting list for
 * - US 01.01.04: Filter events based on interests and availability
 * - US 01.01.05: Search for events by keyword
 * - US 01.01.06: Use keyword search with filtering
 * - US 01.02.01: Entrant provides personal information
 * - US 01.06.02: Sign up for an event from the event details
 *
 * @author Fawaz Mansoor, Edwin David, Kenneth Joseph
 * @version 1.4
 */
public class EventsFragment extends Fragment {

    private EventController eventController;
    private EventSummaryAdapter adapter;
    private TextView empty;
    private Button btnFilter;
    private EditText etSearch;

    // Filter state
    private String currentKeyword = "";
    private String selectedCategory = "All";
    private String selectedLocation = "";
    private boolean openOnly = false;
    private long selectedAfterDateMs = 0;
    private int selectedMaxCapacity = 0;

    private com.example.eventlottery.service.BeaconQrService beaconQrService;
    private androidx.activity.result.ActivityResultLauncher<com.journeyapps.barcodescanner.ScanOptions> scanLauncher;

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
        etSearch = root.findViewById(R.id.etSearch);

        btnQrScan.setOnClickListener(v ->
                requireCompletedProfile(() -> {
                    scanLauncher.launch(new com.journeyapps.barcodescanner.ScanOptions()
                            .setPrompt("Scan an event QR code")
                            .setBeepEnabled(true)
                            .setOrientationLocked(true));
                })
        );

        btnFilter.setOnClickListener(v -> showFilterDialog());

        // Live keyword search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s.toString();
                applyAllFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        eventController = new EventController(new FirestoreEventRepository());

        RecyclerView rv = root.findViewById(R.id.recycler_events);
        empty = root.findViewById(R.id.tv_events_empty);

        adapter = new EventSummaryAdapter();
        adapter.setCriteriaClickListener(event -> {
            String criteria = eventController.getLotteryCriteria(event);

            View dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_lottery_criteria, null);

            TextView tvName = dialogView.findViewById(R.id.tv_criteria_event_name);
            TextView tvContent = dialogView.findViewById(R.id.tv_criteria_content);
            MaterialButton btnGotIt = dialogView.findViewById(R.id.btn_criteria_got_it);

            tvName.setText(event.getName());
            tvContent.setText(criteria);

            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(
                                android.graphics.Color.TRANSPARENT));
            }

            btnGotIt.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        });

        adapter.setItemClickListener(event -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, EventDetailFragment.newInstance(
                            event.getId(),
                            event.getName(),
                            event.getDescription(),
                            event.getOrganizerDeviceId(),
                            event.getPosterUrl()
                    ))
                    .addToBackStack(null)
                    .commit();
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        beaconQrService = new com.example.eventlottery.service.BeaconQrService(
                new FirestoreEventRepository()
        );

        scanLauncher = registerForActivityResult(
                new com.journeyapps.barcodescanner.ScanContract(),
                result -> {
                    if (result.getContents() != null) {
                        requireCompletedProfile(() -> {
                            String payload = result.getContents();
                            beaconQrService.resolveQrScan(payload,
                                    new com.example.eventlottery.service.BeaconQrService.ResolveCallback() {
                                        @Override
                                        public void onSuccess(com.example.eventlottery.domain.EventSummary event) {
                                            EventDetailFragment fragment = EventDetailFragment.newInstance(
                                                    event.getId(),
                                                    event.getName(),
                                                    event.getDescription(),
                                                    event.getOrganizerDeviceId(),
                                                    event.getPosterUrl()
                                            );
                                            getParentFragmentManager().beginTransaction()
                                                    .replace(R.id.fragment_container, fragment)
                                                    .addToBackStack(null)
                                                    .commit();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(requireContext(),
                                                    "Event not found: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        });
                    } else {
                        Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        loadEvents();

        return root;
    }

    private void loadEvents() {
        eventController.loadAllEvents(new EventRepository.ListCallback() {
            @Override
            public void onSuccess(java.util.List<com.example.eventlottery.domain.EventSummary> events) {
                applyAllFilters();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(),
                        "Failed to load events: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyAllFilters() {
        java.util.List<com.example.eventlottery.domain.EventSummary> results =
                eventController.applyAllFilters(
                        currentKeyword,
                        selectedCategory,
                        selectedLocation,
                        openOnly,
                        selectedMaxCapacity,
                        selectedAfterDateMs
                );
        adapter.setFilteredItems(results);
        empty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
        updateFilterButtonLabel();
    }

    private void updateFilterButtonLabel() {
        String label = "Filter";
        if (!selectedCategory.equals("All")) label += ": " + selectedCategory;
        if (!selectedLocation.isEmpty()) label += " 📍" + selectedLocation;
        if (selectedAfterDateMs > 0) {
            String date = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                    .format(new java.util.Date(selectedAfterDateMs));
            label += " 📅" + date;
        }
        if (selectedMaxCapacity > 0) label += " 👥≤" + selectedMaxCapacity;
        if (openOnly) label += " ✓Open";
        btnFilter.setText(label);
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_filter_events, null);

        TextView tvCategoryLabel = dialogView.findViewById(R.id.tv_filter_category_label);
        MaterialButton btnPickCategory = dialogView.findViewById(R.id.btn_filter_pick_category);
        EditText etLocation = dialogView.findViewById(R.id.et_filter_location);
        EditText etMaxCapacity = dialogView.findViewById(R.id.et_filter_max_capacity);
        TextView tvDateLabel = dialogView.findViewById(R.id.tv_filter_date_label);
        MaterialButton btnPickDate = dialogView.findViewById(R.id.btn_filter_pick_date);
        MaterialButton btnClearDate = dialogView.findViewById(R.id.btn_filter_clear_date);
        CheckBox cbOpenOnly = dialogView.findViewById(R.id.cb_filter_open_only);
        MaterialButton btnClear = dialogView.findViewById(R.id.btn_filter_clear);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_filter_apply);

        tvCategoryLabel.setText("Category: " + selectedCategory);
        etLocation.setText(selectedLocation);
        cbOpenOnly.setChecked(openOnly);
        if (selectedMaxCapacity > 0) {
            etMaxCapacity.setText(String.valueOf(selectedMaxCapacity));
        }

        long[] selectedDateMs = {selectedAfterDateMs};
        updateDateLabel(tvDateLabel, selectedDateMs[0]);

        btnPickCategory.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Select Category")
                        .setItems(CATEGORIES, (d, which) -> {
                            selectedCategory = CATEGORIES[which];
                            tvCategoryLabel.setText("Category: " + selectedCategory);
                        })
                        .show()
        );

        btnPickDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            if (selectedDateMs[0] > 0) cal.setTimeInMillis(selectedDateMs[0]);
            new android.app.DatePickerDialog(requireContext(), (view, year, month, day) -> {
                cal.set(year, month, day, 0, 0, 0);
                selectedDateMs[0] = cal.getTimeInMillis();
                updateDateLabel(tvDateLabel, selectedDateMs[0]);
            }, cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        btnClearDate.setOnClickListener(v -> {
            selectedDateMs[0] = 0;
            updateDateLabel(tvDateLabel, 0);
        });

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(
                            android.graphics.Color.TRANSPARENT));
        }

        btnClear.setOnClickListener(v -> {
            selectedCategory = "All";
            selectedLocation = "";
            openOnly = false;
            selectedAfterDateMs = 0;
            selectedMaxCapacity = 0;
            applyAllFilters();
            dialog.dismiss();
        });

        btnApply.setOnClickListener(v -> {
            selectedLocation = etLocation.getText().toString().trim();
            openOnly = cbOpenOnly.isChecked();
            selectedAfterDateMs = selectedDateMs[0];

            String capStr = etMaxCapacity.getText().toString().trim();
            try {
                selectedMaxCapacity = capStr.isEmpty() ? 0 : Integer.parseInt(capStr);
            } catch (NumberFormatException e) {
                selectedMaxCapacity = 0;
            }

            applyAllFilters();
            dialog.dismiss();
        });

        dialog.show();
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

    private void requireCompletedProfile(Runnable onAllowed) {
        String deviceId = com.example.eventlottery.service.DeviceIdentityService
                .getDeviceId(requireContext());
        new FirestoreProfileRepository().getProfile(deviceId,
                new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(UserProfile profile) {
                        if (profile != null && profile.isProfileCompleted()) {
                            onAllowed.run();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Complete your profile first to participate in events.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(),
                                "Could not verify profile.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}