package com.example.eventlottery.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.eventlottery.DashboardFragment;
import com.example.eventlottery.MainActivity;
import com.example.eventlottery.R;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Admin main activity with ability to switch between admin, organizer, and entrant roles.
 * Supports US 03.09.01 - Admin can act as organizer and entrant.
 */
public class AdminMainActivity extends AppCompatActivity {

    private CardView cardBrowseUsers, cardBrowseEvents, cardBrowseImages, 
                    cardNotificationLogs, cardPolicyDetails, cardCommentModeration,
                    cardSwitchToEntrant, cardSwitchToOrganizer;
    private TextView tvAdminName, tvAdminEmail, tvCurrentRole;
    private Button btnLogout;
    
    private String adminDeviceId;
    private FirebaseFirestore db;
    private FirestoreProfileRepository profileRepository;
    private UserProfile adminProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Get admin device ID
        adminDeviceId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("deviceId", "");
        
        db = FirebaseFirestore.getInstance();
        profileRepository = new FirestoreProfileRepository();

        initViews();
        loadAdminProfile();
        setupClickListeners();
        loadStatistics();
    }

    private void initViews() {
        cardBrowseUsers = findViewById(R.id.cardBrowseUsers);
        cardBrowseEvents = findViewById(R.id.cardBrowseEvents);
        cardBrowseImages = findViewById(R.id.cardBrowseImages);
        cardNotificationLogs = findViewById(R.id.cardNotificationLogs);
        cardPolicyDetails = findViewById(R.id.cardPolicyDetails);
        cardCommentModeration = findViewById(R.id.cardCommentModeration);
        cardSwitchToEntrant = findViewById(R.id.cardSwitchToEntrant);
        cardSwitchToOrganizer = findViewById(R.id.cardSwitchToOrganizer);
        tvAdminName = findViewById(R.id.tvAdminName);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvCurrentRole = findViewById(R.id.tvCurrentRole);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadAdminProfile() {
        profileRepository.getProfile(adminDeviceId, new FirestoreProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile != null) {
                    adminProfile = profile;
                    tvAdminName.setText(profile.getName() != null ? profile.getName() : "Admin User");
                    tvAdminEmail.setText(profile.getEmail() != null ? profile.getEmail() : "admin@example.com");
                    tvCurrentRole.setText("Current Role: " + (profile.getRole() != null ? profile.getRole().toUpperCase() : "ADMIN"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminMainActivity.this, 
                    "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStatistics() {
        // Load event count
        db.collection("events").get().addOnSuccessListener(snapshot -> {
            TextView tvEventsCount = findViewById(R.id.tvEventsCount);
            tvEventsCount.setText(String.valueOf(snapshot.size()));
        });

        // Load user count
        db.collection("users").get().addOnSuccessListener(snapshot -> {
            TextView tvUsersCount = findViewById(R.id.tvUsersCount);
            tvUsersCount.setText(String.valueOf(snapshot.size()));
        });

        // Load organizer count
        db.collection("users").whereEqualTo("role", "organizer").get().addOnSuccessListener(snapshot -> {
            TextView tvOrganizersCount = findViewById(R.id.tvOrganizersCount);
            tvOrganizersCount.setText(String.valueOf(snapshot.size()));
        });

        // Load policy violations count
        db.collection("policy_violations").get().addOnSuccessListener(snapshot -> {
            TextView tvPolicyViolations = findViewById(R.id.tvPolicyViolations);
            tvPolicyViolations.setText(String.valueOf(snapshot.size()));
        });
    }

    private void setupClickListeners() {
        // Admin features
        cardBrowseUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminBrowseUsersActivity.class));
        });

        cardBrowseEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminBrowseEventsActivity.class));
        });

        cardBrowseImages.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminBrowseImages.class));
        });

        cardNotificationLogs.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminNotificationLogs.class));
        });

        cardPolicyDetails.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminPolicyViolationsActivity.class));
        });

        // US 03.10.01 - Comment Moderation
        cardCommentModeration.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminCommentModerationActivity.class));
        });

        // US 03.09.01 - Switch to Entrant Role
        cardSwitchToEntrant.setOnClickListener(v -> {
            switchToRole("entrant");
        });

        // US 03.09.01 - Switch to Organizer Role
        cardSwitchToOrganizer.setOnClickListener(v -> {
            switchToRole("organizer");
        });

        btnLogout.setOnClickListener(v -> {
            // Clear saved device ID
            getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
            
            // Go to login screen
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void switchToRole(String role) {
        if (adminProfile == null) {
            Toast.makeText(this, "Loading profile, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the profile to the new role
        adminProfile.setRole(role);
            adminProfile.refreshProfileCompleted();
        
        // Save to Firestore
        profileRepository.saveProfile(adminProfile, new FirestoreProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                Toast.makeText(AdminMainActivity.this, 
                    "Switched to " + role.toUpperCase() + " mode", Toast.LENGTH_LONG).show();
                
                // Launch the main app as the new role
                Intent intent = new Intent(AdminMainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminMainActivity.this, 
                    "Error switching role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Revert the role change
                adminProfile.setRole("admin");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile in case role was changed
        loadAdminProfile();
        loadStatistics();
    }
}
