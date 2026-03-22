package com.example.eventlottery.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.EventComment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying event comments.
 *
 * User stories supported:
 * - US 01.08.01: Post comments on an event
 * - US 01.08.02: View comments on an event
 *
 * @author Edwin David
 * @version 1.0
 */
public class EventCommentAdapter extends RecyclerView.Adapter<EventCommentAdapter.VH> {

    private final List<EventComment> comments;
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public EventCommentAdapter(List<EventComment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Create the comment row layout from XML
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EventComment c = comments.get(position);
        h.authorName.setText(c.getAuthorName());
        h.text.setText(c.getText());

        //Format the timestamp
        h.timestamp.setText(DATE_FORMAT.format(new Date(c.getCreatedAt())));
    }

    @Override
    public int getItemCount() { return comments.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView authorName, text, timestamp;

        VH(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.text_comment_author);
            text = itemView.findViewById(R.id.text_comment_body);
            timestamp = itemView.findViewById(R.id.text_comment_time);
        }
    }
}
