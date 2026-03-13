package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.controller.ProfileController;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ProfileFragment (Account Info)
 *
 * Role: Entrant profile & account screen.
 * - Shows editable profile fields (name/email/phone).
 * - Provides "Save changes" UI (update profile).
 * - Provides notification opt-out toggle (wired to Firestore).
 * - Provides Account Security actions (Logout placeholder + Delete Account).
 * - Provides entry point to EventHistoryFragment.
 *
 * User stories supported:
 * - US 01.02.01/US 01.02.02: Provide/update personal info (name/email/optional phone).
 * - US 01.02.04: Delete profile (delete account button).
 * - US 01.02.03: View event history (button navigates to history screen).
 * - US 01.04.03: Opt out of notifications (wired to Firestore via ProfileController).
 * - US 01.05.05: View lottery criteria per event (via EventsFragment).
 *
 * @author Dmitriy Limanets, Kenneth Joseph, Fawaz Mansoor
 * @version 1.1
 */
public class ProfileFragment extends Fragment {

    private ProfileController profileController;
    private String deviceId;

    private EditText editName, editEmail, editPhone;
    private Button saveButton, deleteButton, eventHistoryButton;
    private Switch optOutSwitch;

    // Add these:
    private CircleImageView profileImage;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileController = new ProfileController(
                new FirestoreProfileRepository(),
                new com.example.eventlottery.firebase.FirestoreEventRepository()
        );
        deviceId = DeviceIdentityService.getDeviceId(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        // Bind views
        editName = view.findViewById(R.id.edit_name);
        editEmail = view.findViewById(R.id.edit_email);
        editPhone = view.findViewById(R.id.edit_phone);

        profileImage = view.findViewById(R.id.profile_image);
        TextView changePhotoText = view.findViewById(R.id.change_photo_text);


        saveButton = view.findViewById(R.id.button_save_profile);
        deleteButton = view.findViewById(R.id.button_delete_account);
        eventHistoryButton = view.findViewById(R.id.button_event_history);
        optOutSwitch = view.findViewById(R.id.switch_opt_out);

        // Load current profile data and wire switch listener after loading
        loadCurrentProfile();

        // Save changes
        saveButton.setOnClickListener(v -> saveProfile());

        // Event history
        eventHistoryButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EventHistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImage.setImageURI(uri);
                        profileController.updateProfilePhoto(deviceId, uri.toString(),
                                new ProfileRepository.ProfileCallback() {
                                    @Override
                                    public void onSuccess(UserProfile profile) {
                                        Toast.makeText(getContext(), "Photo updated!", Toast.LENGTH_SHORT).show();
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(getContext(), "Failed to save photo", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
        );

        View.OnClickListener photoClickListener = v -> pickImageLauncher.launch("image/*");
        profileImage.setOnClickListener(photoClickListener);
        changePhotoText.setOnClickListener(photoClickListener);
        // Delete account
        deleteButton.setOnClickListener(v -> showDeleteConfirmDialog());

        return view;
    }

    private void loadCurrentProfile() {
        profileController.getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (getActivity() == null) return;
                if (profile != null) {
                    editName.setText(profile.getName());
                    editEmail.setText(profile.getEmail());
                    editPhone.setText(profile.getPhoneNumber());

                    // Load saved profile photo
                    if (profile.getProfilePhotoUri() != null && !profile.getProfilePhotoUri().isEmpty()) {
                        profileImage.setImageURI(android.net.Uri.parse(profile.getProfilePhotoUri()));
                    }

                    optOutSwitch.setOnCheckedChangeListener(null);
                    optOutSwitch.setChecked(!profile.isNotificationsEnabled());
                    optOutSwitch.jumpDrawablesToCurrentState();
                }

                optOutSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    boolean notificationsEnabled = !isChecked;
                    android.content.Context appContext = requireContext().getApplicationContext();
                    profileController.setNotificationsEnabled(deviceId, notificationsEnabled,
                            new ProfileRepository.ProfileCallback() {
                                @Override
                                public void onSuccess(UserProfile updatedProfile) {
                                    String msg = notificationsEnabled
                                            ? "Notifications enabled ✓"
                                            : "Notifications disabled — you won't receive lottery alerts.";
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                            Toast.makeText(appContext, msg, Toast.LENGTH_LONG).show()
                                    );
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                        Toast.makeText(appContext,
                                                "Failed to update notification preference",
                                                Toast.LENGTH_SHORT).show();
                                        optOutSwitch.setOnCheckedChangeListener(null);
                                        optOutSwitch.setChecked(!isChecked);
                                        optOutSwitch.jumpDrawablesToCurrentState();
                                    });
                                }
                            });
                });
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                Toast.makeText(getActivity(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        editName.setError(null);
        editEmail.setError(null);

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

                if (profile != null && profile.isProfileCompleted()) {
                    Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Profile saved, but still incomplete.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                saveButton.setEnabled(true);
                saveButton.setText("SAVE CHANGES");
                Toast.makeText(getContext(),
                        "Failed to update profile: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    profileController.deleteProfileAndOwnedEvents(deviceId, new ProfileRepository.ProfileCallback() {
                        @Override
                        public void onSuccess(UserProfile profile) {
                            Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                            requireActivity().finishAffinity();
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