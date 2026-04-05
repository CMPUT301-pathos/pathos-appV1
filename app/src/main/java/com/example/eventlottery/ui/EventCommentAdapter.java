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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventCommentAdapter extends RecyclerView.Adapter<EventCommentAdapter.CommentViewHolder> {

    private List<EventComment> comments = new ArrayList<>();
    private OnCommentClickListener clickListener;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public interface OnCommentClickListener {
        void onCommentClick(EventComment comment);
        void onCommentLongClick(EventComment comment);
        void onDeleteClick(EventComment comment);
    }

    public EventCommentAdapter() {}

    public void setComments(List<EventComment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        EventComment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView authorName;
        TextView text;
        TextView timestamp;
        TextView btnDelete;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.tvCommentAuthor);
            text = itemView.findViewById(R.id.tvCommentText);
            timestamp = itemView.findViewById(R.id.tvCommentTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);

            // Normal click - show details
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (clickListener != null && pos != RecyclerView.NO_POSITION) {
                    clickListener.onCommentClick(comments.get(pos));
                }
            });

            // Delete button click
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (clickListener != null && pos != RecyclerView.NO_POSITION) {
                        clickListener.onDeleteClick(comments.get(pos));
                    }
                });
            }
        }

        void bind(EventComment comment) {
            authorName.setText(comment.getUserName() != null && !comment.getUserName().isEmpty()
                    ? comment.getUserName() : "Anonymous");
            text.setText(comment.getContent() != null ? comment.getContent() : "");

            if (comment.getTimestamp() != null) {
                timestamp.setText(DATE_FORMAT.format(comment.getTimestamp().toDate()));
            } else {
                timestamp.setText("");
            }
        }
    }
}