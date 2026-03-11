/*package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.EventHistoryAdapter;
import com.example.eventlottery.R;
import com.example.eventlottery.domain.EventHistoryRecord;
import com.example.eventlottery.service.DeviceIdentityService;
import java.util.ArrayList;
import java.util.List;

public class EventHistoryFragment extends Fragment {

    private ListView historyListView;
    private EventHistoryAdapter adapter;
    private List<EventHistoryRecord> historyList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_history, container, false);

        historyListView = view.findViewById(R.id.history_list);
        adapter = new EventHistoryAdapter(historyList, inflater);
        historyListView.setAdapter(adapter);

        // Load history (you'll connect to real data later)
        loadDummyData();

        return view;
    }

    private void loadDummyData() {
        historyList.add(new EventHistoryRecord("1", "Swimming Lessons", "March 15, 2026", "SELECTED"));
        historyList.add(new EventHistoryRecord("2", "Yoga Class", "March 10, 2026", "NOT_SELECTED"));
        historyList.add(new EventHistoryRecord("3", "Cooking Workshop", "March 5, 2026", "WAITING"));
        historyList.add(new EventHistoryRecord("4", "Piano Lessons", "Feb 28, 2026", "ACCEPTED"));
        historyList.add(new EventHistoryRecord("5", "Art Class", "Feb 20, 2026", "DECLINED"));
        adapter.notifyDataSetChanged();
    }
}*/
package com.example.eventlottery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.domain.EventHistoryRecord;
import com.example.eventlottery.firebase.FirestoreEventHistoryRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;
/**
 * EventHistoryFragment
 *
 * Role: Displays a list of the entrant's past participation records.
 * - Uses a RecyclerView + EventHistoryAdapter.
 * - Each row is defined by item_history.xml.
 *
 * User stories supported:
 * - US 01.02.03: View history of events registered for, selected or not.
 *
 * Notes:
 * - Data source may be Firebase-backed or placeholder depending on current wiring.
 */

/**
 * Fragment for displaying event history to entrants.
 * Implements US 01.02.03 - View event history (whether selected or not).
 * Shows a list of events the user has interacted with and their outcomes.
 *
 * @author Hasrat Singh Chauhan, Kenneth Joseph
 * @version 2.o
 * @see EventHistoryRecord
 * @see EventHistoryAdapter
 * @see FirestoreEventHistoryRepository
 * @since 1.0
 */
public class EventHistoryFragment extends Fragment {

    private ListView historyListView;
    private EventHistoryAdapter adapter;
    private List<EventHistoryRecord> historyList = new ArrayList<>();
    private FirestoreEventHistoryRepository firestoreRepo;
    private String deviceId;

    /**
     * Called when the fragment is created.
     * Initializes the Firestore repository and gets device ID.
     *
     * @param savedInstanceState saved instance state bundle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreRepo = new FirestoreEventHistoryRepository();
        if (getContext() != null) {
            deviceId = DeviceIdentityService.getDeviceId(requireContext());
        }
    }

    /**
     * Inflates the fragment layout and sets up the ListView with adapter.
     * Loads history data (real or dummy) based on Firebase availability.
     *
     * @param inflater layout inflater
     * @param container parent view group
     * @param savedInstanceState saved instance state
     * @return the inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_history, container, false);

        // Initialize ListView
        historyListView = view.findViewById(R.id.history_list);
        adapter = new EventHistoryAdapter(historyList, inflater);
        historyListView.setAdapter(adapter);

        // Load history data
        loadHistory();

        return view;
    }

    /**
     * Loads event history for the current user.
     * Attempts to load from Firestore first, falls back to dummy data if Firebase
     * is not initialized or if there's an error.
     */
   private void loadHistory() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(getContext(), "Unable to identify device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if Firebase is initialized
        try {
            FirebaseApp.initializeApp(requireContext());
            // Try to load real data from Firestore
            loadRealHistory(deviceId);
        } catch (Exception e) {
            // Firebase not initialized, use dummy data
            loadDummyData(deviceId);
        }
    }

    /**
     * Loads real event history from Firestore.
     * Uses FirestoreEventHistoryRepository to fetch user's history.
     *
     * @param deviceId the unique device identifier
     */
    private void loadRealHistory(String deviceId) {
        firestoreRepo.getHistory(deviceId, new FirestoreEventHistoryRepository.EventHistoryCallback() {
            @Override
            public void onSuccess(List<EventHistoryRecord> history) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (history != null && !history.isEmpty()) {
                        // Use real data from Firestore
                        historyList.clear();
                        historyList.addAll(history);
                        adapter.notifyDataSetChanged();
                    } else {
                        // No history found, show dummy data for now
                        loadDummyData(deviceId);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Failed to load history: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Fallback to dummy data on error
                    loadDummyData(deviceId);
                });
            }
        });
    }

    /**
     * Loads dummy event history for development and testing.
     * Used when Firebase is not available or when no real data exists.
     *
     * @param deviceId the unique device identifier
     */
   private void loadDummyData(String deviceId) {
        historyList.clear();

        historyList.add(new EventHistoryRecord(
                deviceId, "1", "Swimming Lessons",
                "March 15, 2026", "SELECTED"
        ));
        historyList.add(new EventHistoryRecord(
                deviceId, "2", "Yoga Class",
                "March 10, 2026", "NOT_SELECTED"
        ));
        historyList.add(new EventHistoryRecord(
                deviceId, "3", "Cooking Workshop",
                "March 5, 2026", "WAITING"
        ));
        historyList.add(new EventHistoryRecord(
                deviceId, "4", "Piano Lessons",
                "Feb 28, 2026", "ACCEPTED"
        ));
        historyList.add(new EventHistoryRecord(
                deviceId, "5", "Art Class",
                "Feb 20, 2026", "DECLINED"
        ));

        adapter.notifyDataSetChanged();

        // Optional: Show hint that this is dummy data
        Toast.makeText(getContext(),
                "Showing sample data (Firebase not connected)",
                Toast.LENGTH_SHORT).show();
    }




    /**
     * Refreshes the history list when fragment resumes.
     * Useful when user returns from adding new events.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadHistory(); // Refresh data when returning to fragment
    }
}