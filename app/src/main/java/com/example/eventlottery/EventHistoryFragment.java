package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.EventHistoryRecord;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.firebase.FirestoreEventRepository;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.ui.EventHistoryAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment for displaying event history to entrants.
 * Loads real data from Firestore waitlist collection filtered by device ID.
 * Shows events with ACCEPTED, CANCELLED, or DECLINED status.
 *
 * User stories supported:
 * - US 01.02.03: View history of events registered for, selected or not
 *
 * @author Hasrat Singh Chauhan, Fawaz Mansoor
 * @version 3.0
 * @see EventHistoryRecord
 * @see EventHistoryAdapter
 */
public class EventHistoryFragment extends Fragment {

    private ListView historyListView;
    private TextView tvHistoryEmpty;
    private EventHistoryAdapter adapter;
    private final List<EventHistoryRecord> historyList = new ArrayList<>();

    private FirestoreWaitListRepository waitListRepo;
    private FirestoreEventRepository eventRepo;
    private String deviceId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        waitListRepo = new FirestoreWaitListRepository();
        eventRepo = new FirestoreEventRepository();
        deviceId = DeviceIdentityService.getDeviceId(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_history, container, false);

        historyListView = view.findViewById(R.id.history_list);
        tvHistoryEmpty = view.findViewById(R.id.tvHistoryEmpty);
        adapter = new EventHistoryAdapter(historyList, inflater);
        historyListView.setAdapter(adapter);

        loadHistory();

        return view;
    }

    private void loadHistory() {
        waitListRepo.getRecordsForDevice(deviceId, new WaitListRepository.WaitListCallBack() {
            @Override
            public void onSuccess(List<WaitListRecord> records) {
                if (getActivity() == null) return;

                List<WaitListRecord> filtered = new ArrayList<>();
                for (WaitListRecord r : records) {
                    if (r.getStatus() == WaitStatus.ACCEPTED
                            || r.getStatus() == WaitStatus.CANCELLED
                            || r.getStatus() == WaitStatus.DECLINED) {
                        filtered.add(r);
                    }
                }

                if (filtered.isEmpty()) {
                    getActivity().runOnUiThread(() ->
                            tvHistoryEmpty.setVisibility(View.VISIBLE));
                    return;
                }

                AtomicInteger remaining = new AtomicInteger(filtered.size());
                List<EventHistoryRecord> results = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

                for (WaitListRecord record : filtered) {
                    eventRepo.getEventById(record.getEventId(),
                            new FirestoreEventRepository.EventByIdCallback() {
                                @Override
                                public void onResult(com.example.eventlottery.domain.EventSummary event) {
                                    String name = event != null ? event.getName() : record.getEventId();
                                    String date = event != null
                                            ? sdf.format(new Date(event.getEventDate()))
                                            : "";
                                    String status = record.getStatus().name();

                                    synchronized (results) {
                                        results.add(new EventHistoryRecord(
                                                record.getEventId(), name, date, status));
                                    }

                                    if (remaining.decrementAndGet() == 0) {
                                        if (getActivity() == null) return;
                                        getActivity().runOnUiThread(() -> {
                                            historyList.clear();
                                            historyList.addAll(results);
                                            adapter.notifyDataSetChanged();
                                            tvHistoryEmpty.setVisibility(
                                                    historyList.isEmpty() ? View.VISIBLE : View.GONE);
                                        });
                                    }
                                }
                            });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        tvHistoryEmpty.setVisibility(View.VISIBLE));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }
}