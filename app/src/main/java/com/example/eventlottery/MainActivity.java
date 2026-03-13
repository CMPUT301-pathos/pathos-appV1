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
 * Hosts the main bottom-navigation experience for regular users and swaps
 * fragments into the fragment container.
 *
 * Tabs:
 * - Home (DashboardFragment)
 * - Events (EventsFragment)
 * - Organize (OrganizerDashboardFragment)
 * - Notifications (EntrantInvitationFragment)
 * - Profile (ProfileFragment)
 *
 * Behavior:
 * - Receives the user's profile completion state from RouterActivity
 * - Restricts access to protected tabs until the user's required profile
 *   information has been completed
 * - Opens the Profile tab first for incomplete profiles
 * - Starts a Firestore real-time listener for entrant event invitations
 * - Requests notification permission on Android 13+
 *
 * User stories supported:
 * - US 01.02.01: Entrant provides personal information
 * - US 01.02.02: Entrant updates profile information
 * - US 01.07.01: User is identified by device
 *
 * @author Kenneth Joseph, Fawaz Mansoor
 * @version 1.2
 */

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private NotificationListenerService notificationListenerService;
    private boolean profileCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileCompleted = getIntent().getBooleanExtra("profileCompleted", false);

        String deviceId = DeviceIdentityService.getDeviceId(this);
        notificationListenerService = new NotificationListenerService(this, deviceId);
        notificationListenerService.startListening();
        notificationListenerService.setOnInviteReceivedListener(eventName -> {
            runOnUiThread(() ->
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("🎉 You've been selected!")
                            .setMessage("You have been invited to join: " + eventName)
                            .setPositiveButton("View Notifications", (d, w) -> {
                                if (profileCompleted) {
                                    bottomNav.setSelectedItemId(R.id.nav_notifications);
                                } else {
                                    Toast.makeText(this,
                                            "Complete your profile first.",
                                            Toast.LENGTH_SHORT).show();
                                    bottomNav.setSelectedItemId(R.id.nav_profile);
                                }
                            })
                            .setNegativeButton("Later", null)
                            .show()
            );
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                    android.Manifest.permission.POST_NOTIFICATIONS
            }, 1001);
        }

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            if (profileCompleted) {
                switchTo(new DashboardFragment());
                bottomNav.setSelectedItemId(R.id.nav_home);
            } else {
                switchTo(new ProfileFragment());
                bottomNav.setSelectedItemId(R.id.nav_profile);
                Toast.makeText(this,
                        "Please complete your profile before using the app.",
                        Toast.LENGTH_LONG).show();
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                switchTo(new ProfileFragment());
                return true;
            }

            if (!profileCompleted) {
                Toast.makeText(this,
                        "Complete your profile first.",
                        Toast.LENGTH_SHORT).show();
                switchTo(new ProfileFragment());
                bottomNav.setSelectedItemId(R.id.nav_profile);
                return false;
            }

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

            Toast.makeText(this, "Unknown tab", Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileCompletionIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListenerService != null) {
            notificationListenerService.stopListening();
        }
    }

    private void refreshProfileCompletionIfNeeded() {
        String deviceId = DeviceIdentityService.getDeviceId(this);

        new com.example.eventlottery.firebase.FirestoreProfileRepository()
                .getProfile(deviceId, new com.example.eventlottery.data.ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(com.example.eventlottery.domain.UserProfile profile) {
                        if (profile != null) {
                            boolean wasCompleted = profileCompleted;
                            profileCompleted = profile.isProfileCompleted();

                            if (!wasCompleted && profileCompleted) {
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this,
                                            "Profile completed. App unlocked!",
                                            Toast.LENGTH_SHORT).show();
                                    switchTo(new DashboardFragment());
                                    bottomNav.setSelectedItemId(R.id.nav_home);
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });
    }

    private void switchTo(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}