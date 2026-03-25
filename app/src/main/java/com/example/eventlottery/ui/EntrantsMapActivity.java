package com.example.eventlottery.ui;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.eventlottery.R;
import java.util.List;

public class EntrantsMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<WaitListRecord> records;
    private FirestoreWaitListRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        repository = new FirestoreWaitListRepository();
        String eventId = getIntent().getStringExtra("eventId");

        repository.getRecordsByEventAsync(eventId, new WaitListRepository.WaitListCallBack() {
            @Override
            public void onSuccess(List<WaitListRecord> result) {
                records = result;

                SupportMapFragment mapFragment =
                        (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);

                mapFragment.getMapAsync(EntrantsMapActivity.this);
            }

            @Override
            public void onFailure(Exception e) {
                android.widget.Toast.makeText(
                        EntrantsMapActivity.this,
                        "Failed to load locations",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (records == null || records.isEmpty()) return; // prevent crash

        for (WaitListRecord record : records) {
            double lat = record.getLatitude();
            double lng = record.getLongitude();

            if (lat == 0 && lng == 0) continue;

            LatLng position = new LatLng(lat, lng);

            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(record.getDeviceId()));
        }

        // Move camera to first valid location
        for (WaitListRecord record : records) {
            if (record.getLatitude() != 0 && record.getLongitude() != 0) {
                LatLng pos = new LatLng(record.getLatitude(), record.getLongitude());
                mMap.moveCamera(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(pos, 10f)
                );
                break;
            }
        }
    }
}