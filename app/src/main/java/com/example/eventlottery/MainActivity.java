package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.service.NotificationListenerService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private NotificationListenerService notificationListenerService;
    private boolean profileCompleted;
    private String currentDeviceId;

    /**
     * Sets up the main activity, bottom navigation, notification listener,
     * and shows a welcome toast if the user's profile is incomplete.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileCompleted = getIntent().getBooleanExtra("profileCompleted", false);
        currentDeviceId = DeviceIdentityService.getDeviceId(this);

        notificationListenerService = new NotificationListenerService(this, currentDeviceId);
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                    android.Manifest.permission.POST_NOTIFICATIONS
            }, 1001);
        }

        bottomNav = findViewById(R.id.bottom_nav);

        // Setup bottom navigation
        setupBottomNavigation();

        if (savedInstanceState == null) {
            switchTo(new DashboardFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);

            if (!profileCompleted) {
                Toast.makeText(this,
                        "Complete your profile to join or create events.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Configures bottom navigation bar items and wires fragment navigation.
     *
     * The admin tab is hidden by default and shown only if the user has
     * admin privileges. Navigation selections trigger fragment switches via
     * updateFragment().
     */
    private void setupBottomNavigation() {
        // First, hide admin tab by default
        MenuItem adminItem = bottomNav.getMenu().findItem(R.id.nav_admin);
        if (adminItem != null) {
            adminItem.setVisible(false);
        }

        // Check if user is admin
        checkAdminRole();

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

            if (id == R.id.nav_admin) {
                Intent intent = new Intent(this, AdminMainActivity.class);
                intent.putExtra("deviceId", currentDeviceId);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    /**
     * Checks the current user's profile role and shows the admin tab if they
     * have admin privileges.
     */
    private void checkAdminRole() {
        new FirestoreProfileRepository().getProfile(currentDeviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                runOnUiThread(() -> {
                    if (profile != null && "admin".equalsIgnoreCase(profile.getRole())) {
                        MenuItem adminItem = bottomNav.getMenu().findItem(R.id.nav_admin);
                        if (adminItem != null) {
                            adminItem.setVisible(true);
                            Toast.makeText(MainActivity.this, "Admin mode enabled", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Not admin, keep admin tab hidden
            }
        });
    }

    /**
     * Refreshes the profile completion status when the activity resumes,
     * and rechecks admin role.
     *
     * Shows a success toast if the profile was incomplete but is now completed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileCompletionIfNeeded();
        // Re-check admin role when returning
        checkAdminRole();
    }

    /**
     * Stops the notification listener service when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListenerService != null) {
            notificationListenerService.stopListening();
        }
    }

    /**
     * Queries the user's profile to check if it has been completed,
     * updating the status and showing a completion notification if needed.
     */
    private void refreshProfileCompletionIfNeeded() {
        new FirestoreProfileRepository()
                .getProfile(currentDeviceId, new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(UserProfile profile) {
                        if (profile != null) {
                            boolean wasCompleted = profileCompleted;
                            profileCompleted = profile.isProfileCompleted();

                            if (!wasCompleted && profileCompleted) {
                                runOnUiThread(() ->
                                        Toast.makeText(MainActivity.this,
                                                "Profile completed. App unlocked!",
                                                Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });
    }

    /**
     * Replaces the current fragment container content with the specified fragment.
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