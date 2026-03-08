package com.example.eventlottery;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;
import com.example.eventlottery.controller.ProfileController;
import com.example.eventlottery.data.memory.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;

/**
 * Fragment for editing the user profile.
 * Allows the user to update their name, email, and phone number.
 *
 * @author Hasrat Singh Chauhan
 * @version 1.0
 * @see ProfileController
 * @see DeviceIdentityService
 */
public class EditProfileFragment extends Fragment {

    private ProfileController profileController;
    private String deviceId;
    private EditText editName, editEmail, editPhone;
    private Button saveButton, cancelButton;

    /**
     * Initializes the profile controller and retrieves the device ID.
     *
     * @param savedInstanceState the saved instance state bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileController = new ProfileController(new FirestoreProfileRepository());
        deviceId = DeviceIdentityService.getDeviceId(requireContext());
    }

    /**
     * Inflates the edit profile layout, sets up input fields with current data,
     * and handles save/cancel button clicks.
     *
     * @param inflater the layout inflater
     * @param container the parent view group
     * @param savedInstanceState the saved instance state bundle
     * @return the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Initialize views
        editName = view.findViewById(R.id.edit_name);
        editEmail = view.findViewById(R.id.edit_email);
        editPhone = view.findViewById(R.id.edit_phone);
        saveButton = view.findViewById(R.id.button_save);
        cancelButton = view.findViewById(R.id.button_cancel);

        // Load current profile data
        loadCurrentProfile();

        // Set up save button
        saveButton.setOnClickListener(v -> saveProfile());

        // Set up cancel button
        cancelButton.setOnClickListener(v -> {
            // Go back to profile fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    /**
     * Loads the current user profile and populates the input fields.
     */
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

    /**
     * Saves the updated profile information.
     * Validates inputs and shows success/error messages.
     */
    private void saveProfile() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            editName.setError("Name is required");
            return;
        }
        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            return;
        }
        if (phone.isEmpty()) {
            editPhone.setError("Phone number is required");
            return;
        }

        // Show loading state
        saveButton.setEnabled(false);
        saveButton.setText("SAVING...");

        // Call update method (you'll add this to ProfileController)
        profileController.updateProfile(deviceId, name, email, phone, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                saveButton.setEnabled(true);
                saveButton.setText("SAVE");
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                // Go back to profile fragment
                requireActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(Exception e) {
                saveButton.setEnabled(true);
                saveButton.setText("SAVE");
                Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}