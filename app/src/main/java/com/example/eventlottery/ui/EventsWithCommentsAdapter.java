package com.example.eventlottery.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventsWithCommentsAdapter extends RecyclerView.Adapter<EventsWithCommentsAdapter.EventViewHolder> {

    private List<Map<String, Object>> events = new ArrayList<>();
    private OnEventClickListener clickListener;

    public interface OnEventClickListener {
        void onEventClick(String eventId, String eventName);
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.clickListener = listener;
    }

    public void setEvents(List<Map<String, Object>> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_with_comments, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Map<String, Object> event = events.get(position);
        String eventId = (String) event.get("eventId");
        String eventName = (String) event.get("eventName");
        int commentCount = (int) event.get("commentCount");

        holder.bind(eventId, eventName, commentCount);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private CardView cardEvent;
        private TextView tvEventName;
        private TextView tvEventId;
        private TextView tvCommentCount;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardEvent = itemView.findViewById(R.id.cardEvent);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventId = itemView.findViewById(R.id.tvEventId);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
        }

        void bind(String eventId, String eventName, int commentCount) {
            tvEventName.setText(eventName);
            tvEventId.setText("Event ID: " + eventId);
            tvCommentCount.setText(commentCount + " comment" + (commentCount != 1 ? "s" : ""));

            cardEvent.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onEventClick(eventId, eventName);
                }
            });
        }
    }
}
