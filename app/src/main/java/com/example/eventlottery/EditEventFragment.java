package com.example.eventlottery;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.service.PosterService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * EditEventFragment
 *
 * Allows an organizer to update or remove the poster image for an existing event,
 * and enable or disable the geolocation requirement for that event.
 *
 * Responsibilities:
 * - display the current event poster
 * - allow organizer to select a new poster from device gallery
 * - upload a new poster to Firebase Storage and update Firestore
 * - allow organizer to remove the existing poster
 * - allow organizer to enable or disable geolocation requirement
 * - block editing actions unless the user's profile is completed
 *
 * Profile-completion protection:
 * - users with incomplete profiles may not edit event content
 * - editing controls are disabled until the required profile information
 *   has been completed
 *
 * User stories supported:
 * - US 01.02.01: Entrant provides personal information
 * - US 01.02.02: Entrant updates personal information
 * - US 01.07.01: User is identified by device
 * - US 02.02.03: Organizer can enable or disable geolocation requirement
 * - US 02.04.02: Organizer can update an event poster
 *
 * @author Fawaz Mansoor, Kenneth Joseph
 * @version 1.2
 * @see PosterService
 */
public class EditEventFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";
    private static final String ARG_POSTER_URL = "posterUrl";
    private static final String ARG_REQUIRES_GEOLOCATION = "requiresGeolocation";

    private String eventId;
    private String eventName;
    private String currentPosterUrl;
    private boolean currentRequiresGeolocation;
    private boolean originalRequiresGeolocation;

    private ImageView ivPosterPreview;
    private MaterialButton btnSelectPoster;
    private MaterialButton btnSavePoster;
    private MaterialButton btnRemovePoster;
    private Switch switchGeoRequired;

    private Uri selectedPosterUri;
    private PosterService posterService;
    private ActivityResultLauncher<PickVisualMediaRequest> pickPosterLauncher;

    public EditEventFragment() {
    }

    /**
     * Creates a new EditEventFragment for the selected event.
     *
     * @param event event summary containing event ID, name, poster URL,
     *              and geolocation requirement
     * @return configured EditEventFragment instance
     */
    public static EditEventFragment newInstance(EventSummary event) {
        EditEventFragment fragment = new EditEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, event.getId());
        args.putString(ARG_EVENT_NAME, event.getName());
        args.putString(ARG_POSTER_URL, event.getPosterUrl());
        args.putBoolean(ARG_REQUIRES_GEOLOCATION, event.isRequiresGeolocation());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
            currentPosterUrl = getArguments().getString(ARG_POSTER_URL);
            currentRequiresGeolocation =
                    getArguments().getBoolean(ARG_REQUIRES_GEOLOCATION, false);
            originalRequiresGeolocation = currentRequiresGeolocation;
        }

        posterService = new PosterService();

        pickPosterLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedPosterUri = uri;
                        ivPosterPreview.setImageURI(uri);
                        updateSaveButtonState();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);

        ivPosterPreview = view.findViewById(R.id.iv_edit_poster_preview);
        btnSelectPoster = view.findViewById(R.id.btn_edit_select_poster);
        btnSavePoster = view.findViewById(R.id.btn_edit_save_poster);
        btnRemovePoster = view.findViewById(R.id.btn_edit_remove_poster);
        switchGeoRequired = view.findViewById(R.id.switch_edit_geo_required);

        if (currentPosterUrl != null && !currentPosterUrl.isEmpty()) {
            Glide.with(this).load(currentPosterUrl).into(ivPosterPreview);
            btnRemovePoster.setVisibility(View.VISIBLE);
        } else {
            btnRemovePoster.setVisibility(View.GONE);
        }

        switchGeoRequired.setChecked(currentRequiresGeolocation);
        switchGeoRequired.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentRequiresGeolocation = isChecked;
            updateSaveButtonState();
        });

        updateSaveButtonState();

        btnSelectPoster.setOnClickListener(v ->
                requireCompletedProfile(() ->
                        pickPosterLauncher.launch(
                                new PickVisualMediaRequest.Builder()
                                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                        .build()
                        )
                )
        );

        btnSavePoster.setOnClickListener(v ->
                requireCompletedProfile(this::saveChanges)
        );

        btnRemovePoster.setOnClickListener(v ->
                requireCompletedProfile(this::removePoster)
        );

        configureUiAccess();

        return view;
    }

    /**
     * Enables or disables editing controls based on whether the current user's
     * profile is completed.
     */
    private void configureUiAccess() {
        requireCompletedProfile(new Runnable() {
            @Override
            public void run() {
                btnSelectPoster.setEnabled(true);
                btnRemovePoster.setEnabled(true);
                switchGeoRequired.setEnabled(true);
                updateSaveButtonState();
            }
        }, new Runnable() {
            @Override
            public void run() {
                btnSelectPoster.setEnabled(false);
                btnSavePoster.setEnabled(false);
                btnRemovePoster.setEnabled(false);
                switchGeoRequired.setEnabled(false);
                Toast.makeText(requireContext(),
                        "Complete your profile first to edit event content.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves poster changes and/or geolocation requirement changes.
     */
    private void saveChanges() {
        boolean posterChanged = selectedPosterUri != null;
        boolean geoChanged = currentRequiresGeolocation != originalRequiresGeolocation;

        if (!posterChanged && !geoChanged) {
            return;
        }

        btnSavePoster.setEnabled(false);
        btnSavePoster.setText(posterChanged ? "UPLOADING..." : "SAVING...");

        if (posterChanged) {
            savePosterThenMaybeGeoUpdate(geoChanged);
        } else {
            updateGeolocationRequirementOnly();
        }
    }

    /**
     * Uploads the selected poster image and optionally updates geolocation requirement after.
     */
    private void savePosterThenMaybeGeoUpdate(boolean geoChanged) {
        String deviceId = DeviceIdentityService.getDeviceId(requireContext());

        posterService.updatePoster(eventId, deviceId, selectedPosterUri, requireContext(),
                new PosterService.PosterCallback() {
                    @Override
                    public void onSuccess(String posterUrl) {
                        if (getActivity() == null) {
                            return;
                        }

                        currentPosterUrl = posterUrl;
                        selectedPosterUri = null;
                        btnRemovePoster.setVisibility(View.VISIBLE);
                        btnRemovePoster.setEnabled(true);

                        if (geoChanged) {
                            updateGeolocationRequirementOnly();
                        } else {
                            Toast.makeText(getContext(),
                                    "Event updated!",
                                    Toast.LENGTH_SHORT).show();
                            btnSavePoster.setText("SAVE CHANGES");
                            updateSaveButtonState();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (getActivity() == null) {
                            return;
                        }

                        Toast.makeText(getContext(),
                                "Failed to update poster: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnSavePoster.setText("SAVE CHANGES");
                        updateSaveButtonState();
                    }
                });
    }

    /**
     * Updates only the geolocation requirement in Firestore.
     */
    private void updateGeolocationRequirementOnly() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .update("requiresGeolocation", currentRequiresGeolocation)
                .addOnSuccessListener(unused -> {
                    if (getActivity() == null) {
                        return;
                    }

                    originalRequiresGeolocation = currentRequiresGeolocation;
                    Toast.makeText(getContext(),
                            "Event updated!",
                            Toast.LENGTH_SHORT).show();
                    btnSavePoster.setText("SAVE CHANGES");
                    updateSaveButtonState();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) {
                        return;
                    }

                    Toast.makeText(getContext(),
                            "Failed to update event: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnSavePoster.setText("SAVE CHANGES");
                    updateSaveButtonState();
                });
    }

    /**
     * Removes the current event poster from storage and updates the UI.
     */
    private void removePoster() {
        btnRemovePoster.setEnabled(false);

        posterService.deletePoster(eventId, new PosterService.PosterCallback() {
            @Override
            public void onSuccess(String posterUrl) {
                if (getActivity() == null) {
                    return;
                }

                Toast.makeText(getContext(), "Poster removed.", Toast.LENGTH_SHORT).show();
                ivPosterPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                btnRemovePoster.setVisibility(View.GONE);
                currentPosterUrl = null;
                selectedPosterUri = null;
                updateSaveButtonState();
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) {
                    return;
                }

                Toast.makeText(getContext(),
                        "Failed to remove poster: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                btnRemovePoster.setEnabled(true);
            }
        });
    }

    /**
     * Updates save-button state based on pending poster or geolocation changes.
     */
    private void updateSaveButtonState() {
        boolean hasPendingChanges =
                selectedPosterUri != null ||
                        currentRequiresGeolocation != originalRequiresGeolocation;

        btnSavePoster.setEnabled(hasPendingChanges);
        btnSavePoster.setText("SAVE CHANGES");
    }

    /**
     * Runs protected logic only if the current user's profile is completed.
     *
     * @param onAllowed logic to run when the profile is complete
     */
    private void requireCompletedProfile(Runnable onAllowed) {
        requireCompletedProfile(onAllowed, () -> Toast.makeText(
                requireContext(),
                "Complete your profile first to edit event content.",
                Toast.LENGTH_SHORT
        ).show());
    }

    /**
     * Runs one callback if the current user's profile is completed and another
     * callback if it is not.
     *
     * @param onAllowed logic to run when the profile is complete
     * @param onBlocked logic to run when the profile is incomplete or unavailable
     */
    private void requireCompletedProfile(Runnable onAllowed, Runnable onBlocked) {
        String deviceId = DeviceIdentityService.getDeviceId(requireContext());

        new FirestoreProfileRepository().getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile != null && profile.isProfileCompleted()) {
                    onAllowed.run();
                } else {
                    onBlocked.run();
                }
            }

            @Override
            public void onFailure(Exception e) {
                onBlocked.run();
            }
        });
    }
}