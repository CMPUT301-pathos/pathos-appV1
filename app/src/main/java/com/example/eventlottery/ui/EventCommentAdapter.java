package com.example.eventlottery.ui;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.EventComment;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.firebase.FirestoreProfileRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventCommentAdapter extends RecyclerView.Adapter<EventCommentAdapter.CommentViewHolder> {

    private List<EventComment> comments = new ArrayList<>();
    private OnCommentClickListener clickListener;
    private boolean isOrganizer = false;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public interface OnCommentClickListener {
        void onCommentClick(EventComment comment);
        void onCommentLongClick(EventComment comment);
        void onDeleteClick(EventComment comment);
    }

    public EventCommentAdapter() {}

    public void setOrganizerMode(boolean isOrganizer) {
        this.isOrganizer = isOrganizer;
    }

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
        ImageView authorPhoto;
        TextView authorName;
        TextView text;
        TextView timestamp;
        TextView btnDelete;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorPhoto = itemView.findViewById(R.id.ivCommentAuthorPhoto);
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

            authorPhoto.setImageResource(R.drawable.ic_profile_placeholder_forstyledlayout);
            String userId = comment.getUserId();
            if (userId != null && !userId.isEmpty()) {
                new FirestoreProfileRepository().getProfile(userId,
                        new ProfileRepository.ProfileCallback() {
                            @Override
                            public void onSuccess(UserProfile profile) {
                                if (profile == null) return;
                                String photoUri = profile.getProfilePhotoUri();
                                if (photoUri != null && !photoUri.isEmpty()) {
                                    try {
                                        if (photoUri.startsWith("data:image")) {
                                            String base64 = photoUri.substring(photoUri.indexOf(",") + 1);
                                            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                            android.graphics.Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                            authorPhoto.setImageBitmap(bitmap);
                                        }
                                    } catch (Exception e) {
                                        authorPhoto.setImageResource(R.drawable.ic_profile_placeholder_forstyledlayout);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                            }
                        });
            }

            if (btnDelete != null) {
                btnDelete.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
            }

            if (comment.getTimestamp() != null) {
                timestamp.setText(DATE_FORMAT.format(comment.getTimestamp().toDate()));
            } else {
                timestamp.setText("");
            }
        }
    }
}