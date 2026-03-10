package com.example.eventlottery.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.EventSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying EventSummary cards.
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class EventSummaryAdapter extends RecyclerView.Adapter<EventSummaryAdapter.VH> {

    private final List<EventSummary> items = new ArrayList<>();

    public void setItems(List<EventSummary> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_summary, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EventSummary e = items.get(position);

        h.tvName.setText(e.getName());
        h.tvDesc.setText(e.getDescription());

        String meta = "";
        if (!e.getLocation().isEmpty()) meta = "📍 " + e.getLocation();
        if (meta.isEmpty()) meta = "Event ID: " + e.getId();
        h.tvMeta.setText(meta);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvDesc, tvMeta;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_event_name);
            tvDesc = itemView.findViewById(R.id.tv_event_desc);
            tvMeta = itemView.findViewById(R.id.tv_event_meta);
        }
    }
}