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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

/**
 * OrganizerDashboardFragment
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

        adapter = new EventSummaryAdapter();
        adapter.setShowOrganizerActions(true);
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
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);








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
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, EditEventFragment.newInstance(event))
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onGeoDetailsClick(com.example.eventlottery.domain.EventSummary event) {
                Snackbar.make(root, "Geo-details coming soon", Snackbar.LENGTH_SHORT).show();
            }
        });

        loadMyEvents();

        return root;
    }

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