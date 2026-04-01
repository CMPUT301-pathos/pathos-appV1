package com.example.eventlottery.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.domain.EventComment;
import com.example.eventlottery.domain.UserProfile;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

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
 * - US 02.08.01: Organizer can delete entrant comments
 *
 * @author Edwin David
 * @version 1.2
 */
public class EventCommentAdapter extends RecyclerView.Adapter<EventCommentAdapter.VH> {

    /** Callback invoked when the organizer taps Delete on a comment. */
    public interface OnDeleteListener {
        void onDelete(EventComment comment);
    }

    private final List<EventComment> comments;
    private OnDeleteListener deleteListener;
    private ProfileRepository profileRepository;
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public EventCommentAdapter(List<EventComment> comments) {
        this.comments = comments;
    }

    /** Set to enable delete buttons. Pass null to hide them. */
    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    /** Set to enable profile picture loading per comment author. */
    public void setProfileRepository(ProfileRepository repository) {
        this.profileRepository = repository;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EventComment c = comments.get(position);
        h.authorName.setText(c.getAuthorName());
        h.text.setText(c.getText());
        h.timestamp.setText(DATE_FORMAT.format(new Date(c.getCreatedAt())));

        // Reset avatar to placeholder before async load
        h.avatar.setImageResource(R.drawable.ic_profile_placeholder_forstyledlayout);

        if (profileRepository != null && c.getDeviceId() != null) {
            profileRepository.getProfile(c.getDeviceId(), new ProfileRepository.ProfileCallback() {
                @Override
                public void onSuccess(UserProfile profile) {
                    if (profile == null) return;
                    String photoUri = profile.getProfilePhotoUri();
                    if (photoUri != null && photoUri.startsWith("data:image")) {
                        try {
                            String base64 = photoUri.substring(photoUri.indexOf(",") + 1);
                            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            h.avatar.setImageBitmap(bitmap);
                        } catch (Exception ignored) {}
                    }
                }
                @Override
                public void onFailure(Exception e) {}
            });
        }

        // Show delete button only in organizer context
        if (deleteListener != null) {
            h.btnDelete.setVisibility(View.VISIBLE);
            h.btnDelete.setOnClickListener(v -> deleteListener.onDelete(c));
        } else {
            h.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return comments.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView authorName, text, timestamp;
        final MaterialButton btnDelete;
        final CircleImageView avatar;

        VH(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.text_comment_author);
            text = itemView.findViewById(R.id.text_comment_body);
            timestamp = itemView.findViewById(R.id.text_comment_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_comment);
            avatar = itemView.findViewById(R.id.img_comment_avatar);
        }
    }
}
