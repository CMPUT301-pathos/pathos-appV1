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

    /**
     * Initializes the fragment and its dependencies.
     *
     * Sets up the ProfileController with Firestore-backed repositories and retrieves
     * the unique device identifier for profile management.
     *
     * @param savedInstanceState the saved instance state from previous fragment lifecycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileController = new ProfileController(
                new FirestoreProfileRepository(),
                new com.example.eventlottery.firebase.FirestoreEventRepository()
        );
        deviceId = DeviceIdentityService.getDeviceId(requireContext());
    }

    /**
     * Creates and returns the fragment's view hierarchy.
     *
     * Inflates the profile layout, binds all UI elements (name/email/phone fields,
     * profile image, buttons, notification switch), loads the current profile data,
     * and wires all click listeners for save, delete, history, and photo selection.
     *
     * @param inflater the LayoutInflater to inflate the layout
     * @param container the parent ViewGroup
     * @param savedInstanceState the saved instance state
     * @return the root view of the fragment layout
     */
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
                        try {
                            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media
                                    .getBitmap(requireContext().getContentResolver(), uri);
                            int maxWidth = 300;
                            if (bitmap.getWidth() > maxWidth) {
                                float scale = (float) maxWidth / bitmap.getWidth();
                                int newHeight = Math.round(bitmap.getHeight() * scale);
                                bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
                            }
                            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
                            String base64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);
                            String photoData = "data:image/jpeg;base64," + base64;
                            profileImage.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(
                                    baos.toByteArray(), 0, baos.toByteArray().length));
                            profileController.updateProfilePhoto(deviceId, photoData,
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
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        View.OnClickListener photoClickListener = v -> pickImageLauncher.launch("image/*");
        profileImage.setOnClickListener(photoClickListener);
        changePhotoText.setOnClickListener(photoClickListener);
        // Delete account
        deleteButton.setOnClickListener(v -> showDeleteConfirmDialog());

        return view;
    }

    /**
     * Asynchronously loads the current user's profile from Firestore.
     *
     * Fetches the profile by device ID, populates UI fields (name, email, phone),
     * loads profile photo if available, and configures the notification opt-out switch
     * with a change listener. Handles both Base64-encoded and URI-based photos.
     */
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
                        try {
                            String photoUri = profile.getProfilePhotoUri();
                            if (photoUri.startsWith("data:image")) {
                                // Base64 encoded image
                                String base64 = photoUri.substring(photoUri.indexOf(",") + 1);
                                byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(
                                        decoded, 0, decoded.length);
                                profileImage.setImageBitmap(bitmap);
                            } else {
                                // Regular URI - wrap in try/catch in case permission expired
                                profileImage.setImageURI(android.net.Uri.parse(photoUri));
                            }
                        } catch (Exception e) {
                            // URI permission expired, just show placeholder
                            profileImage.setImageResource(R.drawable.ic_profile_placeholder_forstyledlayout);
                        }
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

    /**
     * Validates and saves profile changes to Firestore.
     *
     * Trims input fields, validates that name and email are non-empty, then
     * asynchronously calls the ProfileController to update the profile. Updates
     * button state during save operation and displays success/failure feedback.
     */
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

    /**
     * Shows a confirmation dialog before permanent account deletion.
     *
     * Displays a request for user confirmation before deleting the account and
     * all owned events. On confirmation, calls ProfileController to perform the
     * deletion and finishes the activity upon success.
     */
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