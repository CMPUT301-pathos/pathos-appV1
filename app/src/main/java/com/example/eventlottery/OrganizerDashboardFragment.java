package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.firebase.FirestoreEventRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.ui.EventSummaryAdapter;
import com.example.eventlottery.controller.EventController;
import com.google.android.material.snackbar.Snackbar;

/**
 * OrganizerDashboardFragment
 *Responsibilities:
 *   - Provides a landing page for event organizers.
 *   - Displays a button to create a new event (US 02.01.01).
 *   - Shows a list of events created by the current organizer.
 *   - Allows viewing lottery criteria and organizer-specific actions on events (QR code, edit, geo-details
 *
 * Organizer tools landing page:
 * - Create an Event button (US 02.01.01)
 * - List of user's events (placeholder for now)
 *
 * @author Kenneth Joseph
 * @version 1.4
 */

public class OrganizerDashboardFragment extends Fragment {

    private EventRepository repo;
    private EventSummaryAdapter adapter;
    private TextView empty;

    private EventController eventController;


    public OrganizerDashboardFragment() { }

    /**
     * Lifecycle method called to inflate the fragment layout and initialize UI components.
     *
     * Sets up:
     * - "Create Event" button
     * - RecyclerView for displaying organizer's events
     * - EventSummaryAdapter with organizer actions and lottery criteria
     *
     * @param inflater LayoutInflater to inflate XML layouts
     * @param container Optional parent view group
     * @param savedInstanceState Bundle containing saved state
     * @return root view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_organizer_dashboard, container, false);

        Button btnCreate = root.findViewById(R.id.btn_create_event);
        btnCreate.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CreateEventFragment())
                    .addToBackStack(null)
                    .commit();
        });
        repo = new FirestoreEventRepository();
        eventController = new EventController(repo);

        RecyclerView rv = root.findViewById(R.id.recycler_my_events);
        empty = root.findViewById(R.id.tv_my_events_empty);

        // Setup adapter with organizer actions and criteria listener
        adapter = new EventSummaryAdapter();
        adapter.setShowOrganizerActions(true);
        adapter.setCriteriaClickListener(event -> {
            String criteria = eventController.getLotteryCriteria(event);
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle(event.getName())
                    .setMessage(criteria)
                    .setPositiveButton("Got it", null)
                    .show();
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        // Set organizer-specific actions
        adapter.setOrganizerActionListener(new EventSummaryAdapter.OnOrganizerActionListener() {
            @Override
            public void onSeeQrClick(com.example.eventlottery.domain.EventSummary event) {
                String payload = "eventId:" + event.getId();

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, QrCodeFragment.newInstance(payload))
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onEditClick(com.example.eventlottery.domain.EventSummary event) {
                Snackbar.make(root, "Edit coming soon", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onGeoDetailsClick(com.example.eventlottery.domain.EventSummary event) {
                Snackbar.make(root, "Geo-details coming soon", Snackbar.LENGTH_SHORT).show();
            }
        });
    // Load events for current organizer
        loadMyEvents();

        return root;
    }

    /**
     * Loads the current organizer's events from the repository.
     *
     * Uses {@link DeviceIdentityService} to identify the current organizer's device ID.
     * Updates the adapter and toggles the empty view depending on results.
     * Displays a Snackbar if loading fails.
     */
    private void loadMyEvents() {
        String deviceId = DeviceIdentityService.getDeviceId(requireContext());

        repo.getEventsByOrganizer(deviceId, new EventRepository.ListCallback() {
            @Override
            public void onSuccess(java.util.List<com.example.eventlottery.domain.EventSummary> events) {
                adapter.setItems(events);
                boolean isEmpty = (events == null || events.isEmpty());
                empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                Snackbar.make(requireView(),
                        "Failed to load your events: " + e.getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }
}