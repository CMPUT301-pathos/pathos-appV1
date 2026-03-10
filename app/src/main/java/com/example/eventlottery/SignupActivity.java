package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;

/**
 * SignupActivity
 *
 * US 01.02.01: Create a profile (name/email/optional phone) stored in Firestore.
 * US 01.07.01: Profile is keyed by deviceId (no password login).
 *
 * Role selection is included in this screen (entrant/organizer/admin).
 */
public class SignupActivity extends AppCompatActivity {

    private FirestoreProfileRepository repo;
    private String deviceId;

    private EditText etName, etEmail, etPhone;
    private Button btnUser, btnAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        repo = new FirestoreProfileRepository();

        deviceId = getIntent().getStringExtra("deviceId");
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = DeviceIdentityService.getDeviceId(this);
        }

        etName = findViewById(R.id.signup_name);
        etEmail = findViewById(R.id.signup_email);
        etPhone = findViewById(R.id.signup_phone);

        btnUser = findViewById(R.id.btnSignUpUser);
        btnAdmin = findViewById(R.id.btnLoginAdmin);

        btnUser.setOnClickListener(v -> createProfileAndRoute("entrant"));
        btnAdmin.setOnClickListener(v -> createProfileAndRoute("admin"));
    }

    private void createProfileAndRoute(String role) {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim(); // optional

        if (name.isEmpty()) {
            etName.setError("Name required");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email required");
            return;
        }

        setButtonsEnabled(false);

        UserProfile profile = new UserProfile(deviceId, name, email, phone, role);

        // Upsert profile via saveProfile (Firestore .set)
        repo.saveProfile(profile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                Toast.makeText(SignupActivity.this,
                        "Profile created (" + role + ")",
                        Toast.LENGTH_SHORT).show();
                routeToRoleHome(role);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                setButtonsEnabled(true);
                Toast.makeText(SignupActivity.this,
                        "Create failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void routeToRoleHome(String role) {
        Intent i;
        if ("admin".equalsIgnoreCase(role)) {
            i = new Intent(this, AdminMainActivity.class);
        } else {
            i = new Intent(this, MainActivity.class);
        }
        startActivity(i);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnUser.setEnabled(enabled);
        btnAdmin.setEnabled(enabled);

        if (!enabled) {
            btnUser.setText("CREATING...");
            btnAdmin.setText("LOGGING IN...");
        } else {
            btnUser.setText("SIGN UP AS USER");
            btnAdmin.setText("LOG IN AS ADMIN");
        }
    }
}