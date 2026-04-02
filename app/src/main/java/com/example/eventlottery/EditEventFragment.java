package com.example.eventlottery;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.service.PathosNotifyService;
import com.example.eventlottery.service.PosterService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EditEventFragment
 *
 * Allows an organizer to:
 * - update or remove the poster image for an existing event
 * - enable or disable the geolocation requirement for that event
 * - search for users by name or email and add them as co-organizers
 * - remove co-organizers from that event
 *
 * Co-organizers are still stored internally as device IDs in "coOrganizerIds".
 *
 * User stories supported:
 * - US 01.07.01: User is identified by device
 * - US 02.02.03: Organizer can enable or disable geolocation requirement
 * - US 02.04.02: Organizer can update an event poster
 * - US 02.09.01: Organizer can add/remove a co-organizer for an event
 *
 * @author Fawaz Mansoor, Kenneth Joseph
 * @version 2.0
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

    private final List<String> originalCoOrganizerIds = new ArrayList<>();
    private final LinkedHashMap<String, CoOrganizerCandidate> selectedCoOrganizers =
            new LinkedHashMap<>();

    private ImageView ivPosterPreview;
    private MaterialButton btnSelectPoster;
    private MaterialButton btnSavePoster;
    private MaterialButton btnRemovePoster;
    private MaterialButton btnSearchCoOrganizers;
    private Switch switchGeoRequired;
    private EditText etCoOrganizerSearch;
    private ChipGroup chipGroupCoOrganizers;
    private LinearLayout layoutSearchResults;
    private TextView tvSearchResultsLabel;

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

        etCoOrganizerSearch = view.findViewById(R.id.et_edit_coorganizer_search);
        btnSearchCoOrganizers = view.findViewById(R.id.btn_edit_search_coorganizers);
        chipGroupCoOrganizers = view.findViewById(R.id.chip_group_edit_coorganizers);
        layoutSearchResults = view.findViewById(R.id.layout_edit_search_results);
        tvSearchResultsLabel = view.findViewById(R.id.tv_edit_search_results_label);

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

        btnSearchCoOrganizers.setOnClickListener(v ->
                requireCompletedProfile(this::searchProfiles)
        );

        loadCoOrganizers();
        updateSaveButtonState();
        configureUiAccess();

        return view;
    }

    /**
     * Loads existing co-organizer IDs from the event, then resolves them to user profiles
     * so the chips can show name/email instead of raw device IDs.
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

                    List<String> loadedIds = extractCoOrganizerIds(snapshot);
                    originalCoOrganizerIds.clear();
                    originalCoOrganizerIds.addAll(loadedIds);

                    selectedCoOrganizers.clear();

                    if (loadedIds.isEmpty()) {
                        renderSelectedCoOrganizerChips();
                        updateSaveButtonState();
                        return;
                    }

                    resolveCoOrganizerProfiles(loadedIds);
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

    private void resolveCoOrganizerProfiles(List<String> ids) {
        final int total = ids.size();
        final int[] resolved = {0};

        for (String id : ids) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(id)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (getActivity() == null) {
                            return;
                        }

                        selectedCoOrganizers.put(id, candidateFromUserDoc(doc, id));
                        resolved[0]++;
                        if (resolved[0] == total) {
                            renderSelectedCoOrganizerChips();
                            updateSaveButtonState();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getActivity() == null) {
                            return;
                        }

                        selectedCoOrganizers.put(id,
                                new CoOrganizerCandidate(id, "Unknown User", ""));
                        resolved[0]++;
                        if (resolved[0] == total) {
                            renderSelectedCoOrganizerChips();
                            updateSaveButtonState();
                        }
                    });
        }
    }

    /**
     * Searches all user profiles, then filters client-side by name or email.
     * This is simple and fine for a school project-sized dataset.
     */
    private void searchProfiles() {
        String query = safe(etCoOrganizerSearch);

        if (TextUtils.isEmpty(query)) {
            etCoOrganizerSearch.setError("Enter a name or email");
            return;
        }

        btnSearchCoOrganizers.setEnabled(false);
        btnSearchCoOrganizers.setText("SEARCHING...");
        clearSearchResults();

        String normalizedQuery = query.toLowerCase().trim();

        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (getActivity() == null) {
                        return;
                    }

                    List<CoOrganizerCandidate> matches = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        CoOrganizerCandidate candidate = candidateFromUserDoc(doc, doc.getId());

                        String searchableName = candidate.name.toLowerCase();
                        String searchableEmail = candidate.email.toLowerCase();

                        boolean matchesQuery =
                                searchableName.contains(normalizedQuery)
                                        || searchableEmail.contains(normalizedQuery);

                        boolean isCurrentUser = candidate.deviceId.equals(currentUserDeviceId);
                        boolean alreadyAdded = selectedCoOrganizers.containsKey(candidate.deviceId);

                        if (matchesQuery && !isCurrentUser && !alreadyAdded) {
                            matches.add(candidate);
                        }
                    }

                    renderSearchResults(matches);
                    btnSearchCoOrganizers.setEnabled(true);
                    btnSearchCoOrganizers.setText("SEARCH USERS");
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) {
                        return;
                    }

                    btnSearchCoOrganizers.setEnabled(true);
                    btnSearchCoOrganizers.setText("SEARCH USERS");
                    Toast.makeText(requireContext(),
                            "Failed to search users: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private CoOrganizerCandidate candidateFromUserDoc(DocumentSnapshot doc, String fallbackDeviceId) {
        String deviceId = doc.getId();
        if (deviceId == null || deviceId.trim().isEmpty()) {
            deviceId = fallbackDeviceId;
        }

        String name = safeString(doc.getString("name"));
        String email = safeString(doc.getString("email"));

        if (name.isEmpty()) {
            name = "Unnamed User";
        }

        return new CoOrganizerCandidate(deviceId, name, email);
    }

    private void renderSearchResults(List<CoOrganizerCandidate> matches) {
        clearSearchResults();

        tvSearchResultsLabel.setVisibility(View.VISIBLE);

        if (matches.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No matching users found.");
            empty.setTextColor(0xFFFFFFFF);
            empty.setTextSize(13f);
            layoutSearchResults.addView(empty);
            return;
        }

        for (CoOrganizerCandidate candidate : matches) {
            layoutSearchResults.addView(createSearchResultView(candidate));
        }
    }

    private View createSearchResultView(CoOrganizerCandidate candidate) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(24, 20, 24, 20);
        row.setBackgroundResource(R.drawable.bg_input_field);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = 16;
        row.setLayoutParams(rowParams);

        LinearLayout textContainer = new LinearLayout(requireContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        textContainer.setLayoutParams(textParams);

        TextView tvName = new TextView(requireContext());
        tvName.setText(candidate.name);
        tvName.setTextColor(0xFF1A1A2E);
        tvName.setTextSize(14f);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvEmail = new TextView(requireContext());
        tvEmail.setText(candidate.email.isEmpty() ? candidate.deviceId : candidate.email);
        tvEmail.setTextColor(0x881A1A2E);
        tvEmail.setTextSize(12f);

        textContainer.addView(tvName);
        textContainer.addView(tvEmail);

        MaterialButton addButton = new MaterialButton(requireContext());
        addButton.setText("ADD");
        addButton.setInsetTop(0);
        addButton.setInsetBottom(0);
        addButton.setMinHeight(0);
        addButton.setMinimumHeight(0);
        addButton.setCornerRadius(12);
        addButton.setTextSize(12f);

        addButton.setOnClickListener(v -> {
            selectedCoOrganizers.put(candidate.deviceId, candidate);
            etCoOrganizerSearch.setText("");
            clearSearchResults();
            renderSelectedCoOrganizerChips();
            updateSaveButtonState();
        });

        row.addView(textContainer);
        row.addView(addButton);

        return row;
    }

    private void clearSearchResults() {
        tvSearchResultsLabel.setVisibility(View.GONE);
        layoutSearchResults.removeAllViews();
    }

    /**
     * Shows selected co-organizers as chips using name/email, not raw device IDs.
     */
    private void renderSelectedCoOrganizerChips() {
        chipGroupCoOrganizers.removeAllViews();

        for (CoOrganizerCandidate candidate : selectedCoOrganizers.values()) {
            Chip chip = new Chip(requireContext());

            String label = candidate.name;
            if (!candidate.email.isEmpty()) {
                label += " (" + candidate.email + ")";
            }

            chip.setText(label);
            chip.setCloseIconVisible(true);
            chip.setClickable(false);

            chip.setOnCloseIconClickListener(v ->
                    showRemoveCoOrganizerDialog(candidate)
            );

            chipGroupCoOrganizers.addView(chip);
        }
    }

    private void showRemoveCoOrganizerDialog(CoOrganizerCandidate candidate) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove co-organizer")
                .setMessage("Remove " + candidate.name + " from this event?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    selectedCoOrganizers.remove(candidate.deviceId);
                    renderSelectedCoOrganizerChips();
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
            etCoOrganizerSearch.setEnabled(true);
            btnSearchCoOrganizers.setEnabled(true);
            setCoOrganizerChipRemovalEnabled(true);
            updateSaveButtonState();
        }, () -> {
            btnSelectPoster.setEnabled(false);
            btnSavePoster.setEnabled(false);
            btnRemovePoster.setEnabled(false);
            switchGeoRequired.setEnabled(false);
            etCoOrganizerSearch.setEnabled(false);
            btnSearchCoOrganizers.setEnabled(false);
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
        boolean coOrganizersChanged = !sameIds(getSelectedCoOrganizerIds(), originalCoOrganizerIds);

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

    // Helper function
    private void sendCoOrganizerNotifications(List<String> addedIds) {
        NotificationLogRepository repo =
                new com.example.eventlottery.firebase.FirestoreNotificationLogRepository();

        PathosNotifyService notifyService = new PathosNotifyService(repo);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (String deviceId : addedIds) {
            db.collection("profiles")
                    .document(deviceId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            return;
                        }

                        String recipientName = documentSnapshot.getString("name");
                        if (recipientName == null || recipientName.trim().isEmpty()) {
                            return;
                        }

                        notifyService.notifyCoOrganizerAdded(
                                recipientName,
                                eventId,
                                "You were added as a co-organizer for " + eventName
                        );
                    });
        }
    }
    /**
     * Updates non-poster event metadata in Firestore.
     */
    /**
     * Updates non-poster event metadata in Firestore.
     */
    private void updateEventMetadataOnly() {
        Map<String, Object> updates = new HashMap<>();
        boolean geoChanged = currentRequiresGeolocation != originalRequiresGeolocation;
        boolean coOrganizersChanged = !sameIds(getSelectedCoOrganizerIds(), originalCoOrganizerIds);

        List<String> newIds = getSelectedCoOrganizerIds();
        List<String> addedCoOrganizerIds = new ArrayList<>();

        for (String id : newIds) {
            if (!originalCoOrganizerIds.contains(id)) {
                addedCoOrganizerIds.add(id);
            }
        }

        if (geoChanged) {
            updates.put("requiresGeolocation", currentRequiresGeolocation);
        }

        if (coOrganizersChanged) {
            updates.put("coOrganizerIds", newIds);
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

                    if (!addedCoOrganizerIds.isEmpty()) {
                        sendCoOrganizerNotifications(addedCoOrganizerIds);
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
        originalCoOrganizerIds.addAll(getSelectedCoOrganizerIds());

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
                        || !sameIds(getSelectedCoOrganizerIds(), originalCoOrganizerIds);

        btnSavePoster.setEnabled(hasPendingChanges);
        btnSavePoster.setText("SAVE CHANGES");
    }

    private List<String> getSelectedCoOrganizerIds() {
        return new ArrayList<>(selectedCoOrganizers.keySet());
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

        new FirestoreProfileRepository().getProfile(deviceId,
                new ProfileRepository.ProfileCallback() {
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

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Lightweight UI model for co-organizer selection.
     */
    private static class CoOrganizerCandidate {
        private final String deviceId;
        private final String name;
        private final String email;

        CoOrganizerCandidate(String deviceId, String name, String email) {
            this.deviceId = deviceId;
            this.name = name;
            this.email = email;
        }
    }
}