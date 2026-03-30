package com.example.eventlottery;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.service.PosterService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EditEventFragment
 *
 * Allows an organizer to:
 * - update or remove the poster image for an existing event
 * - enable or disable the geolocation requirement for that event
 * - add or remove co-organizers for that event
 *
 * Co-organizers are stored as event-level device IDs in "coOrganizerIds".
 *
 * User stories supported:
 * - US 01.07.01: User is identified by device
 * - US 02.02.03: Organizer can enable or disable geolocation requirement
 * - US 02.04.02: Organizer can update an event poster
 * - US 02.09.01: Organizer can add/remove a co-organizer for an event
 *
 * @author Fawaz Mansoor, Kenneth Joseph
 * @version 1.3
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

    private String currentUserDeviceId;

    private final List<String> currentCoOrganizerIds = new ArrayList<>();
    private final List<String> originalCoOrganizerIds = new ArrayList<>();

    private ImageView ivPosterPreview;
    private MaterialButton btnSelectPoster;
    private MaterialButton btnSavePoster;
    private MaterialButton btnRemovePoster;
    private MaterialButton btnAddCoOrganizer;
    private Switch switchGeoRequired;
    private EditText etCoOrganizerDeviceId;
    private ChipGroup chipGroupCoOrganizers;

    private Uri selectedPosterUri;
    private PosterService posterService;
    private ActivityResultLauncher<PickVisualMediaRequest> pickPosterLauncher;

    public EditEventFragment() {
    }

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

        currentUserDeviceId = DeviceIdentityService.getDeviceId(requireContext());

        ivPosterPreview = view.findViewById(R.id.iv_edit_poster_preview);
        btnSelectPoster = view.findViewById(R.id.btn_edit_select_poster);
        btnSavePoster = view.findViewById(R.id.btn_edit_save_poster);
        btnRemovePoster = view.findViewById(R.id.btn_edit_remove_poster);
        switchGeoRequired = view.findViewById(R.id.switch_edit_geo_required);

        etCoOrganizerDeviceId = view.findViewById(R.id.et_edit_coorganizer_device_id);
        btnAddCoOrganizer = view.findViewById(R.id.btn_edit_add_coorganizer);
        chipGroupCoOrganizers = view.findViewById(R.id.chip_group_edit_coorganizers);

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

        btnAddCoOrganizer.setOnClickListener(v ->
                requireCompletedProfile(this::addCoOrganizerFromInput)
        );

        loadCoOrganizers();
        updateSaveButtonState();
        configureUiAccess();

        return view;
    }

    /**
     * Loads existing co-organizers directly from Firestore.
     */
    private void loadCoOrganizers() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (getActivity() == null) {
                        return;
                    }

                    currentCoOrganizerIds.clear();
                    originalCoOrganizerIds.clear();

                    List<String> loadedIds = extractCoOrganizerIds(snapshot);
                    currentCoOrganizerIds.addAll(loadedIds);
                    originalCoOrganizerIds.addAll(loadedIds);

                    renderCoOrganizerChips();
                    updateSaveButtonState();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) {
                        return;
                    }

                    Toast.makeText(requireContext(),
                            "Failed to load co-organizers: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private List<String> extractCoOrganizerIds(DocumentSnapshot snapshot) {
        List<String> result = new ArrayList<>();
        Object raw = snapshot.get("coOrganizerIds");

        if (raw instanceof List<?>) {
            for (Object item : (List<?>) raw) {
                if (item != null) {
                    String value = item.toString().trim();
                    if (!value.isEmpty() && !result.contains(value)) {
                        result.add(value);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Adds a co-organizer using the device ID entered in the EditText.
     */
    private void addCoOrganizerFromInput() {
        String enteredId = safe(etCoOrganizerDeviceId);

        if (TextUtils.isEmpty(enteredId)) {
            etCoOrganizerDeviceId.setError("Enter a device ID");
            return;
        }

        if (enteredId.equals(currentUserDeviceId)) {
            etCoOrganizerDeviceId.setError("Organizer is already attached to this event");
            return;
        }

        if (currentCoOrganizerIds.contains(enteredId)) {
            etCoOrganizerDeviceId.setError("That co-organizer is already added");
            return;
        }

        currentCoOrganizerIds.add(enteredId);
        etCoOrganizerDeviceId.setText("");
        renderCoOrganizerChips();
        updateSaveButtonState();
    }

    /**
     * Renders removable chips for each co-organizer.
     */
    private void renderCoOrganizerChips() {
        chipGroupCoOrganizers.removeAllViews();

        for (String coOrganizerId : currentCoOrganizerIds) {
            Chip chip = new Chip(requireContext());
            chip.setText(coOrganizerId);
            chip.setCloseIconVisible(true);
            chip.setClickable(false);

            chip.setOnCloseIconClickListener(v ->
                    showRemoveCoOrganizerDialog(coOrganizerId)
            );

            chipGroupCoOrganizers.addView(chip);
        }
    }

    private void showRemoveCoOrganizerDialog(String coOrganizerId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove co-organizer")
                .setMessage("Remove " + coOrganizerId + " from this event?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    currentCoOrganizerIds.remove(coOrganizerId);
                    renderCoOrganizerChips();
                    updateSaveButtonState();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void configureUiAccess() {
        requireCompletedProfile(() -> {
            btnSelectPoster.setEnabled(true);
            btnRemovePoster.setEnabled(true);
            switchGeoRequired.setEnabled(true);
            etCoOrganizerDeviceId.setEnabled(true);
            btnAddCoOrganizer.setEnabled(true);
            setCoOrganizerChipRemovalEnabled(true);
            updateSaveButtonState();
        }, () -> {
            btnSelectPoster.setEnabled(false);
            btnSavePoster.setEnabled(false);
            btnRemovePoster.setEnabled(false);
            switchGeoRequired.setEnabled(false);
            etCoOrganizerDeviceId.setEnabled(false);
            btnAddCoOrganizer.setEnabled(false);
            setCoOrganizerChipRemovalEnabled(false);

            Toast.makeText(requireContext(),
                    "Complete your profile first to edit event content.",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setCoOrganizerChipRemovalEnabled(boolean enabled) {
        for (int i = 0; i < chipGroupCoOrganizers.getChildCount(); i++) {
            View child = chipGroupCoOrganizers.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setCloseIconVisible(enabled);
                child.setEnabled(enabled);
            }
        }
    }

    /**
     * Saves poster changes, geolocation changes, and/or co-organizer changes.
     */
    private void saveChanges() {
        boolean posterChanged = selectedPosterUri != null;
        boolean geoChanged = currentRequiresGeolocation != originalRequiresGeolocation;
        boolean coOrganizersChanged = !sameIds(currentCoOrganizerIds, originalCoOrganizerIds);

        if (!posterChanged && !geoChanged && !coOrganizersChanged) {
            return;
        }

        btnSavePoster.setEnabled(false);
        btnSavePoster.setText(posterChanged ? "UPLOADING..." : "SAVING...");

        if (posterChanged) {
            savePosterThenUpdateMetadata();
        } else {
            updateEventMetadataOnly();
        }
    }

    /**
     * Uploads the poster first, then updates Firestore metadata.
     */
    private void savePosterThenUpdateMetadata() {
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

                        updateEventMetadataOnly();
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
     * Updates non-poster event metadata in Firestore.
     */
    private void updateEventMetadataOnly() {
        Map<String, Object> updates = new HashMap<>();
        boolean geoChanged = currentRequiresGeolocation != originalRequiresGeolocation;
        boolean coOrganizersChanged = !sameIds(currentCoOrganizerIds, originalCoOrganizerIds);

        if (geoChanged) {
            updates.put("requiresGeolocation", currentRequiresGeolocation);
        }

        if (coOrganizersChanged) {
            updates.put("coOrganizerIds", new ArrayList<>(currentCoOrganizerIds));
        }

        if (updates.isEmpty()) {
            onSaveSucceeded();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (getActivity() == null) {
                        return;
                    }
                    onSaveSucceeded();
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

    private void onSaveSucceeded() {
        originalRequiresGeolocation = currentRequiresGeolocation;
        originalCoOrganizerIds.clear();
        originalCoOrganizerIds.addAll(currentCoOrganizerIds);

        Toast.makeText(getContext(),
                "Event updated!",
                Toast.LENGTH_SHORT).show();

        btnSavePoster.setText("SAVE CHANGES");
        updateSaveButtonState();
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
     * Updates save-button state based on pending changes.
     */
    private void updateSaveButtonState() {
        boolean hasPendingChanges =
                selectedPosterUri != null
                        || currentRequiresGeolocation != originalRequiresGeolocation
                        || !sameIds(currentCoOrganizerIds, originalCoOrganizerIds);

        btnSavePoster.setEnabled(hasPendingChanges);
        btnSavePoster.setText("SAVE CHANGES");
    }

    private boolean sameIds(List<String> first, List<String> second) {
        if (first.size() != second.size()) {
            return false;
        }
        return first.containsAll(second) && second.containsAll(first);
    }

    private void requireCompletedProfile(Runnable onAllowed) {
        requireCompletedProfile(onAllowed, () -> Toast.makeText(
                requireContext(),
                "Complete your profile first to edit event content.",
                Toast.LENGTH_SHORT
        ).show());
    }

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

    private String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}