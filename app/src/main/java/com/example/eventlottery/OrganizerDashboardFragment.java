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
import com.google.android.material.snackbar.Snackbar;

/**
 * OrganizerDashboardFragment
 *
 * Organizer tools landing page:
 * - Create an Event button (US 02.01.01)
 * - List of user's events (placeholder for now)
 *
 * @author Kenneth Joseph
 * @version 1.1
 */

public class OrganizerDashboardFragment extends Fragment {

    private EventRepository repo;
    private EventSummaryAdapter adapter;
    private TextView empty;

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

        RecyclerView rv = root.findViewById(R.id.recycler_my_events);
        empty = root.findViewById(R.id.tv_my_events_empty);

        adapter = new EventSummaryAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        adapter.setItemClickListener(event -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,
                            OrganizerEventManagerFragment.newInstance(event.getId(), event.getName()))
                    .addToBackStack(null)
                    .commit();
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