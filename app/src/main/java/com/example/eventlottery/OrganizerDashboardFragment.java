package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

/**
 * OrganizerDashboardFragment
 *
 * Organizer UI landing page (Marketplace-style):
 * - Create an Event button
 * - List of user's events (placeholder cards for now)
 *
 * Later ties into:
 * - US 02.01.01 (create event + generate QR)
 * - US 02.02.xx (view entrants/waitlist)
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class OrganizerDashboardFragment extends Fragment {

    public OrganizerDashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_organizer_dashboard, container, false);

        Button btnCreate = root.findViewById(R.id.btn_create_event);
        btnCreate.setOnClickListener(v ->
                Snackbar.make(root, "Create Event not wired yet", Snackbar.LENGTH_SHORT).show()
        );

        return root;
    }
}