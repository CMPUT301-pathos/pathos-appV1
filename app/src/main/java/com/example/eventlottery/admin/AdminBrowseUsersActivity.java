package com.example.eventlottery.admin;

/**
 * Browse/delete users (US 03.05.01, 03.02.01)
 * @author hasratsinghchauhan
 *  * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 */
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.ui.AdminUserAdapter;
import com.example.eventlottery.domain.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin activity for browsing and managing user profiles.
 * Supports:
 * - View all users
 * - Search by name/email
 * - Delete user profiles (US 03.02.01)
 * - Remove organizer role (US 03.07.01)
 */
public class AdminBrowseUsersActivity extends AppCompatActivity {
    private EditText etSearch;
    private RecyclerView recyclerViewUsers;
    private LinearLayout emptyStateLayout;
    private AdminUserAdapter userAdapter;
    private FirebaseFirestore db;
    private List<UserProfile> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_users);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Browse Users");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        initViews();
        setupSearch();
        setupRecyclerView();
        loadUsers();
    }

    private void initViews() {
        etSearch = findViewById(R.id.searchUsers);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        userAdapter = new AdminUserAdapter();
        userAdapter.setOnUserClickListener(new AdminUserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(UserProfile user) {
                showUserDetails(user);
            }

            @Override
            public void onDeleteClick(UserProfile user) {
                showDeleteConfirmation(user);
            }

            @Override
            public void onRemoveOrganizerClick(UserProfile user) {
                showRemoveOrganizerConfirmation(user);
            }
        });

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserProfile user = doc.toObject(UserProfile.class);
                        // Set the document ID as deviceId if needed
                        if (user.getDeviceId() == null) {
                            user.setDeviceId(doc.getId());
                        }
                        userList.add(user);
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading users: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void showUserDetails(UserProfile user) {
        String details = "Name: " + user.getName() + "\n" +
                "Email: " + user.getEmail() + "\n" +
                "Phone: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not provided") + "\n" +
                "Role: " + user.getRole() + "\n" +
                "Device ID: " + user.getDeviceId() + "\n" +
                "Notifications: " + (user.isNotificationsEnabled() ? "Enabled" : "Disabled") + "\n" +
                "Events Joined: " + (user.getEventHistory() != null ? user.getEventHistory().size() : 0);

        new AlertDialog.Builder(this)
                .setTitle("User Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteConfirmation(UserProfile user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Delete user \"" + user.getName() + "\"?\n\nThis cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(UserProfile user) {
        db.collection("users")
                .document(user.getDeviceId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    userList.remove(user);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showRemoveOrganizerConfirmation(UserProfile user) {
        if (!"organizer".equals(user.getRole())) {
            Toast.makeText(this, "User is not an organizer", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove Organizer")
                .setMessage("Remove organizer role from \"" + user.getName() + "\"?\n\n" +
                        "This will NOT delete their events. Use with caution.")
                .setPositiveButton("Remove", (dialog, which) -> removeOrganizerRole(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeOrganizerRole(UserProfile user) {
        db.collection("users")
                .document(user.getDeviceId())
                .update("role", "entrant")  // Demote to entrant
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Organizer role removed", Toast.LENGTH_SHORT).show();
                    user.setRole("entrant");
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (userList.isEmpty()) {
            recyclerViewUsers.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewUsers.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            userAdapter.setUsers(userList);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
