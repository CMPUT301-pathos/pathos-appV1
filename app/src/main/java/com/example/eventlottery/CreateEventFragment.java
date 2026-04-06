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
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.example.eventlottery.data.NotificationLogRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreNotificationLogRepository;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.service.PathosNotifyService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CreateEventFragment
 *
 * Allows an organizer to create a new event, optionally marked as private,
 * optionally requiring geolocation when entrants join the waiting list,
 * and optionally assigning co-organizers to the event by searching users
 * through name or email.
 *
 * Private events are not visible in the public event listing and do not
 * generate a promotional QR code. Entrants must be invited manually.
 *
 * Responsibilities:
 * - collect event fields including category, capacity, and event date
 * - choose registration dates with a Material date range picker
 * - optionally upload an event poster (Base64 encoded)
 * - toggle private event mode
 * - toggle geolocation requirement for waiting-list joins
 * - search for co-organizers by name or email
 * - save co-organizer device IDs to the Firestore "events" collection
 * - save the event to Firestore
 * - navigate to QrCodeFragment on successful publish (public events only)
 * - navigate to PrivateEventInviteFragment for private events
 *
 * User stories supported:
 * - US 02.01.01: As an organizer I want to create a new public event and
 *   generate a unique promotional QR code that links to the event description
 *   and event poster in the app.
 * - US 02.01.02: As an organizer, I want to create a private event that is
 *   not visible on the event listing and does not generate a promotional QR code.
 * - US 02.01.03: As an organizer, I want to invite specific entrants to a
 *   private event’s waiting list by searching via name, phone number and/or email.
 * - US 02.01.04: As an organizer, I want to set a registration period.
 * - US 02.02.03: As an organizer I want to enable or disable the geolocation
 *   requirement for my event.
 * - US 02.04.01: As an organizer I want to upload an event poster to the
 *   event details page to provide visual information to entrants.
 * - US 02.09.01: As an organizer I want to add co-organizers to my event.
 *
 * @author Kenneth Joseph, Fawaz Mansoor
 * @version 3.0
 */
public class CreateEventFragment extends Fragment {

    private EditText etName, etDesc, etLocation, etStart, etEnd, etCapacity;
    private EditText etEventDate;
    private EditText etCoOrganizerSearch;
    private ImageView ivPosterPreview;
    private MaterialButton btnSelectPoster, btnPublish, btnSearchCoOrganizers;
    private TextView tvSelectedCategory;
    private TextView tvSearchResultsLabel;
    private Switch switchPrivate, switchGeoRequired;
    private ChipGroup chipGroupCoOrganizers;
    private LinearLayout layoutSearchResults;

    private final LinkedHashMap<String, CoOrganizerCandidate> selectedCoOrganizers =
            new LinkedHashMap<>();

    private Uri selectedPosterUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickPosterLauncher;
    private boolean isPublishing = false;
    private boolean isPrivateEvent = false;
    private boolean requiresGeolocation = false;
    private String selectedCategory = "All";

    private long registrationStartMillis = 0;
    private long registrationEndMillis = 0;
    private long eventDateMillis = 0;

    private static final String[] CATEGORIES = {
            "All", "Sports", "Music", "Arts", "Education", "Community"
    };

    private static final CountingIdlingResource PUBLISH_IDLING =
            new CountingIdlingResource("CreateEventPublish");

    public CreateEventFragment() {
    }

    /**
     * Returns the Espresso idling resource used during publish operations.
     *
     * This is used for UI tests to wait until background event publishing
     * has completed.
     */
    public static IdlingResource getPublishIdlingResource() {
        return PUBLISH_IDLING;
    }

    /**
     * Signals the Espresso idling resource that a publish operation has started.
     */
    private static void beginPublishAsync() {
        PUBLISH_IDLING.increment();
    }

    /**
     * Signals the Espresso idling resource that a publish operation has finished.
     */
    private static void endPublishAsync() {
        if (!PUBLISH_IDLING.isIdleNow()) {
            PUBLISH_IDLING.decrement();
        }
    }

