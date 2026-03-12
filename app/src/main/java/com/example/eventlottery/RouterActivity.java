package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
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
 * US 01.02.01: If no profile exists, route to SignupActivity to create it
 *
 * Responsibilities:
 *  * - Identify the device using {@link DeviceIdentityService}.
 *  * - Lookup existing profile in Firestore via {@link FirestoreProfileRepository}.
 *  * - Route users according to their role:
 *  *      - "entrant" -> {@link MainActivity}
 *  *      - "admin" -> {@link AdminMainActivity}
 *  * - If no profile exists, launch {@link SignupActivity} to create a profile.
 *  * - Handles fallback if Firestore lookup fails (routes to SignupActivity).
 *
 * Flow:
 * - get deviceId
 * - look up profile in Firestore
 * - if found -> route based on role (entrant/organizer/admin)
 * - else -> SignupActivity
 *
 * @author Kenneth Joseph
 * @version 1.1
 */
public class RouterActivity extends AppCompatActivity {

    private FirestoreProfileRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router);

        repo = new FirestoreProfileRepository();

        String deviceId = DeviceIdentityService.getDeviceId(this);

        repo.getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile == null) {
                    Intent i = new Intent(RouterActivity.this, SignupActivity.class);
                    i.putExtra("deviceId", deviceId);
                    startActivity(i);
                    finish();
                    return;
                }

                // Route existing user based on role
                String role = profile.getRole();
                routeToRoleHome(role);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RouterActivity.this,
                        "Profile lookup failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Still allow signup as fallback
                Intent i = new Intent(RouterActivity.this, SignupActivity.class);
                i.putExtra("deviceId", deviceId);
                startActivity(i);
                finish();
            }
        });
    }

    /**
     * Routes the user to the appropriate main activity based on role.
     *
     * @param role The user's role string from profile
     */
    private void routeToRoleHome(String role) {
        Intent i;
        if ("admin".equalsIgnoreCase(role)) {
            i = new Intent(this, AdminMainActivity.class);
        } else {
            i = new Intent(this, MainActivity.class); // default entrant
        }
        startActivity(i);
    }
}