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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * CreateEventFragment
 *
 * Allows an organizer to create a new event, optionally marked as private.
 * Private events are not visible in the event listing and do not generate
 * a promotional QR code. Entrants must be invited manually.
 *
 * Responsibilities:
 * - collect event fields including category and event date
 * - choose registration dates with a Material date range picker
 * - optionally upload an event poster (Base64 encoded)
 * - toggle private event mode
 * - save the event to the Firestore "events" collection
 * - navigate to QrCodeFragment on successful publish (public events only)
 * - navigate to PrivateEventInviteFragment for private events
 *
 * User stories supported:
 * - US 02.01.01: Organizer can create a new event and generate a QR code
 * - US 02.01.04: Organizer sets a registration period
 * - US 02.07.01: Organizer creates a private event
 * - US 02.07.02: Organizer invites specific entrants to a private event
 *
 * @author Kenneth Joseph, Fawaz Mansoor
 * @version 1.7
 */
public class CreateEventFragment extends Fragment {

    private EditText etName, etDesc, etLocation, etStart, etEnd, etCapacity;
    private EditText etEventDate;
    private ImageView ivPosterPreview;
    private MaterialButton btnSelectPoster, btnPublish;
    private TextView tvSelectedCategory;
    private Switch switchPrivate;

    private Uri selectedPosterUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickPosterLauncher;
    private boolean isPublishing = false;
    private boolean isPrivateEvent = false;
    private String selectedCategory = "All";

    private long registrationStartMillis = 0;
    private long registrationEndMillis = 0;
    private long eventDateMillis = 0;

    private static final String[] CATEGORIES = {
            "All", "Sports", "Music", "Arts", "Education", "Community"
    };

    private static final CountingIdlingResource PUBLISH_IDLING =
            new CountingIdlingResource("CreateEventPublish");

    public CreateEventFragment() { }

    public static IdlingResource getPublishIdlingResource() {
        return PUBLISH_IDLING;
    }

    private static void beginPublishAsync() { PUBLISH_IDLING.increment(); }

    private static void endPublishAsync() {
        if (!PUBLISH_IDLING.isIdleNow()) PUBLISH_IDLING.decrement();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_event, container, false);

        etName        = root.findViewById(R.id.et_event_name);
        etDesc        = root.findViewById(R.id.et_event_description);
        etLocation    = root.findViewById(R.id.et_event_location);
        etStart       = root.findViewById(R.id.et_event_start);
        etEnd         = root.findViewById(R.id.et_event_end);
        etCapacity    = root.findViewById(R.id.et_event_capacity);
        etEventDate   = root.findViewById(R.id.et_event_date);
        ivPosterPreview  = root.findViewById(R.id.iv_event_poster_preview);
        btnSelectPoster  = root.findViewById(R.id.btn_select_event_poster);
        btnPublish       = root.findViewById(R.id.btn_publish_event);
        tvSelectedCategory = root.findViewById(R.id.tv_selected_category);
        switchPrivate    = root.findViewById(R.id.switch_private_event);
        MaterialButton btnPickCategory = root.findViewById(R.id.btn_pick_category);

