package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventsFragment extends Fragment {

    public EventsFragment() {
        // Required empty public constructor
    }

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

        return root;
    }
}