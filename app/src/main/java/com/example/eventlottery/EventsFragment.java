package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.firebase.FirestoreEventRepository;
import com.example.eventlottery.ui.EventSummaryAdapter;

public class EventsFragment extends Fragment {

    private EventRepository repo;
    private EventSummaryAdapter adapter;
    private TextView empty;

    public EventsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_events, container, false);

        Button btnQrScan = root.findViewById(R.id.btnQrScan);
        Button btnFilter = root.findViewById(R.id.btnFilter);

        btnQrScan.setOnClickListener(v ->
                Toast.makeText(requireContext(), "QR Scan not wired yet", Toast.LENGTH_SHORT).show()
        );

        btnFilter.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Filter not wired yet", Toast.LENGTH_SHORT).show()
        );

        repo = new FirestoreEventRepository();

        RecyclerView rv = root.findViewById(R.id.recycler_events);
        empty = root.findViewById(R.id.tv_events_empty);

        adapter = new EventSummaryAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        loadEvents();

        return root;
    }

    private void loadEvents() {
        repo.getAllEvents(new EventRepository.ListCallback() {
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
}