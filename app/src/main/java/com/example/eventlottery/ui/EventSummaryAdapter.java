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
 * @version 1.2
 */
public class EventSummaryAdapter extends RecyclerView.Adapter<EventSummaryAdapter.VH> {

    private final List<EventSummary> items = new ArrayList<>();
    private final List<EventSummary> allItems = new ArrayList<>();
    private OnCriteriaClickListener criteriaClickListener;

    public interface OnCriteriaClickListener {
        void onCriteriaClick(EventSummary event);
    }

    public void setCriteriaClickListener(OnCriteriaClickListener listener) {
        this.criteriaClickListener = listener;
    }

    public void setItems(List<EventSummary> list) {
        allItems.clear();
        if (list != null) allItems.addAll(list);
        items.clear();
        items.addAll(allItems);
        notifyDataSetChanged();
    }

    public void setFilteredItems(List<EventSummary> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

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
        if (!e.getCategory().isEmpty()) meta += "  🏷️ " + e.getCategory();
        if (e.getEventDate() > 0) {
            String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(e.getEventDate()));
            meta += "  📅 " + date;
        }
        if (meta.isEmpty()) meta = "Event ID: " + e.getId();
        h.tvMeta.setText(meta);

        h.btnLotteryCriteria.setOnClickListener(v -> {
            if (criteriaClickListener != null) {
                criteriaClickListener.onCriteriaClick(e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvDesc, tvMeta;
        final Button btnLotteryCriteria;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_event_name);
            tvDesc = itemView.findViewById(R.id.tv_event_desc);
            tvMeta = itemView.findViewById(R.id.tv_event_meta);
            btnLotteryCriteria = itemView.findViewById(R.id.btn_lottery_criteria);
        }
    }
}