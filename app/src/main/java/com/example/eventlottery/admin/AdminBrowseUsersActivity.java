package com.example.eventlottery.admin;

/**
 * Browse/delete users (US 03.05.01, 03.02.01)
 * @author hasratsinghchauhan
 * * P.S do not change the contents of the file w/o informing/collaboratng (with) the author.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.PolicyViolation;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.ui.AdminUserAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Activity entry point. Initializes UI, Firestore, and loads the user list.
     */
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
    /**
     * Records a policy violation for a user in Firestore.
     * This is used when deleting or disciplining a user for policy breaches.
     */
    private void savePolicyViolationRecord(UserProfile user, String reason) {
        String adminName = "Admin"; // You might want to get actual admin name

        PolicyViolation violation = new PolicyViolation(
                user.getDeviceId(),
                user.getName(),
                user.getEmail(),
                adminName,
                reason
        );

        // Save to Firestore
        db.collection("policy_violations")
                .add(violation)
                .addOnSuccessListener(docRef -> {
                    Log.d("AdminUsers", "✅ Policy violation saved with ID: " + docRef.getId());
                    Toast.makeText(this, "Policy violation recorded", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminUsers", "❌ Error saving policy violation", e);
                });
    }

    /**
     * Finds and caches the main view references used by this activity.
     */
    private void initViews() {
        etSearch = findViewById(R.id.searchUsers);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    /**
     * Sets up the search input to filter users as the text changes.
     */
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

    /**
     * Configures the RecyclerView and binds user interaction callbacks.
     */
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

    /**
     * Loads all user profiles from Firestore and refreshes the user list.
     */
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

    /**
     * Displays a dialog with user profile details.
     */
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

    /*private void showDeleteConfirmation(UserProfile user) {
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
    }*/
 /*private void showDeleteConfirmation(UserProfile user) {
 // Options for deletion reasons
 String[] options = {
 "Violated app policy",
 "Inactive account",
 "Duplicate account",
 "User requested",
 "Other reason"
 };

 new AlertDialog.Builder(this)
 .setTitle("Delete User")
 .setMessage("Delete user \"" + user.getName() + "\"?")
 .setItems(options, (dialog, which) -> {
 String reason = options[which];
 deleteUser(user, reason);
 })
 .setNegativeButton("Cancel", null)
 .show();
 }
*/
    /**
     * Shows a delete confirmation dialog with selectable reasons.
     */
    private void showDeleteConfirmation(UserProfile user) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_user, null);

        TextView tvUserName = dialogView.findViewById(R.id.tvUserName);
        tvUserName.setText(user.getName());

        // Wrap the content in a ScrollView programmatically
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(dialogView);

        // Get all checkboxes
        CheckBox reasonPolicy = dialogView.findViewById(R.id.reasonPolicy);
        CheckBox reasonInactive = dialogView.findViewById(R.id.reasonInactive);
        CheckBox reasonDuplicate = dialogView.findViewById(R.id.reasonDuplicate);
        CheckBox reasonRequested = dialogView.findViewById(R.id.reasonRequested);
        CheckBox reasonOther = dialogView.findViewById(R.id.reasonOther);

        // Make checkboxes mutually exclusive
        View.OnClickListener checkboxListener = v -> {
            CheckBox clicked = (CheckBox) v;
            if (clicked.isChecked()) {
                reasonPolicy.setChecked(clicked == reasonPolicy);
                reasonInactive.setChecked(clicked == reasonInactive);
                reasonDuplicate.setChecked(clicked == reasonDuplicate);
                reasonRequested.setChecked(clicked == reasonRequested);
                reasonOther.setChecked(clicked == reasonOther);
            }
        };

        reasonPolicy.setOnClickListener(checkboxListener);
        reasonInactive.setOnClickListener(checkboxListener);
        reasonDuplicate.setOnClickListener(checkboxListener);
        reasonRequested.setOnClickListener(checkboxListener);
        reasonOther.setOnClickListener(checkboxListener);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete User")
                .setView(scrollView) // Use scrollView instead of dialogView
                .setPositiveButton("Delete", (dialog, which) -> {
                    String reason = "No reason specified";

                    if (reasonPolicy.isChecked()) {
                        reason = "Violated app policy";
                    } else if (reasonInactive.isChecked()) {
                        reason = "Inactive account";
                    } else if (reasonDuplicate.isChecked()) {
                        reason = "Duplicate account";
                    } else if (reasonRequested.isChecked()) {
                        reason = "User requested deletion";
                    } else if (reasonOther.isChecked()) {
                        reason = "Other reason";
                    }

                    deleteUser(user, reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    /*private void deleteUser(UserProfile user, String reason) {
    boolean isPolicyViolation = reason.equals("Violated app policy");

    // Delete from Firestore
    db.collection("users")
    .document(user.getDeviceId())
    .delete()
    .addOnSuccessListener(aVoid -> {
    // If policy violation, increment counter
    if (isPolicyViolation) {
    incrementPolicyViolationCount();
    }

    Toast.makeText(this, "User deleted: " + reason, Toast.LENGTH_SHORT).show();
    userList.remove(user);
    updateUI();
    })
    .addOnFailureListener(e -> {
    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    });
    }*/
 /*private void deleteUser(UserProfile user, String reason) {
 boolean isPolicyViolation = reason.equals("Violated app policy");

 // Show loading
 ProgressDialog progressDialog = new ProgressDialog(this);
 progressDialog.setMessage("Deleting user...");
 progressDialog.show();

 // Delete from Firestore
 db.collection("users")
 .document(user.getDeviceId())
 .delete()
 .addOnSuccessListener(aVoid -> {
 progressDialog.dismiss();

 // If policy violation, increment counter
 if (isPolicyViolation) {
 incrementPolicyViolationCount();
 Toast.makeText(this, " User deleted for policy violation", Toast.LENGTH_LONG).show();
 } else {
 Toast.makeText(this, "User deleted: " + reason, Toast.LENGTH_SHORT).show();
 }

 userList.remove(user);
 updateUI();
 })
 .addOnFailureListener(e -> {
 progressDialog.dismiss();
 Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
 });
 }*/
 /* private void deleteUser(UserProfile user, String reason) {
 boolean isPolicyViolation = reason.equals("Violated app policy");

 // Show loading
 ProgressDialog progressDialog = new ProgressDialog(this);
 progressDialog.setMessage("Deleting user...");
 progressDialog.setCancelable(false);
 progressDialog.show();

 // Delete from Firestore
 db.collection("users")
 .document(user.getDeviceId())
 .delete()
 .addOnSuccessListener(aVoid -> {
 progressDialog.dismiss();

 // If policy violation, increment counter
 if (isPolicyViolation) {
 incrementPolicyViolationCount();
 Toast.makeText(this, " User deleted for policy violation", Toast.LENGTH_LONG).show();
 } else {
 Toast.makeText(this, "User deleted: " + reason, Toast.LENGTH_SHORT).show();
 }

 userList.remove(user);
 updateUI();
 })
 .addOnFailureListener(e -> {
 progressDialog.dismiss();
 Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
 });
 }
*/
   /* private void deleteUser(UserProfile user, String reason) {
        boolean isPolicyViolation = reason.equals("Violated app policy");

        // If policy violation, SAVE TO FIRESTORE FIRST
        if (isPolicyViolation) {
            Map<String, Object> violation = new HashMap<>();
            violation.put("userId", user.getDeviceId());
            violation.put("userName", user.getName());
            violation.put("userEmail", user.getEmail());
            violation.put("deletedAt", System.currentTimeMillis());
            violation.put("reason", reason);

            // Save to Firestore
            db.collection("policy_violations")
                    .add(violation)
                    .addOnSuccessListener(docRef -> {
                        Log.d("Admin", " Violation saved: " + user.getName());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Admin", " Failed to save violation", e);
                    });
        }

        // Then delete the user
        db.collection("users")
                .document(user.getDeviceId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (isPolicyViolation) {
                        incrementPolicyViolationCount();
                        Toast.makeText(this, " User deleted for policy violation", Toast.LENGTH_LONG).show();
                    }
                    userList.remove(user);
                    updateUI();
                });
    }*/
    /**
     * Deletes a user and optionally logs a policy violation record.
     */
    private void deleteUser(UserProfile user, String reason) {
        boolean isPolicyViolation = reason.equals("Violated app policy");

        // If policy violation, SAVE TO FIRESTORE FIRST with ALL required fields
        if (isPolicyViolation) {
            Map<String, Object> violation = new HashMap<>();
            violation.put("userId", user.getDeviceId());
            violation.put("userName", user.getName());
            violation.put("userEmail", user.getEmail());
            violation.put("deletedAt", System.currentTimeMillis());
            violation.put("deletedBy", "Admin");
            violation.put("reason", reason);
            violation.put("violationType", "User Violation");  // CRITICAL: This is needed for filtering
            violation.put("content", "User account deleted for: " + reason);

            // Save to Firestore
            db.collection("policy_violations")
                    .add(violation)
                    .addOnSuccessListener(docRef -> {
                        Log.d("Admin", "Violation saved with ID: " + docRef.getId());
                        // After saving violation, delete the user
                        performUserDeletion(user, isPolicyViolation);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Admin", "Failed to save violation", e);
                        // Still try to delete the user
                        performUserDeletion(user, isPolicyViolation);
                    });
        } else {
            // Not a policy violation, just delete
            performUserDeletion(user, isPolicyViolation);
        }
    }

    /**
     * Deletes the user document from Firestore and updates the UI.
     */
    private void performUserDeletion(UserProfile user, boolean isPolicyViolation) {
        db.collection("users")
                .document(user.getDeviceId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (isPolicyViolation) {
                        Toast.makeText(this, "User deleted for policy violation", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    }
                    userList.remove(user);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Increments the local policy violation counter in shared preferences.
     */
    private void incrementPolicyViolationCount() {
        try {
            // Use MODE_PRIVATE to ensure proper access
            SharedPreferences prefs = getSharedPreferences("AdminStats", MODE_PRIVATE);
            int currentCount = prefs.getInt("policyViolations", 0);
            int newCount = currentCount + 1;

            // Use edit() and apply() properly
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("policyViolations", newCount);
            editor.apply(); // apply() is asynchronous but usually fine

            // Log for debugging
            Log.d("AdminUsers", " Policy violation incremented: " + currentCount + " → " + newCount);

            // Optional: Show toast for confirmation
            Toast.makeText(this, "Policy violation recorded! Total: " + newCount, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("AdminUsers", "Error incrementing policy count", e);
        }
    }

    /**
     * Prompts the admin to confirm removal of organizer privileges from a user.
     */
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

    /**
     * Demotes the selected organizer to an entrant role in Firestore.
     */
    private void removeOrganizerRole(UserProfile user) {
        db.collection("users")
                .document(user.getDeviceId())
                .update("role", "entrant") // Demote to entrant
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Organizer role removed", Toast.LENGTH_SHORT).show();
                    user.setRole("entrant");
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Refreshes the UI based on whether there are users to display.
     */
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

    /**
     * Handles action bar navigation up by closing the activity.
     * @return true when navigation is handled.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
