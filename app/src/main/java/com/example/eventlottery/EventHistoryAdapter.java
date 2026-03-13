package com.example.eventlottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.eventlottery.domain.EventHistoryRecord;

import java.util.List;

/**
 * Custom ArrayAdapter for displaying event history records in a ListView.
 * This adapter handles the visualization of event history items with color-coded status indicators.
 * Used in conjunction with EventHistoryFragment to display US 01.02.03 (Event History).
 *
 * The adapter provides:
 * - Efficient view recycling using convertView pattern
 * - Color-coded status badges (Green for selected/accepted, Red for not selected/declined, Orange for waiting)
 * - Clean separation of data and presentation
 *
 * @author Hasrat Singh Chauhan
 * @version 1.0
 * @see EventHistoryRecord
 * @see EventHistoryFragment
 * @see BaseAdapter
 * @since 1.0
 */
public class EventHistoryAdapter extends BaseAdapter {

    /**
     * List of event history records to be displayed.
     * This list is maintained by the parent fragment and updated as data changes.
     */
    private List<EventHistoryRecord> historyList;

    /**
     * LayoutInflater service for inflating the item layout.
     * Used to create new views for each list item when needed.
     */
    private LayoutInflater inflater;

    /**
     * Constructs a new EventHistoryAdapter.
     *
     * @param historyList The list of EventHistoryRecord objects to display
     * @param inflater    The LayoutInflater service to use for creating views
     */
    public EventHistoryAdapter(List<EventHistoryRecord> historyList, LayoutInflater inflater) {
        this.historyList = historyList;
        this.inflater = inflater;
    }

    /**
     * Returns the total number of items in the data set.
     * Required by BaseAdapter to determine list size.
     *
     * @return The number of items in the history list
     */
    @Override
    public int getCount() {
        return historyList.size();
    }

    /**
     * Returns the data item at the specified position.
     *
     * @param position The position of the item within the adapter's data set
     * @return The EventHistoryRecord at the specified position
     */
    @Override
    public Object getItem(int position) {
        return historyList.get(position);
    }

    /**
     * Returns the row ID associated with the specified position.
     * In this implementation, the position itself is used as the ID.
     *
     * @param position The position of the item within the adapter's data set
     * @return The ID of the item at the specified position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * Implements view recycling pattern for optimal performance.
     *
     * The view includes:
     * - Event name displayed prominently
     * - Event date shown below the name
     * - Color-coded status badge indicating the user's selection result
     *
     * Color coding:
     * - Green (#4CAF50): User was selected or accepted the invitation
     * - Red (#F44336): User was not selected or declined the invitation
     * - Orange (#FF9800): User is still waiting or other status
     *
     * @param position    The position of the item within the adapter's data set
     * @param convertView The old view to reuse, if possible
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Recycle existing view if available, otherwise inflate new one
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_history, parent, false);
        }

        // Get the history record for this position
        EventHistoryRecord record = historyList.get(position);

        // Find and populate views
        TextView eventName = convertView.findViewById(R.id.event_name);
        TextView eventDate = convertView.findViewById(R.id.event_date);
        TextView eventStatus = convertView.findViewById(R.id.event_status);

        eventName.setText(record.getEventName());
        eventDate.setText(record.getEventDate());
        eventStatus.setText(record.getStatus());

        // Color code the status badge based on the outcome
        String status = record.getStatus();
        if (status == null) {
            eventStatus.setBackgroundColor(0xFFFF9800); // Default orange
            return convertView;
        }

        switch (status) {
            case "SELECTED":
            case "ACCEPTED":
                // Green for positive outcomes
                eventStatus.setBackgroundColor(0xFF4CAF50);
                break;

            case "NOT_SELECTED":
            case "DECLINED":
            case "CANCELLED":
                // Red for negative outcomes
                eventStatus.setBackgroundColor(0xFFF44336);
                break;

            case "WAITING":
                // Orange for pending/in-progress
                eventStatus.setBackgroundColor(0xFFFF9800);
                break;

            default:
                // Fallback color for unknown statuses
                eventStatus.setBackgroundColor(0xFFFF9800);
                break;
        }

        return convertView;
    }

    /**
     * Updates the adapter's data set and refreshes the ListView.
     * Should be called whenever the underlying data changes.
     *
     * @param newHistoryList The updated list of EventHistoryRecord objects
     */
    public void updateData(List<EventHistoryRecord> newHistoryList) {
        this.historyList = newHistoryList;
        notifyDataSetChanged();
    }
}
