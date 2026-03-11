package com.example.eventlottery.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.NotificationLog;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying notification logs in the admin panel.
 * Supports search filtering and click events for viewing details.
 *
 * @author hasratsinghchauhan
 * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 * */
public class NotificationLogAdapter extends RecyclerView.Adapter<NotificationLogAdapter.ViewHolder> {

    private List<NotificationLog> logs = new ArrayList<>();
    private List<NotificationLog> logsFull = new ArrayList<>(); // For search filtering
    private OnLogClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    /**
     * Interface for handling log item click events.
     */
    public interface OnLogClickListener {
        /**
         * Called when a log item is clicked.
         * @param log The notification log that was clicked
         */
        void onLogClick(NotificationLog log);
    }

    /**
     * Sets the click listener for log items.
     * @param listener The listener to handle clicks
     */
    public void setOnLogClickListener(OnLogClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationLog log = logs.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    /**
     * Updates the adapter's dataset.
     * @param logs The new list of notification logs to display
     */
    public void setLogs(List<NotificationLog> logs) {
        this.logs = logs;
        this.logsFull = new ArrayList<>(logs); // Store full list for filtering
        notifyDataSetChanged();
    }

    /**
     * Filters the log list based on a search query.
     * @param query The search text to filter by
     */
    public void filter(String query) {
        logs.clear();

        if (query == null || query.isEmpty()) {
            logs.addAll(logsFull);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (NotificationLog log : logsFull) {
                // Search in sender name, recipient name, and event name
                if (log.getSenderName().toLowerCase().contains(lowerQuery) ||
                        log.getRecipientName().toLowerCase().contains(lowerQuery) ||
                        (log.getEventName() != null && log.getEventName().toLowerCase().contains(lowerQuery))) {
                    logs.add(log);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for notification log items.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLogSender;
        private final TextView tvLogRecipient;
        private final TextView tvLogEvent;
        private final TextView tvLogType;
        private final TextView tvLogTime;
        private final TextView tvLogStatus;
        private final TextView tvLogPreview;
        private final CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogSender = itemView.findViewById(R.id.tvLogSender);
            tvLogRecipient = itemView.findViewById(R.id.tvLogRecipient);
            tvLogEvent = itemView.findViewById(R.id.tvLogEvent);
            tvLogType = itemView.findViewById(R.id.tvLogType);
            tvLogTime = itemView.findViewById(R.id.tvLogTime);
            tvLogStatus = itemView.findViewById(R.id.tvLogStatus);
            tvLogPreview = itemView.findViewById(R.id.tvLogPreview);
            cardView = (CardView) itemView;

            // Set up click listener
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onLogClick(logs.get(position));
                }
            });
        }

        void bind(NotificationLog log) {
            // Set sender info
            tvLogSender.setText("From: " + log.getSenderName());

            // Set recipient info
            tvLogRecipient.setText("To: " + log.getRecipientName());

            // Set event info (if available)
            if (log.getEventName() != null && !log.getEventName().isEmpty()) {
                tvLogEvent.setText("Event: " + log.getEventName());
                tvLogEvent.setVisibility(View.VISIBLE);
            } else {
                tvLogEvent.setVisibility(View.GONE);
            }

            // Format and set notification type
            String type = log.getNotificationType();
            if (type != null) {
                // Convert from "lottery_won" to "Lottery Won"
                String[] words = type.split("_");
                StringBuilder formattedType = new StringBuilder();
                for (String word : words) {
                    if (word.length() > 0) {
                        formattedType.append(word.substring(0, 1).toUpperCase())
                                .append(word.substring(1).toLowerCase())
                                .append(" ");
                    }
                }
                tvLogType.setText(formattedType.toString().trim());
            } else {
                tvLogType.setText("Unknown");
            }

            // Set timestamp
            if (log.getTimestamp() != null) {
                Date date = log.getTimestamp().toDate();
                tvLogTime.setText(dateFormat.format(date));
            } else {
                tvLogTime.setText("Unknown");
            }

            // Set status with appropriate color
            String status = log.getStatus() != null ? log.getStatus() : "sent";
            tvLogStatus.setText(status.toUpperCase());

            // Color code the status
            int statusColor;
            switch (status.toLowerCase()) {
                case "failed":
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "delivered":
                    statusColor = android.R.color.holo_green_dark;
                    break;
                case "sent":
                default:
                    statusColor = android.R.color.darker_gray;
                    break;
            }
            tvLogStatus.setTextColor(itemView.getContext().getColor(statusColor));

            // Set message preview
            if (tvLogPreview != null && log.getMessage() != null) {
                tvLogPreview.setText(log.getMessage());
                tvLogPreview.setVisibility(View.VISIBLE);
            } else if (tvLogPreview != null) {
                tvLogPreview.setVisibility(View.GONE);
            }

            // Optional: Set background color based on status
            if ("failed".equalsIgnoreCase(status)) {
                cardView.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.background_light));
            } else {
                cardView.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
            }
        }
    }
}
