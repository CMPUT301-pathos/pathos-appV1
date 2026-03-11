package com.example.eventlottery.admin;

/**
 *View event details before deleting
 * @author hasratsinghchauhan
 *  * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 */

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.domain.Event;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminEventDetails extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";

    private CardView cardPoster;
    private ImageView ivEventPoster;
    private TextView tvDetailEventName;
    private TextView tvDetailStatus;
    private TextView tvDetailDescription;
    private TextView tvDetailOrganizer;
    private TextView tvDetailEventDate;
    private TextView tvDetailEntrantCount;
    private TextView tvDetailCapacity;
    private TextView tvDetailEventId;
    private CardView cardWarning;
    private TextView tvWarningMessage;
    private LinearLayout layoutCapacity;
    private MaterialButton btnDeleteEvent;

    private FirebaseFirestore db;
    private Event currentEvent;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_details);  // Your layout file

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Event Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupDeleteButton();
        loadEventDetails();
    }

    private void initViews() {
        cardPoster = findViewById(R.id.cardPoster);
        ivEventPoster = findViewById(R.id.ivEventPoster);
        tvDetailEventName = findViewById(R.id.tvDetailEventName);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailOrganizer = findViewById(R.id.tvDetailOrganizer);
        tvDetailEventDate = findViewById(R.id.tvDetailEventDate);
        tvDetailEntrantCount = findViewById(R.id.tvDetailEntrantCount);
        tvDetailCapacity = findViewById(R.id.tvDetailCapacity);
        tvDetailEventId = findViewById(R.id.tvDetailEventId);
        cardWarning = findViewById(R.id.cardWarning);
        tvWarningMessage = findViewById(R.id.tvWarningMessage);
        layoutCapacity = findViewById(R.id.layoutCapacity);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
    }

    private void setupDeleteButton() {
        btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void loadEventDetails() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    currentEvent = documentSnapshot.toObject(Event.class);
                    if (currentEvent != null) {
                        currentEvent.setId(documentSnapshot.getId());
                        displayEventDetails();
                    } else {
                        Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayEventDetails() {
        tvDetailEventName.setText(currentEvent.getName());

        // Status logic - you can add this to your Event class or compute here
        String status = getEventStatus(currentEvent);
        tvDetailStatus.setText(status);

        int statusColor;
        switch (status) {
            case "ACTIVE":
                statusColor = android.R.color.holo_green_dark;
                break;
            case "INACTIVE":
                statusColor = android.R.color.darker_gray;
                break;
            case "COMPLETED":
                statusColor = android.R.color.holo_blue_dark;
                break;
            default:
                statusColor = android.R.color.holo_orange_dark;
        }
        tvDetailStatus.setBackgroundColor(getColor(statusColor));

        // Description
        String description = currentEvent.getDescription();
        if (description != null && !description.isEmpty()) {
            tvDetailDescription.setText(description);
        } else {
            tvDetailDescription.setText("No description provided");
        }

        // Organizer (using device ID since that's what your Event has)
        String organizerId = currentEvent.getOrganizerDeviceId();
        tvDetailOrganizer.setText(organizerId != null ? "ID: " + organizerId : "Unknown");

        // Event date (your Event uses long timestamp)
        long eventDate = currentEvent.getEventDate();
        if (eventDate > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            tvDetailEventDate.setText(dateFormat.format(new Date(eventDate)));
        } else {
            tvDetailEventDate.setText("No date specified");
        }

        // Entrant count - you'd need to add this to your Event class or compute it
        tvDetailEntrantCount.setText("0"); // Placeholder

        // Capacity
        int capacity = currentEvent.getCapacity();
        if (capacity > 0) {
            layoutCapacity.setVisibility(View.VISIBLE);
            tvDetailCapacity.setText(String.valueOf(capacity));
        } else {
            layoutCapacity.setVisibility(View.GONE);
        }

        // Event ID
        tvDetailEventId.setText(currentEvent.getId());

        // Poster
        String posterUrl = currentEvent.getPosterUrl();
        if (posterUrl != null && !posterUrl.isEmpty()) {
            cardPoster.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(posterUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivEventPoster);
        } else {
            cardPoster.setVisibility(View.GONE);
        }

        // Warning - you can implement this in your Event class
        cardWarning.setVisibility(View.GONE); // Placeholder
    }

    private String getEventStatus(Event event) {
        long now = System.currentTimeMillis();
        if (now < event.getRegistrationStart()) {
            return "INACTIVE";
        } else if (now <= event.getRegistrationEnd()) {
            return "ACTIVE";
        } else {
            return "COMPLETED";
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + currentEvent.getName() + "\"?\n\n" +
                        "This action cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent() {
        btnDeleteEvent.setEnabled(false);
        btnDeleteEvent.setText("Deleting...");

        db.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnDeleteEvent.setEnabled(true);
                    btnDeleteEvent.setText("Delete Event");
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
