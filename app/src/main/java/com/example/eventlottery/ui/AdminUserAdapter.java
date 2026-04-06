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
        /**
         * Called when a user item is selected.
         *
         * @param user selected user profile
         */
        void onUserClick(UserProfile user);

        /**
         * Called when the delete button on a user item is pressed.
         *
         * @param user user profile to delete
         */
        void onDeleteClick(UserProfile user);

        /**
         * Called when the remove organizer action is pressed.
         *
         * @param user user profile to demote from organizer
         */
        void onRemoveOrganizerClick(UserProfile user);
    }

    /**
     * Sets the listener that receives user item events.
     *
     * @param listener listener handling clicks and organizer removals
     */
    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    /**
     * Inflates the user item view.
     *
     * @param parent containing view group
     * @param viewType view type of the row
     * @return a new ViewHolder for a user item
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a UserProfile to the view holder.
     *
     * @param holder holder displaying the row
     * @param position position of the user in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile user = users.get(position);
        holder.bind(user);
    }

    /**
     * Returns the current number of user rows.
     *
     * @return item count
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Replaces the current user list and preserves a full copy for filtering.
     *
     * @param users new list of user profiles
     */
    public void setUsers(List<UserProfile> users) {
        this.users = users;
        this.usersFull = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    /**
     * Filters the user list by name or email.
     *
     * @param query search text to filter users
     */
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

        /**
         * Constructs a view holder for the admin user row.
         *
         * @param itemView inflated user item view
         */
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

        /**
         * Binds the user profile fields into the row views.
         *
         * @param user the user profile to display
         */
        void bind(UserProfile user) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
            tvRole.setText(user.getRole() != null ? user.getRole() : "entrant");
        }
    }
}