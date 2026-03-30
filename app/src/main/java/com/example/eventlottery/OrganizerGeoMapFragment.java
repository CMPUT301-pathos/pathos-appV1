package com.example.eventlottery;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizerGeoMapFragment extends Fragment implements OnMapReadyCallback {

    public static final String ARG_EVENT_ID = "eventId";
    public static final String ARG_EVENT_TITLE = "eventTitle";

    private static final String USERS_COLLECTION = "users";
    private static final String FIELD_NAME = "name";
    private static final String MAP_TAG = "geo_map";

    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private FirestoreWaitListRepository waitListRepository;

    private String eventId;
    private String eventTitle;

    private TextView titleText;
    private TextView emptyText;
    private ProgressBar progressBar;
    private ListView entrantListView;

    private ArrayAdapter<String> listAdapter;

    private final List<EntrantMapItem> entrantItems = new ArrayList<>();
    private final List<String> entrantLabels = new ArrayList<>();
    private final Map<String, Marker> markersByDeviceId = new HashMap<>();

    public OrganizerGeoMapFragment() {
        // Required empty public constructor
    }

    public static OrganizerGeoMapFragment newInstance(String eventId, String eventTitle) {
        OrganizerGeoMapFragment fragment = new OrganizerGeoMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_TITLE, eventTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_geo_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        waitListRepository = new FirestoreWaitListRepository();

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventTitle = getArguments().getString(ARG_EVENT_TITLE, "Entrant Join Locations");
        }

        titleText = view.findViewById(R.id.geo_map_title);
        emptyText = view.findViewById(R.id.geo_empty_text);
        progressBar = view.findViewById(R.id.geo_progress_bar);
        entrantListView = view.findViewById(R.id.geo_entrant_list);

        titleText.setText(eventTitle);

        listAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                entrantLabels
        );
        entrantListView.setAdapter(listAdapter);

        entrantListView.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position < 0 || position >= entrantItems.size()) {
                return;
            }
            zoomToEntrant(entrantItems.get(position));
        });

        setupMap();
        loadEntrantLocations();
    }

    private void setupMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentByTag(MAP_TAG);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.geo_map_container, mapFragment, MAP_TAG)
                    .commit();
            getChildFragmentManager().executePendingTransactions();
        }

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (!(tag instanceof String)) {
                return false;
            }

            String deviceId = (String) tag;
            for (int i = 0; i < entrantItems.size(); i++) {
                if (deviceId.equals(entrantItems.get(i).deviceId)) {
                    entrantListView.setItemChecked(i, true);
                    break;
                }
            }
            return false;
        });

        renderMarkers();
    }

    private void loadEntrantLocations() {
        if (TextUtils.isEmpty(eventId)) {
            showLoading(false);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No event selected.");
            Toast.makeText(requireContext(), "Missing event ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        clearCurrentData();

        waitListRepository.getRecordsByEventAsync(eventId, new com.example.eventlottery.data.WaitListRepository.WaitListCallBack() {
            @Override
            public void onSuccess(List<WaitListRecord> records) {
                List<WaitListRecord> recordsWithLocation = new ArrayList<>();

                for (WaitListRecord record : records) {
                    if (record != null
                            && record.getJoinLatitude() != null
                            && record.getJoinLongitude() != null
                            && !TextUtils.isEmpty(record.getDeviceId())) {
                        recordsWithLocation.add(record);
                    }
                }

                if (recordsWithLocation.isEmpty()) {
                    showLoading(false);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("No entrant locations found for this event.");
                    return;
                }

                loadUserNamesForRecords(recordsWithLocation);
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                emptyText.setVisibility(View.VISIBLE);
                emptyText.setText("Failed to load entrant locations.");
                Toast.makeText(requireContext(),
                        "Could not load map data.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserNamesForRecords(@NonNull List<WaitListRecord> records) {
        final int[] pendingLookups = {records.size()};

        for (WaitListRecord record : records) {
            final String deviceId = record.getDeviceId();
            final double latitude = record.getJoinLatitude();
            final double longitude = record.getJoinLongitude();

            db.collection(USERS_COLLECTION)
                    .document(deviceId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String name = userDoc.getString(FIELD_NAME);
                        if (TextUtils.isEmpty(name)) {
                            name = deviceId;
                        }
                        addEntrantItem(deviceId, name, latitude, longitude);
                        pendingLookups[0]--;
                        if (pendingLookups[0] == 0) {
                            finishLoadingState();
                        }
                    })
                    .addOnFailureListener(e -> {
                        addEntrantItem(deviceId, deviceId, latitude, longitude);
                        pendingLookups[0]--;
                        if (pendingLookups[0] == 0) {
                            finishLoadingState();
                        }
                    });
        }
    }

    private void addEntrantItem(@NonNull String deviceId,
                                @NonNull String name,
                                double latitude,
                                double longitude) {
        entrantItems.add(new EntrantMapItem(deviceId, name, latitude, longitude));
        entrantLabels.add(name);
        listAdapter.notifyDataSetChanged();
        renderMarkers();
    }

    private void finishLoadingState() {
        showLoading(false);

        if (entrantItems.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No entrant locations found for this event.");
            return;
        }

        emptyText.setVisibility(View.GONE);
        zoomToEntrant(entrantItems.get(0));
    }

    private void renderMarkers() {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();
        markersByDeviceId.clear();

        for (EntrantMapItem item : entrantItems) {
            LatLng position = new LatLng(item.latitude, item.longitude);

            Marker marker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(position)
                            .title(item.name)
                            .snippet("Joined event from this location")
            );

            if (marker != null) {
                marker.setTag(item.deviceId);
                markersByDeviceId.put(item.deviceId, marker);
            }
        }
    }

    private void zoomToEntrant(@NonNull EntrantMapItem item) {
        if (googleMap == null) {
            return;
        }

        LatLng target = new LatLng(item.latitude, item.longitude);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 14f));

        Marker marker = markersByDeviceId.get(item.deviceId);
        if (marker != null) {
            marker.showInfoWindow();
        }
    }

    private void clearCurrentData() {
        entrantItems.clear();
        entrantLabels.clear();
        markersByDeviceId.clear();

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

        if (googleMap != null) {
            googleMap.clear();
        }

        emptyText.setVisibility(View.GONE);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private static class EntrantMapItem {
        final String deviceId;
        final String name;
        final double latitude;
        final double longitude;

        EntrantMapItem(String deviceId, String name, double latitude, double longitude) {
            this.deviceId = deviceId;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}