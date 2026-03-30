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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizerGeoMapFragment extends Fragment implements OnMapReadyCallback {

    public static final String ARG_EVENT_ID = "eventId";
    public static final String ARG_EVENT_TITLE = "eventTitle";

    // Firestore collection names
    // Change these only if your project uses different names.
    private static final String WAITLIST_COLLECTION = "waitlist";
    private static final String USERS_COLLECTION = "users";

    // Waitlist field names
    // Change these if your Firestore field names differ.
    private static final String FIELD_EVENT_ID = "eventId";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_LATITUDE = "latitude";
    private static final String FIELD_LONGITUDE = "longitude";

    // User/profile field names
    private static final String FIELD_NAME = "name";

    private GoogleMap googleMap;
    private FirebaseFirestore db;

    private String eventId;
    private String eventTitle;

    private TextView titleText;
    private TextView emptyText;
    private ProgressBar progressBar;
    private ListView entrantListView;

    private ArrayAdapter<String> listAdapter;

    private final List<EntrantMapItem> entrantItems = new ArrayList<>();
    private final List<String> entrantLabels = new ArrayList<>();
    private final Map<String, Marker> markersByUserId = new HashMap<>();

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
            if (position < 0 || position >= entrantItems.size()) return;

            EntrantMapItem selected = entrantItems.get(position);
            zoomToEntrant(selected);
        });

        setupMap();
        loadEntrantLocations();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.geo_map_container, mapFragment)
                .commit(); // <-- CHANGE: NO commitNow()

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

            String userId = (String) tag;
            for (int i = 0; i < entrantItems.size(); i++) {
                if (userId.equals(entrantItems.get(i).userId)) {
                    entrantListView.setItemChecked(i, true);
                    break;
                }
            }

            return false;
        });

        // In case data finished loading before the map was ready
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

        db.collection(WAITLIST_COLLECTION)
                .whereEqualTo(FIELD_EVENT_ID, eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();

                    if (docs.isEmpty()) {
                        showLoading(false);
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText("No entrant locations found for this event.");
                        return;
                    }

                    final int[] pendingLookups = {0};

                    for (DocumentSnapshot doc : docs) {
                        Double lat = getDoubleSafely(doc, FIELD_LATITUDE);
                        Double lng = getDoubleSafely(doc, FIELD_LONGITUDE);
                        String userId = doc.getString(FIELD_USER_ID);

                        if (lat == null || lng == null || TextUtils.isEmpty(userId)) {
                            continue;
                        }

                        pendingLookups[0]++;

                        final Double finalLat = lat;
                        final Double finalLng = lng;
                        final String finalUserId = userId;

                        db.collection(USERS_COLLECTION)
                                .document(finalUserId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString(FIELD_NAME);
                                    if (TextUtils.isEmpty(name)) {
                                        name = finalUserId;
                                    }

                                    EntrantMapItem item = new EntrantMapItem(
                                            finalUserId,
                                            name,
                                            finalLat,
                                            finalLng
                                    );

                                    entrantItems.add(item);
                                    entrantLabels.add(name);
                                    listAdapter.notifyDataSetChanged();
                                    renderMarkers();

                                    pendingLookups[0]--;
                                    if (pendingLookups[0] == 0) {
                                        finishLoadingState();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    EntrantMapItem item = new EntrantMapItem(
                                            finalUserId,
                                            finalUserId,
                                            finalLat,
                                            finalLng
                                    );

                                    entrantItems.add(item);
                                    entrantLabels.add(finalUserId);
                                    listAdapter.notifyDataSetChanged();
                                    renderMarkers();

                                    pendingLookups[0]--;
                                    if (pendingLookups[0] == 0) {
                                        finishLoadingState();
                                    }
                                });
                    }

                    if (pendingLookups[0] == 0) {
                        showLoading(false);
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText("No entrant locations found for this event.");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Failed to load entrant locations.");
                    Toast.makeText(requireContext(),
                            "Could not load map data.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void finishLoadingState() {
        showLoading(false);

        if (entrantItems.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No entrant locations found for this event.");
            return;
        }

        emptyText.setVisibility(View.GONE);

        // Zoom to first entrant automatically once everything is loaded
        zoomToEntrant(entrantItems.get(0));
    }

    private void renderMarkers() {
        if (googleMap == null) return;

        googleMap.clear();
        markersByUserId.clear();

        for (EntrantMapItem item : entrantItems) {
            LatLng position = new LatLng(item.latitude, item.longitude);

            Marker marker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(position)
                            .title(item.name)
                            .snippet("Joined event from this location")
            );

            if (marker != null) {
                marker.setTag(item.userId);
                markersByUserId.put(item.userId, marker);
            }
        }
    }

    private void zoomToEntrant(@NonNull EntrantMapItem item) {
        if (googleMap == null) return;

        LatLng target = new LatLng(item.latitude, item.longitude);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 14f));

        Marker marker = markersByUserId.get(item.userId);
        if (marker != null) {
            marker.showInfoWindow();
        }
    }

    private void clearCurrentData() {
        entrantItems.clear();
        entrantLabels.clear();
        markersByUserId.clear();

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

    @Nullable
    private Double getDoubleSafely(@NonNull DocumentSnapshot doc, @NonNull String field) {
        Object value = doc.get(field);

        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return null;
    }

    private static class EntrantMapItem {
        final String userId;
        final String name;
        final double latitude;
        final double longitude;

        EntrantMapItem(String userId, String name, double latitude, double longitude) {
            this.userId = userId;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}