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

/**
 * DashboardFragment
 *
 * Role: Entrant landing page (Dashboard).
 * - Currently a placeholder view for "upcoming events / waitlist summary".
 * - Later will be extended to show real event cards & waitlist state from repositories.
 *
 * User stories (future extension target):
 * - US 01.02.03 (History summary / past participation)
 * - US 01.05.04 (waiting list counts)
 *
 * Current status:
 * - UI placeholder only (no Firebase wiring yet).
 * @author: Kenneth Joseph
 * @version: 1.0
 * @see: menu_entrant_bottom_nav.xml
 */


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