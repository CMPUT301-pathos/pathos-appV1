package com.example.eventlottery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Teammate's profile button
        Button profileButton = findViewById(R.id.button_profile);
        profileButton.setOnClickListener(v -> {
            profileButton.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });
        Button eventHistoryButton = findViewById(R.id.button_event_history);
        eventHistoryButton.setOnClickListener(v -> {
            profileButton.setVisibility(View.GONE);
            eventHistoryButton.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EventHistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                profileButton.setVisibility(View.VISIBLE);
                // ADD THIS: Show event history button when back to main
                Button eventHistoryBtn = findViewById(R.id.button_event_history);
                if (eventHistoryBtn != null) {
                    eventHistoryBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                profileButton.setVisibility(View.VISIBLE);
            }
        });

        // Load invitation fragment with test data
        if (savedInstanceState == null) {
            EntrantInvitationFragment fragment = EntrantInvitationFragment.newInstance(
                    "test_event_123",
                    "Test Event",
                    "test_device_456"
            );
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}