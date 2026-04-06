package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.eventlottery.admin.AdminBrowseEventsActivity;
import com.example.eventlottery.admin.AdminBrowseImages;
import com.example.eventlottery.admin.AdminBrowseUsersActivity;
import com.example.eventlottery.admin.AdminCommentModerationActivity;
import com.example.eventlottery.admin.AdminNotificationLogs;
import com.example.eventlottery.admin.AdminPolicyViolationsActivity;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Admin dashboard activity for viewing app analytics and navigating admin tools.
 *
 * This activity displays counts for events, users, organizers, and policy
 * violations, and provides navigation to admin-specific sections of the app.
 */
public class AdminMainActivity extends AppCompatActivity {

    private TextView tvEventsCount, tvUsersCount, tvOrganizersCount, tvPolicyViolations;
    private CardView cardBrowseEvents, cardBrowseUsers, cardBrowseImages,
            cardNotificationLogs, cardPolicyDetails, cardCommentModeration;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNav;
    private String currentDeviceId;

    /**
     * Sets up the admin dashboard activity and initializes admin data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        db = FirebaseFirestore.getInstance();
        currentDeviceId = getIntent().getStringExtra("deviceId");

        initViews();
        setupClickListeners();
        loadStatistics();
        loadAdminInfo();
        setupBottomNavigation();
    }

    /**
     * Finds and stores references to all UI elements used by the admin dashboard.
     */
    private void initViews() {
        tvEventsCount = findViewById(R.id.tvEventsCount);
        tvUsersCount = findViewById(R.id.tvUsersCount);
        tvOrganizersCount = findViewById(R.id.tvOrganizersCount);
        tvPolicyViolations = findViewById(R.id.tvPolicyViolations);
        cardBrowseEvents = findViewById(R.id.cardBrowseEvents);
        cardBrowseUsers = findViewById(R.id.cardBrowseUsers);
        cardBrowseImages = findViewById(R.id.cardBrowseImages);
        cardNotificationLogs = findViewById(R.id.cardNotificationLogs);
        cardPolicyDetails = findViewById(R.id.cardPolicyDetails);
        cardCommentModeration = findViewById(R.id.cardCommentModeration);
        bottomNav = findViewById(R.id.bottom_nav_admin);
    }

    /**
     * Hooks the bottom navigation bar into the admin and regular user flows.
     *
     * Selecting a regular user tab routes back to the normal MainActivity,
     * while the admin dashboard remains selected when already active.
     */
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Map admin menu IDs to entrant menu IDs
            if (id == R.id.nav_home || id == R.id.nav_events ||
                    id == R.id.nav_organizer || id == R.id.nav_notifications ||
                    id == R.id.nav_profile) {

                // Navigate back to MainActivity with the selected tab
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("deviceId", currentDeviceId);
                intent.putExtra("selectedTab", id);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            }

            if (id == R.id.nav_admin_dashboard) {
                // Already on admin dashboard
                return true;
            }

            return false;
        });

        // Highlight the admin dashboard item
        bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
    }

    /**
     * Registers click listeners for all admin cards to launch supporting admin screens.
     */
    private void setupClickListeners() {
        cardBrowseEvents.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseEventsActivity.class)));

        cardBrowseUsers.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseUsersActivity.class)));

        cardBrowseImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseImages.class)));

        cardNotificationLogs.setOnClickListener(v ->
                startActivity(new Intent(this, AdminNotificationLogs.class)));

        cardPolicyDetails.setOnClickListener(v ->
                startActivity(new Intent(this, AdminPolicyViolationsActivity.class)));

        cardCommentModeration.setOnClickListener(v ->
                startActivity(new Intent(this, AdminCommentModerationActivity.class)));

    }
    /**
     * Resets the displayed policy violations count to zero.
     *
     * This method is currently used for demo purposes only.
     */
    private void resetDisplayedCount() {
        tvPolicyViolations.setText("0");
        Toast.makeText(this, "Count reset to 0 for demo", Toast.LENGTH_SHORT).show();
    }
    /**
     * Loads admin metrics from Firestore and displays them in the dashboard.
     *
     * This includes counts for events, users, organizers, and policy violations.
     */
    private void loadStatistics() {
        db.collection("events")
                .get()
                .addOnSuccessListener(query ->
                        tvEventsCount.setText(String.valueOf(query.size())))
                .addOnFailureListener(e -> Log.e("AdminMain", "Error loading events", e));

        db.collection("users")
                .get()
                .addOnSuccessListener(query ->
                        tvUsersCount.setText(String.valueOf(query.size())))
                .addOnFailureListener(e -> Log.e("AdminMain", "Error loading users", e));

        db.collection("users")
                .whereEqualTo("role", "organizer")
                .get()
                .addOnSuccessListener(query ->
                        tvOrganizersCount.setText(String.valueOf(query.size())))
                .addOnFailureListener(e -> Log.e("AdminMain", "Error loading organizers", e));

        db.collection("policy_violations")
                .whereEqualTo("violationType", "User Violation")
                .get()
                .addOnSuccessListener(snapshot -> {
                    tvPolicyViolations.setText(String.valueOf(snapshot.size()));
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminMain", "Error loading policy violations", e);
                    tvPolicyViolations.setText("0");
                });
    }

    /**
     * Loads the current admin user's profile information and updates the header.
     */
    private void loadAdminInfo() {
        if (currentDeviceId != null) {
            new FirestoreProfileRepository().getProfile(currentDeviceId, new com.example.eventlottery.data.ProfileRepository.ProfileCallback() {
                @Override
                public void onSuccess(UserProfile profile) {
                    if (profile != null) {
                        TextView tvAdminName = findViewById(R.id.tvAdminName);
                        TextView tvAdminEmail = findViewById(R.id.tvAdminEmail);
                        if (tvAdminName != null) tvAdminName.setText(profile.getName());
                        if (tvAdminEmail != null) tvAdminEmail.setText(profile.getEmail());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("AdminMain", "Error loading admin info", e);
                }
            });
        }
    }

    /**
     * Refreshes the dashboard metrics each time the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    /**
     * Handles the action bar back/up button by closing the admin activity.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}