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
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public interface OnCommentDeleteListener {
        void onDeleteClick(EventComment comment);
    }

    /**
     * Sets the listener for delete actions on moderated comments.
     *
     * @param listener listener invoked when a comment delete action occurs
     */
    public void setOnCommentDeleteListener(OnCommentDeleteListener listener) {
        this.deleteListener = listener;
    }

    /**
     * Updates the list of comments displayed by the adapter.
     *
     * @param comments new list of event comments
     */
    public void setComments(List<EventComment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    /**
     * Creates a view holder for a comment moderation row.
     *
     * @param parent parent view group
     * @param viewType row view type
     * @return new CommentViewHolder
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment_moderation, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Binds a comment payload into the moderation row.
     *
     * @param holder holder displaying the comment
     * @param position comment position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        EventComment comment = comments.get(position);
        holder.bind(comment);
    }

    /**
     * Returns the count of comments shown.
     *
     * @return number of comment rows
     */
    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvContent;
        private TextView tvAuthor;
        private TextView tvTimestamp;
        private TextView btnDelete;

        /**
         * Constructs the view holder for a moderation comment item.
         *
         * @param itemView inflated comment item view
         */
        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvTimestamp = itemView.findViewById(R.id.tvCommentTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }

        /**
         * Binds the comment details into the moderation item views.
         *
         * @param comment comment data to display
         */
        void bind(EventComment comment) {
            // Display comment content
            tvContent.setText(comment.getContent() != null ? comment.getContent() : "No content");
            
            // Display author info
            String author = comment.getUserName() != null ? comment.getUserName() : "Unknown";
            String userId = comment.getUserId() != null ? comment.getUserId() : "Unknown ID";
            tvAuthor.setText("By: " + author + " (ID: " + userId + ")");
            
            // Display timestamp
            if (comment.getTimestamp() != null) {
                tvTimestamp.setText(DATE_FORMAT.format(comment.getTimestamp().toDate()));
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
