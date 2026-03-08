package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.controller.ProfileController;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;

/**
 * ProfileFragment (Account Info)
 *
 * Role: Entrant profile & account screen.
 * - Shows editable profile fields (name/email/phone).
 * - Provides "Save changes" UI (update profile).
 * - Provides notification opt-out UI toggle (placeholder if not persisted yet).
 * - Provides Account Security actions (Logout placeholder + Delete Account).
 * - Provides entry point to EventHistoryFragment.
 *
 * User stories supported:
 * - US 01.02.01/US 01.02.02: Provide/update personal info (name/email/optional phone).
 * - US 01.02.04: Delete profile (delete account button).
 * - US 01.02.03: View event history (button navigates to history screen).
 * - US 01.04.03: Opt out of notifications (toggle UI; persistence may be TODO).
 */

/**
 * Combined Account Info page:
 * - Edit profile fields (US 01.02.02)
 * - Event History entry point (US 01.02.03) via EventHistoryFragment
 * - Notification opt-out placeholder (US 01.04.03 placeholder)
 * - Logout/Delete buttons (delete wired, logout placeholder)
 *
 * Author: Dmitriy Limanets, Kenneth Joseph
 */
public class ProfileFragment extends Fragment {

    private ProfileController profileController;
    private String deviceId;

    private EditText editName, editEmail, editPhone;
    private Button saveButton, logoutButton, deleteButton, eventHistoryButton;
    private Switch optOutSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileController = new ProfileController(new FirestoreProfileRepository());
        deviceId = DeviceIdentityService.getDeviceId(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind views
        editName = view.findViewById(R.id.edit_name);
        editEmail = view.findViewById(R.id.edit_email);
        editPhone = view.findViewById(R.id.edit_phone);

        saveButton = view.findViewById(R.id.button_save_profile);
        logoutButton = view.findViewById(R.id.button_logout);
        deleteButton = view.findViewById(R.id.button_delete_account);
        eventHistoryButton = view.findViewById(R.id.button_event_history);
        optOutSwitch = view.findViewById(R.id.switch_opt_out);

        // Load current profile data
        loadCurrentProfile();

        // Save changes
        saveButton.setOnClickListener(v -> saveProfile());

        // Opt-out placeholder
        optOutSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(),
                    isChecked ? "Opt-out enabled (not wired yet)" : "Opt-out disabled (not wired yet)",
                    Toast.LENGTH_SHORT).show();
        });

        // Event history
        eventHistoryButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EventHistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Logout placeholder
        logoutButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Logout not wired yet", Toast.LENGTH_SHORT).show()
        );

        // Delete account (wired)
        deleteButton.setOnClickListener(v -> showDeleteConfirmDialog());

        return view;
    }

    private void loadCurrentProfile() {
        profileController.getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile != null) {
                    editName.setText(profile.getName());
                    editEmail.setText(profile.getEmail());
                    editPhone.setText(profile.getPhoneNumber());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Name is required");
            return;
        }
        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("SAVING...");

        profileController.updateProfile(deviceId, name, email, phone, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                saveButton.setEnabled(true);
                saveButton.setText("SAVE CHANGES");
                Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                saveButton.setEnabled(true);
                saveButton.setText("SAVE CHANGES");
                Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    profileController.deleteProfile(deviceId, new ProfileRepository.ProfileCallback() {
                        @Override
                        public void onSuccess(UserProfile profile) {
                            Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}