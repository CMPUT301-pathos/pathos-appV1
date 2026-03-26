package com.example.eventlottery;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * CreateEventFragment
 *
 * Allows an organizer to create a new event and generate a promotional QR code.
 *
 * Responsibilities:
 * - collect event fields including category
 * - choose registration dates with a Material date range picker
 * - optionally upload an event poster to Firebase Storage
 * - save the event to the Firestore "events" collection
 * - navigate to QrCodeFragment on successful publish
 *
 * Profile-completion protection:
 * - event creation is blocked unless the current user's required profile
 *   information has been completed
 * - publishing controls are disabled for incomplete profiles
 *
 * Reliability notes:
 * - prevents double publish
 * - guards against fragment/activity lifecycle issues
 * - uses commitAllowingStateLoss only when FragmentManager state is saved
 *
 * User stories supported:
 * - US 01.02.01: Entrant provides personal information
 * - US 01.02.02: Entrant updates personal information
 * - US 01.07.01: User is identified by device
 * - US 02.01.01: Organizer can create a new event and generate a QR code
 *
 * @author Kenneth Joseph, Fawaz Mansoor
 * @version 1.4
 */
public class CreateEventFragment extends Fragment {

    private EditText etName, etDesc, etLocation, etStart, etEnd, etCapacity;
    private ImageView ivPosterPreview;
    private MaterialButton btnSelectPoster, btnPublish;
    private TextView tvSelectedCategory;

    private Uri selectedPosterUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickPosterLauncher;

    private boolean isPublishing = false;
    private String selectedCategory = "All";

    private CheckBox cbGeoRequired;

    private static final String[] CATEGORIES = {
            "All", "Sports", "Music", "Arts", "Education", "Community"
    };

    /**
     * Espresso idling resource used by UI tests to wait for async publish work.
     */
    private static final CountingIdlingResource PUBLISH_IDLING =
            new CountingIdlingResource("CreateEventPublish");

    public CreateEventFragment() { }

    public static IdlingResource getPublishIdlingResource() {
        return PUBLISH_IDLING;
    }

    private static void beginPublishAsync() {
        PUBLISH_IDLING.increment();
    }

    private static void endPublishAsync() {
        if (!PUBLISH_IDLING.isIdleNow()) {
            PUBLISH_IDLING.decrement();
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_create_event, container, false);

        etName = root.findViewById(R.id.et_event_name);
        etDesc = root.findViewById(R.id.et_event_description);
        etLocation = root.findViewById(R.id.et_event_location);
        etStart = root.findViewById(R.id.et_event_start);
        etEnd = root.findViewById(R.id.et_event_end);
        etCapacity = root.findViewById(R.id.et_event_capacity);
        ivPosterPreview = root.findViewById(R.id.iv_event_poster_preview);
        btnSelectPoster = root.findViewById(R.id.btn_select_event_poster);
        btnPublish = root.findViewById(R.id.btn_publish_event);
        tvSelectedCategory = root.findViewById(R.id.tv_selected_category);
        MaterialButton btnPickCategory = root.findViewById(R.id.btn_pick_category);

        btnPickCategory.setOnClickListener(v ->
                requireCompletedProfile(() -> {
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Select Category")
                            .setItems(CATEGORIES, (d, which) -> {
                                selectedCategory = CATEGORIES[which];
                                tvSelectedCategory.setText("Category: " + selectedCategory);
                            })
                            .show();
                })
        );

        setupImagePicker();
        setupDateRangePicker();

        btnSelectPoster.setOnClickListener(v ->
                requireCompletedProfile(this::openPosterPicker)
        );

        btnPublish.setOnClickListener(v ->
                requireCompletedProfile(this::publishEvent)
        );

        configureUiAccess();
        cbGeoRequired = root.findViewById(R.id.cb_geo_required);
        return root;
    }

