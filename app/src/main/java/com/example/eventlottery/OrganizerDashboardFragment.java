package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

/**
 * OrganizerDashboardFragment
 *
 * Organizer tools landing page:
 * - Create an Event button (US 02.01.01)
 * - List of user's events (placeholder for now)
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class OrganizerDashboardFragment extends Fragment {

    public OrganizerDashboardFragment() { }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_organizer_dashboard, container, false);

        View btn = root.findViewById(R.id.btn_create_event);
        btn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CreateEventFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }
}