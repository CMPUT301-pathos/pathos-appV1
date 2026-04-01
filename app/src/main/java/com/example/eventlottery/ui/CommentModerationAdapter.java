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

public class CommentModerationAdapter extends RecyclerView.Adapter<CommentModerationAdapter.CommentViewHolder> {

    private List<EventComment> comments = new ArrayList<>();
    private OnCommentDeleteListener deleteListener;

    public interface OnCommentDeleteListener {
        void onDeleteClick(EventComment comment);
    }

    public void setOnCommentDeleteListener(OnCommentDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setComments(List<EventComment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment_moderation, parent, false);
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
        private TextView tvContent;
        private TextView tvAuthor;
        private TextView tvTimestamp;
        private TextView btnDelete;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvTimestamp = itemView.findViewById(R.id.tvCommentTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }

        void bind(EventComment comment) {
            tvContent.setText(comment.getContent());
            tvAuthor.setText("By: " + comment.getUserName());

            if (comment.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                tvTimestamp.setText(sdf.format(comment.getTimestamp().toDate()));
            } else {
                tvTimestamp.setText("Unknown date");
            }

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(comment);
                }
            });
        }
    }
}
