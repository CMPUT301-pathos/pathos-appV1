package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.controller.ProfileController;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;

/**
 * Fragment for displaying the user profile screen.
 * Allows the user to delete their account with a confirmation dialog.
 *
 * @author Dmitriy Limanets
 * @version 1.0
 * @see ProfileController
 * @see DeviceIdentityService
 */

public class ProfileFragment extends Fragment {

    private ProfileController profileController;
    private String deviceId;
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
     * Inflates the profile layout and sets up the delete account button.
     * When the delete button is clicked, a confirmation dialog is shown.
     * On confirmation, the user's profile is deleted from Firestore.
     *
     * @param inflater  the layout inflater
     * @param container the parent view group
     * @param savedInstanceState the saved instance state bundle
     * @return the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button deleteButton = view.findViewById(R.id.button_delete_account);

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        profileController.deleteProfile(deviceId, new ProfileRepository.ProfileCallback() {
                            @Override
                            public void onSuccess(UserProfile profile) {
                                Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                // TODO: Navigate to welcome screen
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(), "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }
}
