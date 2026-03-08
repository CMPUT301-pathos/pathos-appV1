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

public class DashboardFragment extends Fragment {

    public DashboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        Button leave1 = view.findViewById(R.id.btnLeave1);
        Button leave2 = view.findViewById(R.id.btnLeave2);

        // These buttons exist in fragment_dashboard.xml (placeholder until waitlist logic exists)
        if (leave1 != null) {
            leave1.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Leave waitlist (not wired yet)", Toast.LENGTH_SHORT).show()
            );
        }

        if (leave2 != null) {
            leave2.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Leave waitlist (not wired yet)", Toast.LENGTH_SHORT).show()
            );
        }

        return view;
    }
}