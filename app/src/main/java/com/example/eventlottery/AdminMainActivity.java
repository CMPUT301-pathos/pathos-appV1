package com.example.eventlottery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Admin Main Dashboard Activity
 * This activity serves as the central hub for all administrative functions.
 *
 * @author hasratsinghchauhan
 * @version 1.0
 */
public class AdminMainActivity extends AppCompatActivity {

    private TextView tvEventsCount, tvUsersCount, tvOrganizersCount, tvPolicyViolations;
    private CardView cardBrowseEvents, cardBrowseUsers, cardBrowseImages,
            cardNotificationLogs, cardPolicyDetails, cardCommentModeration;
    private Button btnLogout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        loadStatistics();
        loadAdminInfo();
    }

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
        btnLogout = findViewById(R.id.btnLogout);
    }

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

        btnLogout.setOnClickListener(v -> {
            // Logout and go back to RouterActivity
            Intent intent = new Intent(this, RouterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadStatistics() {
        // Count events
        db.collection("events")
                .get()
                .addOnSuccessListener(query ->
                        tvEventsCount.setText(String.valueOf(query.size())))
                .addOnFailureListener(e -> Log.e("AdminMain", "Error loading events", e));

        // Count users
        db.collection("users")
                .get()
                .addOnSuccessListener(query ->
                        tvUsersCount.setText(String.valueOf(query.size())))
                .addOnFailureListener(e -> Log.e("AdminMain", "Error loading users", e));

        // Count organizers
        db.collection("users")
                .whereEqualTo("role", "organizer")
                .get()
                .addOnSuccessListener(query ->
                        tvOrganizersCount.setText(String.valueOf(query.size())))
                .addOnFailureListener(e -> Log.e("AdminMain", "Error loading organizers", e));

        // Load ONLY user policy violations (where violationType is "User Violation")
        db.collection("policy_violations")
                //.whereEqualTo("violationType", "User Violation")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int count = snapshot.size();
                    tvPolicyViolations.setText(String.valueOf(count));
                    Log.d("AdminMain", "User policy violations loaded: " + count);
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminMain", "Error loading policy violations", e);
                    tvPolicyViolations.setText("0");
                });
    }

    private void loadAdminInfo() {
        String deviceId = getIntent().getStringExtra("deviceId");
        if (deviceId != null) {
            new FirestoreProfileRepository().getProfile(deviceId, new com.example.eventlottery.data.ProfileRepository.ProfileCallback() {
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

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