        // Update publish button text when private toggle changes
        switchPrivate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isPrivateEvent = isChecked;
            btnPublish.setText(isChecked ? "PUBLISH PRIVATE EVENT" : "GENERATE QR CODE & POST");
        });

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

        setupImagePicker();
        setupDateRangePicker();
        setupEventDatePicker();

        btnSelectPoster.setOnClickListener(v ->
                requireCompletedProfile(this::openPosterPicker));

        btnPublish.setOnClickListener(v ->
                requireCompletedProfile(this::publishEvent));

        configureUiAccess();

        return root;
    }

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

    private void openPosterPicker() {
        pickPosterLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

    private void setupDateRangePicker() {
        View.OnClickListener openPicker = v -> showDateRangePicker();
        etStart.setOnClickListener(openPicker);
        etEnd.setOnClickListener(openPicker);
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select registration dates")
                        .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) return;
            registrationStartMillis = selection.first;
            registrationEndMillis   = selection.second;
            etStart.setText(formatDate(selection.first));
            etEnd.setText(formatDate(selection.second));
        });
        picker.show(getChildFragmentManager(), "registration_date_range_picker");
    }

    private void setupEventDatePicker() {
        etEventDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select event date")
                            .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection == null) return;
                eventDateMillis = selection;
                etEventDate.setText(formatDate(selection));
            });
            picker.show(getChildFragmentManager(), "event_date_picker");
        });
    }

    private void publishEvent() {
        if (isPublishing) return;
        isPublishing = true;

        String name     = safe(etName);
        String desc     = safe(etDesc);
        String location = safe(etLocation);
        String capStr   = safe(etCapacity);

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
            if (getActivity() == null) { endPublishAsync(); return; }
            isPublishing = false;
            setPublishingState(false, "PUBLISH");
            Toast.makeText(requireContext(),
                    "Poster processing failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            endPublishAsync();
        }
    }

    private void createEventDocument(String name, String desc, String location,
                                     Integer capacity, @Nullable String posterUrl) {
        String organizerDeviceId = DeviceIdentityService.getDeviceId(requireContext());

        Map<String, Object> eventDoc = new HashMap<>();
        eventDoc.put("name",               name);
        eventDoc.put("description",        desc);
        eventDoc.put("location",           location);
        eventDoc.put("registrationStart",  registrationStartMillis);
        eventDoc.put("registrationEnd",    registrationEndMillis);
        eventDoc.put("eventDate",          eventDateMillis);
        eventDoc.put("capacity",           capacity);
        eventDoc.put("category",           selectedCategory);
        eventDoc.put("organizerDeviceId",  organizerDeviceId);
        eventDoc.put("createdAt",          System.currentTimeMillis());
        eventDoc.put("posterUrl",          posterUrl);
        eventDoc.put("isPrivate",          isPrivateEvent);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .add(eventDoc)
                .addOnSuccessListener(ref -> {
                    if (getActivity() == null) { endPublishAsync(); return; }

                    String eventId = ref.getId();
                    String qrPayload = "eventId:" + eventId;

                    if (!isPrivateEvent) {
                        ref.update("qrPayload", qrPayload);
                    }

                    Toast.makeText(requireContext(), "Event created!", Toast.LENGTH_SHORT).show();
                    isPublishing = false;
                    setPublishingState(false, "PUBLISH");

                    if (isPrivateEvent) {
                        // Navigate to invite screen instead of QR
                        goToInvite(eventId, name);
                    } else {
                        goToQr(qrPayload);
                    }
                    endPublishAsync();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) { endPublishAsync(); return; }
                    isPublishing = false;
                    setPublishingState(false, "PUBLISH");
                    Toast.makeText(requireContext(),
                            "Create failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    endPublishAsync();
                });
    }

    private void goToQr(@NonNull String payload) {
        QrCodeFragment qr = QrCodeFragment.newInstance(payload);
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, qr)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

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

    private void setPublishingState(boolean publishing, String buttonText) {
        btnPublish.setEnabled(!publishing);
        btnSelectPoster.setEnabled(!publishing);
        etStart.setEnabled(!publishing);
        etEnd.setEnabled(!publishing);
        btnPublish.setText(buttonText);
    }

    private void configureUiAccess() {
        requireCompletedProfile(
                () -> {
                    btnSelectPoster.setEnabled(true);
                    btnPublish.setEnabled(true);
                },
                () -> {
                    btnSelectPoster.setEnabled(false);
                    btnPublish.setEnabled(false);
                    etStart.setEnabled(false);
                    etEnd.setEnabled(false);
                    Toast.makeText(requireContext(),
                            "Complete your profile first to create events.",
                            Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void requireCompletedProfile(Runnable onAllowed) {
        requireCompletedProfile(onAllowed, () -> Toast.makeText(
                requireContext(),
                "Complete your profile first to create events.",
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

    private String formatDate(long utcMillis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(utcMillis));
    }

    private String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
