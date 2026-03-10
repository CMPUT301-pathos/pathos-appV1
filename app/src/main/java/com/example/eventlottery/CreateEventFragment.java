package com.example.eventlottery;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
 * US 02.01.01: Organizer can create a new event and generate a promotional QR code.
 *
 * MVP behavior:
 * - Collect event fields
 * - Choose registration dates with a Material date range picker
 * - Optionally upload an event poster to Firebase Storage
 * - Save to Firestore "events" (auto-id)
 * - Navigate to QrCodeFragment on success
 *
 * Reliability notes:
 * - Prevents double publish
 * - Guards against fragment/activity lifecycle issues
 * - Uses commitAllowingStateLoss only when FragmentManager state is saved
 *
 * @author Kenneth Joseph
 * @version 1.2
 */
public class CreateEventFragment extends Fragment {

    private EditText etName, etDesc, etLocation, etStart, etEnd, etCapacity;
    private ImageView ivPosterPreview;
    private MaterialButton btnSelectPoster, btnPublish;

    private Uri selectedPosterUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickPosterLauncher;

    private boolean isPublishing = false;

    /**
     * Espresso idling resource used by UI tests to wait for async publish work (Firestore/Storage).
     *
     * NOTE: Safe to keep in production code; it is lightweight and only used by tests.
     */
    private static final CountingIdlingResource PUBLISH_IDLING =
            new CountingIdlingResource("CreateEventPublish");

    /**
     * Returns the idling resource so instrumented tests can synchronize on publish completion.
     */
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

    public CreateEventFragment() { }

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

        setupImagePicker();
        setupDateRangePicker();

        btnSelectPoster.setOnClickListener(v -> openPosterPicker());
        btnPublish.setOnClickListener(v -> publishEvent());

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
            etStart.setText(formatDate(selection.first));
            etEnd.setText(formatDate(selection.second));
        });

        picker.show(getChildFragmentManager(), "registration_date_range_picker");
    }

    private void publishEvent() {
        if (isPublishing) return; // prevent double publish
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
                        if (exception != null) throw exception;
                    }
                    return posterRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    // Fragment might have been detached; only proceed if we still have an Activity.
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
                    Toast.makeText(requireContext(), "Poster upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    endPublishAsync();
                });
    }

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
        eventDoc.put("organizerDeviceId", organizerDeviceId);
        eventDoc.put("createdAt", System.currentTimeMillis());
        eventDoc.put("posterUrl", posterUrl);

        FirebaseFirestore.getInstance()
                .collection("events")
                .add(eventDoc)
                .addOnSuccessListener(ref -> {
                    // Even if the fragment is no longer 'added', we can still navigate as long as Activity exists.
                    if (getActivity() == null) {
                        endPublishAsync();
                        return;
                    }

                    String eventId = ref.getId();
                    String payload = "eventId:" + eventId;

                    Toast.makeText(requireContext(), "Event created", Toast.LENGTH_SHORT).show();

                    // Reset state BEFORE navigating so we don't get stuck in "publishing..."
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
                    Toast.makeText(requireContext(), "Create failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    endPublishAsync();
                });
    }

    /**
     * Navigates to QrCodeFragment safely.
     * Uses commitAllowingStateLoss only if state is already saved.
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

    private void setPublishingState(boolean publishing, String buttonText) {
        btnPublish.setEnabled(!publishing);
        btnSelectPoster.setEnabled(!publishing);
        etStart.setEnabled(!publishing);
        etEnd.setEnabled(!publishing);
        btnPublish.setText(buttonText);
    }

    private String formatDate(long utcMillis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(utcMillis));
    }

    private String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}