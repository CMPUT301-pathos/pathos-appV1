package com.example.eventlottery.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * @author hasratsinghchauhan
 * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private List<UserProfile> users = new ArrayList<>();
    private List<UserProfile> usersFull = new ArrayList<>();
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserProfile user);
        void onDeleteClick(UserProfile user);
        void onRemoveOrganizerClick(UserProfile user);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<UserProfile> users) {
        this.users = users;
        this.usersFull = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        users.clear();
        if (query.isEmpty()) {
            users.addAll(usersFull);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (UserProfile user : usersFull) {
                if (user.getName().toLowerCase().contains(lowerQuery) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery))) {
                    users.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvEmail, tvRole;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(users.get(getAdapterPosition()));
                }
            });

            itemView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(users.get(getAdapterPosition()));
                }
            });

            itemView.findViewById(R.id.btnRemoveOrganizer).setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveOrganizerClick(users.get(getAdapterPosition()));
                }
            });
        }

        void bind(UserProfile user) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
            tvRole.setText(user.getRole() != null ? user.getRole() : "entrant");
        }
    }
}