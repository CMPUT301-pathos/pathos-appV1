package com.example.eventlottery;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity
 *
 * Hosts BottomNavigationView and swaps fragments into fragment_container.
 *
 * Responsibilities:
 * - Handle bottom navigation tab selection
 * - Load the default landing fragment on app start
 * - Swap fragments dynamically based on selected tab
 *
 * Tabs:
 * - Home (DashboardFragment)
 * - Events (EventsFragment)
 * - Organize (OrganizerDashboardFragment)  <-- NEW
 * - Notifications (EntrantInvitationFragment)
 * - Profile (ProfileFragment)
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    /** Bottom navigation bar controlling fragment switching. */
    private BottomNavigationView bottomNav;

    /**
     * Lifecycle method called when the activity is created.
     * Sets up the layout, initializes the BottomNavigationView,
     * and loads the default fragment (Home/Dashboard).
     *
     * @param savedInstanceState saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        // Default landing screen = Home
        if (savedInstanceState == null) {
            switchTo(new DashboardFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                switchTo(new DashboardFragment());
                return true;
            }

            if (id == R.id.nav_events) {
                switchTo(new EventsFragment());
                return true;
            }

            // NEW: Organizer tab
            if (id == R.id.nav_organizer) {
                switchTo(new OrganizerDashboardFragment());
                return true;
            }

            if (id == R.id.nav_notifications) {
                switchTo(new EntrantInvitationFragment());
                return true;
            }

            if (id == R.id.nav_profile) {
                switchTo(new ProfileFragment());
                return true;
            }

            Toast.makeText(this, "Unknown tab", Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    /**
     * Replaces the current fragment in the container with the specified fragment.
     *
     * @param fragment fragment to display
     */
    private void switchTo(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}