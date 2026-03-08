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
 * Role: UI host + navigation coordinator.
 * - Hosts a single fragment container and a BottomNavigationView.
 * - Switches between major entrant screens: Dashboard, Events (placeholder), Notifications, Profile.
 *
 * Related UI resources:
 * - activity_main.xml defines fragment_container + bottom_nav. (see activity_main.xml)
 * - menu_entrant_bottom_nav.xml defines nav items (Dashboard/Events/Notifications/Profile).
 *
 * User stories supported:
 * - Indirect support for all implemented entrant stories by providing navigation entry points:
 *   - US 01.02.02 (Update profile) via Profile tab
 *   - US 01.02.03 (Event history) via Profile tab → EventHistory
 *   - US 01.04.xx (Notification UI) via Notifications tab
 *
 * Notes:
 * - Events tab is currently a placeholder for US 01.06.01/01.06.02 (QR scan + join).
 */


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        // Default landing screen = Dashboard/Home
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

            // Repurposed tab: Events page
            if (id == R.id.nav_scan) {
                switchTo(new EventsFragment());
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

    private void switchTo(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}