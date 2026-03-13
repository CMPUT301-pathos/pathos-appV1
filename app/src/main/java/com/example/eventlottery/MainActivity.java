package com.example.eventlottery;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.service.NotificationListenerService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity
 *
 * Hosts BottomNavigationView and swaps fragments into fragment_container.
 *
 * Tabs:
 * - Home (DashboardFragment)
 * - Events (EventsFragment)
 * - Organize (OrganizerDashboardFragment)
 * - Notifications (EntrantInvitationFragment)
 * - Profile (ProfileFragment)
 *
 * Also starts a Firestore real-time listener for push-style notifications
 * when the entrant is invited to an event.
 *
 * @author Kenneth Joseph, Fawaz Mansoor
 * @version 1.1
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private NotificationListenerService notificationListenerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start real-time notification listener
        String deviceId = DeviceIdentityService.getDeviceId(this);
        notificationListenerService = new NotificationListenerService(this, deviceId);
        notificationListenerService.startListening();
        notificationListenerService.setOnInviteReceivedListener(eventName -> {
            runOnUiThread(() ->
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("🎉 You've been selected!")
                            .setMessage("You have been invited to join: " + eventName)
                            .setPositiveButton("View Notifications", (d, w) -> {
                                bottomNav.setSelectedItemId(R.id.nav_notifications);
                            })
                            .setNegativeButton("Later", null)
                            .show()
            );
        });

        // Request notification permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                    android.Manifest.permission.POST_NOTIFICATIONS
            }, 1001);
        }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListenerService != null) {
            notificationListenerService.stopListening();
        }
    }

    private void switchTo(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}