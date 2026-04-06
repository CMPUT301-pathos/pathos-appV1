package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;

/**
 * RouterActivity (Launcher)
 *
 * US 01.07.01: Identify user by device (no username/password)
 * US 01.02.01 / 01.02.02: User may provide/update profile info later
 *
 * Flow:
 * - get deviceId
 * - look up profile in Firestore
 * - if found -> route based on role
 * - if not found -> create blank profile and then route
 *
 * @author Kenneth Joseph
 * @version 1.3
 */
public class RouterActivity extends AppCompatActivity {

    private FirestoreProfileRepository repo;

    /**
     * Launcher activity that routes the user to their appropriate home screen.
     *
     * Retrieves the device ID, looks up the corresponding profile in Firestore,
     * and delegates to role-based routing. If the profile doesn't exist, creates
     * a default blank profile before routing. Logs device ID for debugging.
     *
     * @param savedInstanceState the saved instance state from previous activity lifecycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router);

        repo = new FirestoreProfileRepository();

        String deviceId = DeviceIdentityService.getDeviceId(this);
        Log.d("MY_DEVICE_ID", "=====================================");
        Log.d("MY_DEVICE_ID", "YOUR DEVICE ID IS: " + deviceId);
        Log.d("MY_DEVICE_ID", "=====================================");
        // ADD THIS TOAST TO SEE YOUR DEVICE ID

        Toast.makeText(this, "Your Device ID: " + deviceId, Toast.LENGTH_LONG).show();

        repo.getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile == null) {
                    createDefaultProfile(deviceId);
                    return;
                }

                routeToRoleHome(profile);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RouterActivity.this,
                        "Profile lookup failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Optional fallback: try creating a default profile anyway
                createDefaultProfile(deviceId);
            }
        });

    }

    /**
     * Creates and saves a new default profile in Firestore with empty fields.
     *
     * Initializes a blank UserProfile with user role, marks it as incomplete,
     * enables notifications, and asynchronously persists it. Routes to appropriate
     * home screen upon successful creation.
     *
     * @param deviceId the unique device identifier for the new profile
     */
    private void createDefaultProfile(String deviceId) {
        UserProfile newProfile = new UserProfile(deviceId, "", "", "", "user");
        newProfile.setProfileCompleted(false);
        newProfile.setNotificationsEnabled(true);

        repo.saveProfile(newProfile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                routeToRoleHome(profile);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RouterActivity.this,
                        "Failed to create profile: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    /**
     * Routes the user to their role-appropriate home screen.
     *
     * Selects AdminMainActivity for admin users or MainActivity for regular users,
     * passes the device ID and profile completion status, and starts the activity.
     *
     * @param profile the user's profile containing role and completion status
     */
    private void routeToRoleHome(UserProfile profile) {
        Intent i;

        if ("admin".equalsIgnoreCase(profile.getRole())) {
            i = new Intent(this, AdminMainActivity.class);
        } else {
            i = new Intent(this, MainActivity.class);
        }

        i.putExtra("deviceId", profile.getDeviceId());
        i.putExtra("profileCompleted", profile.isProfileCompleted());

        startActivity(i);
        finish();
    }
}