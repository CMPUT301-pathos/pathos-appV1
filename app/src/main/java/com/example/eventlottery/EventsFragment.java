package com.example.eventlottery;

import android.app.AlertDialog;
import android.os.Bundle;
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
 * Displays the list of browseable events for regular users.
 *
 * Responsibilities:
 * - display all available events in a RecyclerView
 * - provide filter dialog for category, location, date, and availability
 * - allow users to browse event details
 * - block participation-related actions until the user's profile is completed
 *
 * Profile-completion protection:
 * - browsing is allowed without a completed profile
 * - QR scanning and QR-based participation flow are blocked until the
 *   required profile information is completed
 *
 * User stories supported:
 * - US 01.01.03: See a list of events to join the waiting list for
 * - US 01.01.04: Filter events based on interests and availability
 * - US 01.02.01: Entrant provides personal information
 * - US 01.02.02: Entrant updates personal information
 * - US 01.06.02: Sign up for an event from the event details
 * - US 01.07.01: User is identified by device
 *
 * @author Fawaz Mansoor, Edwin David, Kenneth Joseph
 * @version 1.2
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

        btnQrScan.setOnClickListener(v ->
                requireCompletedProfile(() -> {
                    scanLauncher.launch(new com.journeyapps.barcodescanner.ScanOptions()
                            .setPrompt("Scan an event QR code")
                            .setBeepEnabled(true)
                            .setOrientationLocked(true));
                })
        );

        btnFilter.setOnClickListener(v -> showFilterDialog());

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

        // Browsing event details is allowed even if the profile is incomplete.
        adapter.setItemClickListener(event -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, EventDetailFragment.newInstance(
                            event.getId(), event.getName(), event.getDescription()))
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
                                                    event.getDescription()
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

    /**
     * Loads all available events for browsing.
     * Browsing is allowed even if the user's profile is not yet completed.
     */
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

    /**
     * Shows the filter dialog for event browsing.
     */
    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_filter_events, null);

        TextView tvCategoryLabel = dialogView.findViewById(R.id.tv_filter_category_label);
        MaterialButton btnPickCategory = dialogView.findViewById(R.id.btn_filter_pick_category);
        EditText etLocation = dialogView.findViewById(R.id.et_filter_location);
        TextView tvDateLabel = dialogView.findViewById(R.id.tv_filter_date_label);
        MaterialButton btnPickDate = dialogView.findViewById(R.id.btn_filter_pick_date);
        MaterialButton btnClearDate = dialogView.findViewById(R.id.btn_filter_clear_date);
        CheckBox cbOpenOnly = dialogView.findViewById(R.id.cb_filter_open_only);
        MaterialButton btnClear = dialogView.findViewById(R.id.btn_filter_clear);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_filter_apply);

        tvCategoryLabel.setText("Category: " + selectedCategory);
        etLocation.setText(selectedLocation);
        cbOpenOnly.setChecked(openOnly);

        long[] selectedDateMs = {selectedAfterDateMs};
        updateDateLabel(tvDateLabel, selectedDateMs[0]);

        btnPickCategory.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Select Category")
                    .setItems(CATEGORIES, (d, which) -> {
                        selectedCategory = CATEGORIES[which];
                        tvCategoryLabel.setText("Category: " + selectedCategory);
                    })
                    .show();
        });

        btnPickDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            if (selectedDateMs[0] > 0) {
                cal.setTimeInMillis(selectedDateMs[0]);
            }

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
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnClear.setOnClickListener(v -> {
            selectedCategory = "All";
            selectedLocation = "";
            openOnly = false;
            selectedAfterDateMs = 0;

            adapter.setFilteredItems(
                    eventController.filterByCategoryAndAvailability("All", false)
            );

            btnFilter.setText("Filter");
            empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            dialog.dismiss();
        });

        btnApply.setOnClickListener(v -> {
            selectedLocation = etLocation.getText().toString().trim();
            openOnly = cbOpenOnly.isChecked();
            selectedAfterDateMs = selectedDateMs[0];
            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Updates the label showing the currently selected "after date" filter.
     *
     * @param tv     text view to update
     * @param dateMs selected date in milliseconds, or 0 for no date filter
     */
    private void updateDateLabel(TextView tv, long dateMs) {
        if (dateMs == 0) {
            tv.setText("Show events after: Any date");
        } else {
            String date = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    .format(new java.util.Date(dateMs));
            tv.setText("Show events after: " + date);
        }
    }

    /**
     * Applies all selected filters to the loaded event list and updates the UI.
     */
    private void applyFilters() {
        java.util.List<com.example.eventlottery.domain.EventSummary> filtered =
                eventController.filterByCategoryAndAvailability(selectedCategory, openOnly);

        if (!selectedLocation.isEmpty()) {
            java.util.List<com.example.eventlottery.domain.EventSummary> locationFiltered =
                    new java.util.ArrayList<>();

            for (com.example.eventlottery.domain.EventSummary e : filtered) {
                if (e.getLocation().toLowerCase().contains(selectedLocation.toLowerCase())) {
                    locationFiltered.add(e);
                }
            }
            filtered = locationFiltered;
        }

        if (selectedAfterDateMs > 0) {
            java.util.List<com.example.eventlottery.domain.EventSummary> dateFiltered =
                    new java.util.ArrayList<>();

            for (com.example.eventlottery.domain.EventSummary e : filtered) {
                if (e.getEventDate() >= selectedAfterDateMs) {
                    dateFiltered.add(e);
                }
            }
            filtered = dateFiltered;
        }

        adapter.setFilteredItems(filtered);

        String filterLabel = "Filter";
        if (!selectedCategory.equals("All")) {
            filterLabel += ": " + selectedCategory;
        }
        if (!selectedLocation.isEmpty()) {
            filterLabel += " 📍" + selectedLocation;
        }
        if (selectedAfterDateMs > 0) {
            String date = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                    .format(new java.util.Date(selectedAfterDateMs));
            filterLabel += " 📅" + date;
        }
        if (openOnly) {
            filterLabel += " ✓Open";
        }

        btnFilter.setText(filterLabel);
        empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * Verifies that the current user's profile is completed before allowing
     * participation-related actions such as QR scanning.
     *
     * @param onAllowed logic to run only if the profile is completed
     */
    private void requireCompletedProfile(Runnable onAllowed) {
        String deviceId = com.example.eventlottery.service.DeviceIdentityService.getDeviceId(requireContext());

        new FirestoreProfileRepository().getProfile(deviceId, new ProfileRepository.ProfileCallback() {
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
                        "Could not verify profile.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}