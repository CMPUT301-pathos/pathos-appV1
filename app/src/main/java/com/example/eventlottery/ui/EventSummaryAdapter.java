package com.example.eventlottery.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.EventSummary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying EventSummary cards in a RecyclerView.
 *
 * Extended to support filtering by category and viewing lottery criteria.
 *
 * User stories supported:
 * - US 01.01.03: See a list of events to join the waiting list for
 * - US 01.01.04: Filter events based on interests and availability
 * - US 01.05.05: Be informed about lottery selection criteria
 *
 * @author Kenneth Joseph, Fawaz Mansoor
 * @version 1.3
 */
public class EventSummaryAdapter extends RecyclerView.Adapter<EventSummaryAdapter.VH> {

    private final List<EventSummary> items = new ArrayList<>();
    private final List<EventSummary> allItems = new ArrayList<>();
    private OnCriteriaClickListener criteriaClickListener;
    private OnItemClickListener itemClickListener;
    private OnOrganizerActionListener organizerActionListener;
    private boolean showOrganizerActions = false;

    public interface OnCriteriaClickListener {
        void onCriteriaClick(EventSummary event);
    }
    public interface OnItemClickListener{
        void onItemClick(EventSummary event);
    }

    public interface OnOrganizerActionListener {
        void onSeeQrClick(EventSummary event);
        void onEditClick(EventSummary event);
        void onGeoDetailsClick(EventSummary event);
    }

    /**
     * Sets the listener that will be triggered when the user requests
     * to view the lottery criteria for an event.
     *
     * @param listener callback invoked when the criteria button is clicked
     */
    public void setCriteriaClickListener(OnCriteriaClickListener listener) {
        this.criteriaClickListener = listener;
    }
    /**
     * Sets the listener triggered when an event card is clicked.
     *
     * @param listener callback invoked when an event item is selected
     */
    public void setItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * Sets the listener used for organizer-specific actions
     * such as viewing QR codes, editing events, or viewing
     * geolocation details.
     *
     * @param listener organizer action callback
     */
    public void setOrganizerActionListener(OnOrganizerActionListener listener) {
        this.organizerActionListener = listener;
    }

    /**
     * Enables or disables organizer action buttons in each event card.
     * When enabled, organizer controls (QR, edit, geo details) become visible.
     *
     * @param showOrganizerActions true to display organizer actions
     */
    public void setShowOrganizerActions(boolean showOrganizerActions) {
        this.showOrganizerActions = showOrganizerActions;
        notifyDataSetChanged();
    }

    /**
     * Replaces the current list of events with a new dataset.
     * This also resets the filtered list used for category filtering.
     *
     * @param list list of EventSummary objects to display
     */
    public void setItems(List<EventSummary> list) {
        allItems.clear();
        if (list != null) allItems.addAll(list);
        items.clear();
        items.addAll(allItems);
        notifyDataSetChanged();
    }
    /**
     * Updates the adapter with a filtered list of events.
     *
     * @param list filtered list of EventSummary objects
     */
    public void setFilteredItems(List<EventSummary> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }
    /**
     * Filters the displayed events by category.
     * If the category is "All", the full event list is restored.
     *
     * @param category category name used for filtering
     */
    public void filterByCategory(String category) {
        items.clear();
        if (category == null || category.equals("All")) {
            items.addAll(allItems);
        } else {
            for (EventSummary e : allItems) {
                if (e.getCategory().equalsIgnoreCase(category)) items.add(e);
            }
        }
        notifyDataSetChanged();
    }


    /**
     * Inflates the event card layout and creates a new ViewHolder.
     *
     * @param parent parent view group
     * @param viewType type of view
     * @return ViewHolder containing the inflated event layout
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_summary, parent, false);
        return new VH(v);
    }


    /**
     * Binds event data to a ViewHolder and configures click listeners
     * for user and organizer interactions.
     *
     * @param h the ViewHolder representing the event card
     * @param position position of the event in the list
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EventSummary e = items.get(position);

        h.tvName.setText(e.getName());
        h.tvDesc.setText(e.getDescription());

        String meta = "";
        if (!e.getLocation().isEmpty()) meta = "📍 " + e.getLocation();
        if (!e.getCategory().isEmpty()) meta += "  🏷️ " + e.getCategory();
        if (e.getEventDate() > 0) {
            String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(e.getEventDate()));
            meta += "  📅 " + date;
        }
        if (meta.isEmpty()) meta = "Event ID: " + e.getId();
        h.tvMeta.setText(meta);

        int organizerVisibility = showOrganizerActions ? View.VISIBLE : View.GONE;
        h.organizerActionsRow.setVisibility(organizerVisibility);

        h.btnLotteryCriteria.setOnClickListener(v -> {
            if (criteriaClickListener != null) {
                criteriaClickListener.onCriteriaClick(e);
            }
        });

        h.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(e);
            }
        });

        h.btnSeeQr.setOnClickListener(v -> {
            if (organizerActionListener != null) {
                organizerActionListener.onSeeQrClick(e);
            }
        });

        h.btnEditEvent.setOnClickListener(v -> {
            if (organizerActionListener != null) {
                organizerActionListener.onEditClick(e);
            }
        });

        h.btnGeoDetails.setOnClickListener(v -> {
            if (organizerActionListener != null) {
                organizerActionListener.onGeoDetailsClick(e);
            }
        });
    }


    /**
     * Returns the number of events currently displayed in the adapter.
     *
     * @return number of event items
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class representing a single event card view.
     * Holds references to UI components to improve RecyclerView performance.
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvDesc, tvMeta;
        final Button btnLotteryCriteria, btnSeeQr, btnEditEvent, btnGeoDetails;
        final View organizerActionsRow;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_event_name);
            tvDesc = itemView.findViewById(R.id.tv_event_desc);
            tvMeta = itemView.findViewById(R.id.tv_event_meta);
            btnLotteryCriteria = itemView.findViewById(R.id.btn_lottery_criteria);
            btnSeeQr = itemView.findViewById(R.id.btn_see_qr);
            btnEditEvent = itemView.findViewById(R.id.btn_edit_event);
            btnGeoDetails = itemView.findViewById(R.id.btn_geo_details);
            organizerActionsRow = itemView.findViewById(R.id.layout_organizer_actions);
        }
    }
}