    /**
     * Registers the visual media picker used to select an event poster.
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
     * Opens the device image picker for selecting an event poster.
     */
    private void openPosterPicker() {
        pickPosterLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

    /**
     * Configures both date fields to open the registration date range picker.
     */
    private void setupDateRangePicker() {
        View.OnClickListener openPicker = v -> showDateRangePicker();
        etStart.setOnClickListener(openPicker);
        etEnd.setOnClickListener(openPicker);
    }

    /**
     * Shows the Material date range picker and fills the selected start/end fields.
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
            etStart.setText(formatDate(selection.first));
            etEnd.setText(formatDate(selection.second));
        });

        picker.show(getChildFragmentManager(), "registration_date_range_picker");
    }

    /**
     * Validates fields and starts the publish flow.
     */
    private void publishEvent() {
        if (isPublishing) {
            return;
        }
        isPublishing = true;

        String name = safe(etName);
        String desc = safe(etDesc);
        String location = safe(etLocation);
        String start = safe(etStart);
        String end = safe(etEnd);
        String capStr = safe(etCapacity);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            isPublishing = false;
            return;
        }

        if (TextUtils.isEmpty(start)) {
            etStart.setError("Required");
            isPublishing = false;
            return;
        }

        if (TextUtils.isEmpty(end)) {
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
        setPublishingState(true, selectedPosterUri == null ? "PUBLISHING..." : "UPLOADING POSTER...");

        if (selectedPosterUri != null) {
            uploadPosterAndCreateEvent(name, desc, location, start, end, capacity);
        } else {
            createEventDocument(name, desc, location, start, end, capacity, null);
        }
    }

    /**
     * Uploads the selected poster to Firebase Storage before creating the event document.
     */
    private void uploadPosterAndCreateEvent(
            String name,
            String desc,
            String location,
            String start,
            String end,
            Integer capacity
    ) {
        String organizerDeviceId = DeviceIdentityService.getDeviceId(requireContext());
        String fileName = "event_posters/" + organizerDeviceId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference posterRef = FirebaseStorage.getInstance().getReference().child(fileName);

        posterRef.putFile(selectedPosterUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Exception exception = task.getException();
                        if (exception != null) {
                            throw exception;
                        }
                    }
                    return posterRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    if (getActivity() == null) {
                        endPublishAsync();
                        return;
                    }
                    createEventDocument(name, desc, location, start, end, capacity, uri.toString());
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) {
                        endPublishAsync();
                        return;
                    }
                    isPublishing = false;
                    setPublishingState(false, "PUBLISH");
                    Toast.makeText(requireContext(),
                            "Poster upload failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    endPublishAsync();
                });
    }

    /**
     * Creates the Firestore document for the event.
     */
    private void createEventDocument(
            String name,
            String desc,
            String location,
            String start,
            String end,
            Integer capacity,
            @Nullable String posterUrl
    ) {
        String organizerDeviceId = DeviceIdentityService.getDeviceId(requireContext());

        Map<String, Object> eventDoc = new HashMap<>();
        eventDoc.put("name", name);
        eventDoc.put("description", desc);
        eventDoc.put("location", location);
        eventDoc.put("registrationStart", start);
        eventDoc.put("registrationEnd", end);
        eventDoc.put("capacity", capacity);
        eventDoc.put("category", selectedCategory);
        eventDoc.put("organizerDeviceId", organizerDeviceId);
        eventDoc.put("createdAt", System.currentTimeMillis());
        eventDoc.put("posterUrl", posterUrl);
        boolean geoRequired = cbGeoRequired != null && cbGeoRequired.isChecked();
        eventDoc.put("geoRequired", geoRequired);
        FirebaseFirestore.getInstance()
                .collection("events")
                .add(eventDoc)
                .addOnSuccessListener(ref -> {
                    if (getActivity() == null) {
                        endPublishAsync();
                        return;
                    }

                    String eventId = ref.getId();
                    String payload = "eventId:" + eventId;

                    Toast.makeText(requireContext(), "Event created", Toast.LENGTH_SHORT).show();
                    isPublishing = false;
                    setPublishingState(false, "PUBLISH");
                    goToQr(payload);
                    endPublishAsync();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) {
                        endPublishAsync();
                        return;
                    }

                    isPublishing = false;
                    setPublishingState(false, "PUBLISH");
                    Toast.makeText(requireContext(),
                            "Create failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    endPublishAsync();
                });
    }

    /**
     * Navigates to the QR code screen for the newly created event.
     *
     * @param payload QR payload string
     */
    private void goToQr(@NonNull String payload) {
        QrCodeFragment qr = QrCodeFragment.newInstance(payload);
        FragmentManager fm = requireActivity().getSupportFragmentManager();

        if (fm.isStateSaved()) {
            fm.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, qr)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else {
            fm.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, qr)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Enables or disables publish-related controls during async work.
     *
     * @param publishing whether publishing is currently in progress
     * @param buttonText text to show on the publish button
     */
    private void setPublishingState(boolean publishing, String buttonText) {
        btnPublish.setEnabled(!publishing);
        btnSelectPoster.setEnabled(!publishing);
        etStart.setEnabled(!publishing);
        etEnd.setEnabled(!publishing);
        btnPublish.setText(buttonText);
    }

    /**
     * Enables or disables the event-creation UI based on profile completion.
     */
    private void configureUiAccess() {
        requireCompletedProfile(new Runnable() {
            @Override
            public void run() {
                btnSelectPoster.setEnabled(true);
                btnPublish.setEnabled(true);
            }
        }, new Runnable() {
            @Override
            public void run() {
                btnSelectPoster.setEnabled(false);
                btnPublish.setEnabled(false);
                etStart.setEnabled(false);
                etEnd.setEnabled(false);
                Toast.makeText(requireContext(),
                        "Complete your profile first to create events.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Runs protected logic only if the current user's profile is completed.
     *
     * @param onAllowed logic to run when the profile is complete
     */
    private void requireCompletedProfile(Runnable onAllowed) {
        requireCompletedProfile(onAllowed, () -> Toast.makeText(
                requireContext(),
                "Complete your profile first to create events.",
                Toast.LENGTH_SHORT
        ).show());
    }

    /**
     * Runs one callback if the current user's profile is completed and another
     * if it is not.
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

    /**
     * Formats a UTC timestamp into yyyy-MM-dd for display.
     *
     * @param utcMillis date in milliseconds
     * @return formatted date string
     */
    private String formatDate(long utcMillis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(utcMillis));
    }

    /**
     * Safely trims text from an EditText.
     *
     * @param et source EditText
     * @return trimmed string or empty string if null
     */
    private String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}