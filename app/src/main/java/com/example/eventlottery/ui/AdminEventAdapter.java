package com.example.eventlottery.ui;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.admin.AdminBrowseEventsActivity;
import com.example.eventlottery.domain.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying events in the admin panel.
 * Handles the display of event items with name, organizer, date, and capacity.
 * Supports click events for viewing details and deleting events.
 *
 * @author Hasrat Singh Chauhan
 * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 * @see AdminBrowseEventsActivity
 * @see R.layout item_admin_event
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private List<Event> eventsFull = new ArrayList<>(); // For search filtering
    private OnEventClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    /**
     * Interface for handling event item click events.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event item is clicked.
         * @param event The event that was clicked
         */
        void onEventClick(Event event);

        /**
         * Called when the delete button on an event is clicked.
         * @param event The event to delete
         */
        void onDeleteClick(Event event);
    }

    /**
     * Sets the click listener for event items.
     * @param listener The listener to handle clicks
     */
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Updates the adapter's dataset.
     * @param events The new list of events to display
     */
    public void setEvents(List<Event> events) {
        this.events = events;
        this.eventsFull = new ArrayList<>(events); // Store full list for filtering
        notifyDataSetChanged();
    }

    /**
     * Filters the event list based on a search query.
     * @param query The search text to filter by
     */
    public void filter(String query) {
        events.clear();

        if (query == null || query.isEmpty()) {
            events.addAll(eventsFull);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (Event event : eventsFull) {
                // Search in event name and organizer ID
                if (event.getName().toLowerCase().contains(lowerQuery) ||
                        (event.getOrganizerDeviceId() != null &&
                                event.getOrganizerDeviceId().toLowerCase().contains(lowerQuery))) {
                    events.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for event items.
     */
    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventName;
        private final TextView tvEventOrganizer;
        private final TextView tvEventDate;
        private final TextView tvEventCapacity;
        private final TextView tvEventStatus;
        private final ImageView btnDelete;
        private final CardView cardView;

        EventViewHolder(@NonNull View itemView) {  // Should be EventViewHolder, not EventViewModel
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventOrganizer = itemView.findViewById(R.id.tvEventOrganizer);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventCapacity = itemView.findViewById(R.id.tvEventCapacity);
            tvEventStatus = itemView.findViewById(R.id.tvEventStatus);
            btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
            cardView = (CardView) itemView;

            // Set up click listeners
            setupClickListeners();  // Make sure this method name matches
        }

        private void setupClickListeners() {
            // Whole card click for viewing details
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEventClick(events.get(position));
                }
            });

            // Delete button click
            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(events.get(position));
                }
            });
        }

        void bind(Event event) {
            // Set event name
            tvEventName.setText(event.getName());

            // Set organizer info
            String organizerText = "Organized by: " +
                    (event.getOrganizerDeviceId() != null ?
                            "ID: " + event.getOrganizerDeviceId().substring(0, Math.min(6, event.getOrganizerDeviceId().length())) + "..."
                            : "Unknown");
            tvEventOrganizer.setText(organizerText);

            // Set event date
            if (event.getEventDate() > 0) {
                tvEventDate.setText(dateFormat.format(new Date(event.getEventDate())));
            } else {
                tvEventDate.setText("Date not set");
            }

            // Set capacity info
            String capacityText;
            if (event.getCapacity() > 0) {
                capacityText = "Capacity: " + event.getCapacity();
            } else {
                capacityText = "Unlimited capacity";
            }
            tvEventCapacity.setText(capacityText);

            // Set status based on registration period
            String status = getEventStatus(event);
            tvEventStatus.setText(status);

            // Set status color
            switch (status) {
                case "ACTIVE":
                    tvEventStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                    break;
                case "INACTIVE":
                    tvEventStatus.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                    break;
                case "COMPLETED":
                    tvEventStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                    break;
                default:
                    tvEventStatus.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }
        }

        /**
         * Determines the current status of an event based on registration dates.
         * @param event The event to check
         * @return Status string: "ACTIVE", "INACTIVE", or "COMPLETED"
         */
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
    }
}