    /**
     * Inflates the create event layout and wires UI controls to fragment state.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_event, container, false);

        etName = root.findViewById(R.id.et_event_name);
        etDesc = root.findViewById(R.id.et_event_description);
        etLocation = root.findViewById(R.id.et_event_location);
        etStart = root.findViewById(R.id.et_event_start);
        etEnd = root.findViewById(R.id.et_event_end);
        etCapacity = root.findViewById(R.id.et_event_capacity);
        etEventDate = root.findViewById(R.id.et_event_date);

        ivPosterPreview = root.findViewById(R.id.iv_event_poster_preview);
        btnSelectPoster = root.findViewById(R.id.btn_select_event_poster);
        btnPublish = root.findViewById(R.id.btn_publish_event);

        tvSelectedCategory = root.findViewById(R.id.tv_selected_category);
        switchPrivate = root.findViewById(R.id.switch_private_event);
        switchGeoRequired = root.findViewById(R.id.switch_geo_required);

        etCoOrganizerSearch = root.findViewById(R.id.et_coorganizer_search);
        btnSearchCoOrganizers = root.findViewById(R.id.btn_search_coorganizers);
        chipGroupCoOrganizers = root.findViewById(R.id.chip_group_coorganizers);
        layoutSearchResults = root.findViewById(R.id.layout_search_results);
        tvSearchResultsLabel = root.findViewById(R.id.tv_search_results_label);

        MaterialButton btnPickCategory = root.findViewById(R.id.btn_pick_category);

        switchPrivate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isPrivateEvent = isChecked;
            btnPublish.setText(getPublishButtonText());
        });

        switchGeoRequired.setOnCheckedChangeListener((buttonView, isChecked) ->
                requiresGeolocation = isChecked
        );

        btnPickCategory.setOnClickListener(v ->
                requireCompletedProfile(() ->
                        new android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Select Category")
                                .setItems(CATEGORIES, (d, which) -> {
                                    selectedCategory = CATEGORIES[which];
                                    tvSelectedCategory.setText("Category: " + selectedCategory);
                                })
                                .show()
                )
        );

        btnSearchCoOrganizers.setOnClickListener(v ->
                requireCompletedProfile(this::searchProfiles));

        setupImagePicker();
        setupDateRangePicker();
        setupEventDatePicker();

        btnSelectPoster.setOnClickListener(v ->
                requireCompletedProfile(this::openPosterPicker));

        btnPublish.setOnClickListener(v ->
                requireCompletedProfile(this::publishEvent));

        btnPublish.setText(getPublishButtonText());
        refreshCoOrganizerDisplay();
        configureUiAccess();

        return root;
    }
    /**
     * Searches user profiles by name or email so the organizer can add
     * co-organizers to the event.
     *
     * The search is performed client-side against the users collection and
     * excludes the current organizer and already-selected co-organizers.
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

        String currentUserDeviceId = DeviceIdentityService.getDeviceId(requireContext());
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
    /**
     * Builds a co-organizer candidate model from a user document.
     *
     * If the Firestore document id is missing or blank, the provided fallback
     * device id is used instead. Missing names are replaced with a default
     * placeholder label.
     *
     * @param doc user document snapshot
     * @param fallbackDeviceId fallback device id if the document id is unavailable
     * @return lightweight co-organizer candidate for UI display
     */
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
    /**
     * Renders co-organizer search results in the UI.
     *
     * Matching users are displayed as selectable rows. If no matches are found,
     * an empty-state message is shown instead.
     *
     * @param matches list of matching co-organizer candidates
     */
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
    /**
     * Creates a single search-result row for a co-organizer candidate.
     *
     * The row displays the candidate's name and email, along with an add
     * button that inserts the candidate into the selected co-organizers list.
     *
     * @param candidate candidate to render
     * @return configured row view for the search results container
     */
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
            refreshCoOrganizerDisplay();
        });

        row.addView(textContainer);
        row.addView(addButton);

        return row;
    }
    /**
     * Clears rendered co-organizer search results and hides the results label.
     */
    private void clearSearchResults() {
        tvSearchResultsLabel.setVisibility(View.GONE);
        layoutSearchResults.removeAllViews();
    }
    /**
     * Refreshes the selected co-organizer chips shown in the UI.
     *
     * Each chip displays the co-organizer's name and optional email and
     * supports removal from the selected set.
     */
    private void refreshCoOrganizerDisplay() {
        if (chipGroupCoOrganizers == null) {
            return;
        }

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

            chip.setOnCloseIconClickListener(v -> {
                selectedCoOrganizers.remove(candidate.deviceId);
                refreshCoOrganizerDisplay();
            });

            chipGroupCoOrganizers.addView(chip);
        }
    }
    /**
     * Registers the image picker used to select an optional event poster.
     */
    private void setupImagePicker() {
        pickPosterLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedPosterUri = uri;
                        ivPosterPreview.setImageURI(uri);
                    }
                }
        );
    }
    /**
     * Launches the image picker for selecting an event poster.
     */
    private void openPosterPicker() {
        pickPosterLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }
    /**
     * Wires the registration start and end fields to open the shared
     * registration date range picker.
     */
    private void setupDateRangePicker() {
        View.OnClickListener openPicker = v -> showDateRangePicker();
        etStart.setOnClickListener(openPicker);
        etEnd.setOnClickListener(openPicker);
    }
    /**
     * Shows a Material date range picker for selecting the event's
     * registration period.
     */
    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select registration dates")
                        .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) {
                return;
            }

            registrationStartMillis = selection.first;
            registrationEndMillis = selection.second;
            etStart.setText(formatDate(selection.first));
            etEnd.setText(formatDate(selection.second));
        });

        picker.show(getChildFragmentManager(), "registration_date_range_picker");
    }
    /**
     * Wires the event date field to open a single-date picker.
     */
    private void setupEventDatePicker() {
        etEventDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select event date")
                            .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection == null) {
                    return;
                }
                eventDateMillis = selection;
                etEventDate.setText(formatDate(selection));
            });

            picker.show(getChildFragmentManager(), "event_date_picker");
        });
    }
    /**
     * Validates event input and begins the event publishing flow.
     *
     * If a poster is selected, the poster is processed first before the event
     * document is created. Otherwise, the event document is created directly.
     */
    private void publishEvent() {
        if (isPublishing) {
            return;
        }
        isPublishing = true;

        String name = safe(etName);
        String desc = safe(etDesc);
        String location = safe(etLocation);
        String capStr = safe(etCapacity);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            isPublishing = false;
            return;
        }

        if (registrationStartMillis == 0) {
            etStart.setError("Required");
            isPublishing = false;
            return;
        }

        if (registrationEndMillis == 0) {
            etEnd.setError("Required");
            isPublishing = false;
            return;
        }

        Integer capacity = null;
        if (!TextUtils.isEmpty(capStr)) {
            try {
                capacity = Integer.parseInt(capStr);
                if (capacity < 1) {
                    etCapacity.setError("Must be >= 1");
                    isPublishing = false;
                    return;
                }
            } catch (NumberFormatException e) {
                etCapacity.setError("Enter a number");
                isPublishing = false;
                return;
            }
        }

        beginPublishAsync();
        setPublishingState(true,
                selectedPosterUri == null ? "PUBLISHING..." : "UPLOADING POSTER...");

        if (selectedPosterUri != null) {
            uploadPosterAndCreateEvent(name, desc, location, capacity);
        } else {
            createEventDocument(name, desc, location, capacity, null);
        }
    }
    /**
     * Compresses and encodes the selected poster image, then continues the
     * event creation flow with the generated poster data.
     *
     * @param name event name
     * @param desc event description
     * @param location event location
     * @param capacity event capacity, or null if unspecified
     */
    private void uploadPosterAndCreateEvent(String name, String desc,
                                            String location, Integer capacity) {
        try {
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media
                    .getBitmap(requireContext().getContentResolver(), selectedPosterUri);

            int maxWidth = 600;
            if (bitmap.getWidth() > maxWidth) {
                float scale = (float) maxWidth / bitmap.getWidth();
                int newHeight = Math.round(bitmap.getHeight() * scale);
                bitmap = android.graphics.Bitmap.createScaledBitmap(
                        bitmap, maxWidth, newHeight, true);
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
            String base64Image = android.util.Base64.encodeToString(
                    baos.toByteArray(), android.util.Base64.DEFAULT);
            String posterData = "data:image/jpeg;base64," + base64Image;

            createEventDocument(name, desc, location, capacity, posterData);

        } catch (Exception e) {
            if (getActivity() == null) {
                endPublishAsync();
                return;
            }

            isPublishing = false;
            setPublishingState(false, getPublishButtonText());
            Toast.makeText(requireContext(),
                    "Poster processing failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            endPublishAsync();
        }
    }
    /**
     * Creates the Firestore event document and optionally sends
     * co-organizer notifications after creation succeeds.
     *
     * Public events also receive a QR payload after the document is created.
     *
     * @param name event name
     * @param desc event description
     * @param location event location
     * @param capacity event capacity, or null if unspecified
     * @param posterUrl encoded poster data, or null if no poster was selected
     */
    private void createEventDocument(String name, String desc, String location,
                                     Integer capacity, @Nullable String posterUrl) {
        String organizerDeviceId = DeviceIdentityService.getDeviceId(requireContext());
        List<String> coOrganizerIds = sanitizeCoOrganizerIds(organizerDeviceId);

        Map<String, Object> eventDoc = new HashMap<>();
        eventDoc.put("name", name);
        eventDoc.put("description", desc);
        eventDoc.put("location", location);
        eventDoc.put("registrationStart", registrationStartMillis);
        eventDoc.put("registrationEnd", registrationEndMillis);
        eventDoc.put("eventDate", eventDateMillis);
        eventDoc.put("capacity", capacity);
        eventDoc.put("category", selectedCategory);
        eventDoc.put("organizerDeviceId", organizerDeviceId);
        eventDoc.put("coOrganizerIds", coOrganizerIds);
        eventDoc.put("createdAt", System.currentTimeMillis());
        eventDoc.put("posterUrl", posterUrl);
        eventDoc.put("isPrivate", isPrivateEvent);
        eventDoc.put("requiresGeolocation", requiresGeolocation);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .add(eventDoc)
                .addOnSuccessListener(ref -> {
                    if (getActivity() == null) {
                        endPublishAsync();
                        return;
                    }

                    String eventId = ref.getId();
                    String qrPayload = "eventId:" + eventId;

                    if (!isPrivateEvent) {
                        ref.update("qrPayload", qrPayload);
                    }

                    if (!coOrganizerIds.isEmpty()) {
                        NotificationLogRepository repo = new FirestoreNotificationLogRepository();
                        PathosNotifyService notifyService = new PathosNotifyService(repo);

                        for (String deviceId : coOrganizerIds) {
                            db.collection("users")
                                    .document(deviceId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (!userDoc.exists()) {
                                            android.util.Log.d("COORG_NOTIFY", "User doc not found for deviceId=" + deviceId);
                                            return;
                                        }

                                        String recipientName = safeString(userDoc.getString("name"));
                                        android.util.Log.d("COORG_NOTIFY",
                                                "deviceId=" + deviceId + ", recipientName=" + recipientName);

                                        notifyService.notifyCoOrganizerAdded(
                                                        deviceId,
                                                        eventId,
                                                        "You were added as a co-organizer for " + name
                                                )
                                                .addOnSuccessListener(unused ->
                                                        android.util.Log.d("COORG_NOTIFY",
                                                                "Notification saved for recipient=" + recipientName))
                                                .addOnFailureListener(e ->
                                                        android.util.Log.e("COORG_NOTIFY",
                                                                "Failed to save notification", e));
                                    })
                                    .addOnFailureListener(e ->
                                            android.util.Log.e("COORG_NOTIFY",
                                                    "Failed to fetch user for deviceId=" + deviceId, e));
                        }
                    }

                    Toast.makeText(requireContext(), "Event created!", Toast.LENGTH_SHORT).show();
                    isPublishing = false;
                    setPublishingState(false, getPublishButtonText());

                    if (isPrivateEvent) {
                        goToInvite(eventId, name);
                    } else {
                        goToQr(qrPayload);
                    }
                    endPublishAsync();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) {
                        endPublishAsync();
                        return;
                    }

                    isPublishing = false;
                    setPublishingState(false, getPublishButtonText());
                    Toast.makeText(requireContext(),
                            "Create failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    endPublishAsync();
                });
    }
    /**
     * Cleans the selected co-organizer device IDs before storing them.
     *
     * Blank values, duplicates, and the organizer's own device id are removed.
     *
     * @param organizerDeviceId current organizer's device id
     * @return cleaned list of co-organizer device IDs
     */
    private List<String> sanitizeCoOrganizerIds(@NonNull String organizerDeviceId) {
        List<String> cleaned = new ArrayList<>();
        for (String id : selectedCoOrganizers.keySet()) {
            if (id != null) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()
                        && !trimmed.equals(organizerDeviceId)
                        && !cleaned.contains(trimmed)) {
                    cleaned.add(trimmed);
                }
            }
        }
        return cleaned;
    }
    /**
     * Navigates to the QR code screen for a newly created public event.
     *
     * @param payload QR payload associated with the event
     */
    private void goToQr(@NonNull String payload) {
        QrCodeFragment qr = QrCodeFragment.newInstance(payload);
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, qr)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
    /**
     * Navigates to the private-event invite screen after a private event
     * has been created.
     *
     * @param eventId Firestore id of the created event
     * @param eventName display name of the created event
     */
    private void goToInvite(String eventId, String eventName) {
        PrivateEventInviteFragment fragment =
                PrivateEventInviteFragment.newInstance(eventId, eventName);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
    /**
     * Updates control enabled states and button text while an event is being
     * published or poster data is being uploaded.
     *
     * @param publishing true while publishing is in progress
     * @param buttonText text to show on the publish button
     */
    private void setPublishingState(boolean publishing, String buttonText) {
        btnPublish.setEnabled(!publishing);
        btnSelectPoster.setEnabled(!publishing);
        etStart.setEnabled(!publishing);
        etEnd.setEnabled(!publishing);
        switchPrivate.setEnabled(!publishing);
        switchGeoRequired.setEnabled(!publishing);
        btnSearchCoOrganizers.setEnabled(!publishing);
        etCoOrganizerSearch.setEnabled(!publishing);

        btnPublish.setText(buttonText);
    }
    /**
     * Enables or disables event-creation controls based on whether the current
     * user has completed their profile.
     */
    private void configureUiAccess() {
        requireCompletedProfile(
                () -> {
                    btnSelectPoster.setEnabled(true);
                    btnPublish.setEnabled(true);
                    switchPrivate.setEnabled(true);
                    switchGeoRequired.setEnabled(true);
                    btnSearchCoOrganizers.setEnabled(true);
                    etCoOrganizerSearch.setEnabled(true);
                },
                () -> {
                    btnSelectPoster.setEnabled(false);
                    btnPublish.setEnabled(false);
                    etStart.setEnabled(false);
                    etEnd.setEnabled(false);
                    switchPrivate.setEnabled(false);
                    switchGeoRequired.setEnabled(false);
                    btnSearchCoOrganizers.setEnabled(false);
                    etCoOrganizerSearch.setEnabled(false);

                    Toast.makeText(requireContext(),
                            "Complete your profile first to create events.",
                            Toast.LENGTH_SHORT).show();
                }
        );
    }
    /**
     * Runs the provided action only if the current user has a completed profile.
     * Otherwise, a default blocked message is shown.
     *
     * @param onAllowed action to run when the profile is complete
     */
    private void requireCompletedProfile(Runnable onAllowed) {
        requireCompletedProfile(onAllowed, () -> Toast.makeText(
                requireContext(),
                "Complete your profile first to create events.",
                Toast.LENGTH_SHORT
        ).show());
    }
    /**
     * Checks whether the current user has completed their profile before
     * allowing access to event-creation actions.
     *
     * @param onAllowed action to run when the profile is complete
     * @param onBlocked action to run when the profile is incomplete or lookup fails
     */
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
    /**
     * Returns the correct publish button label based on whether the event
     * is public or private.
     *
     * @return button text for the current event mode
     */
    private String getPublishButtonText() {
        return isPrivateEvent ? "PUBLISH PRIVATE EVENT" : "GENERATE QR CODE & POST";
    }
    /**
     * Formats a UTC millisecond timestamp into a user-friendly date string.
     *
     * @param utcMillis timestamp in UTC milliseconds
     * @return formatted date string
     */
    private String formatDate(long utcMillis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(utcMillis));
    }
    /**
     * Safely reads trimmed text from an EditText.
     *
     * @param et input field to read
     * @return trimmed text, or an empty string if the field has no text
     */
    private String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    /**
     * Safely trims a nullable string value.
     *
     * @param value source string
     * @return trimmed string, or an empty string if the value is null
     */
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