package com.example.eventlottery;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.service.DeviceIdentityService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * CreateEventFragment
 *
 * US 02.01.01: Organizer can create a new event and generate a promotional QR code.
 *
 * Minimal MVP:
 * - Collect event fields
 * - Save to Firestore collection "events" (auto-id)
 * - Navigate to QrCodeFragment to display the QR payload
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class CreateEventFragment extends Fragment {

    private EditText etName, etDesc, etLocation, etStart, etEnd, etCapacity;
    private MaterialButton btnPublish;

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
        btnPublish = root.findViewById(R.id.btn_publish_event);

        btnPublish.setOnClickListener(v -> publishEvent());

        return root;
    }

    private void publishEvent() {
        String name = safe(etName);
        String desc = safe(etDesc);
        String location = safe(etLocation);
        String start = safe(etStart);
        String end = safe(etEnd);
        String capStr = safe(etCapacity);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(start)) {
            etStart.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(end)) {
            etEnd.setError("Required");
            return;
        }

        Integer capacity = null;
        if (!TextUtils.isEmpty(capStr)) {
            try {
                capacity = Integer.parseInt(capStr);
                if (capacity < 1) {
                    etCapacity.setError("Must be >= 1");
                    return;
                }
            } catch (NumberFormatException e) {
                etCapacity.setError("Enter a number");
                return;
            }
        }

        btnPublish.setEnabled(false);
        btnPublish.setText("PUBLISHING...");

        String organizerDeviceId = DeviceIdentityService.getDeviceId(requireContext());

        Map<String, Object> eventDoc = new HashMap<>();
        eventDoc.put("name", name);
        eventDoc.put("description", desc);
        eventDoc.put("location", location);
        eventDoc.put("registrationStart", start);
        eventDoc.put("registrationEnd", end);
        eventDoc.put("capacity", capacity); // can be null
        eventDoc.put("organizerDeviceId", organizerDeviceId);
        eventDoc.put("createdAt", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("events")
                .add(eventDoc)
                .addOnSuccessListener(ref -> {
                    String eventId = ref.getId();
                    String payload = "eventId:" + eventId; // simple stable payload for QR

                    Toast.makeText(requireContext(), "Event created", Toast.LENGTH_SHORT).show();

                    QrCodeFragment qr = QrCodeFragment.newInstance(payload);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, qr)
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e -> {
                    btnPublish.setEnabled(true);
                    btnPublish.setText("PUBLISH");
                    Toast.makeText(requireContext(), "Create failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}