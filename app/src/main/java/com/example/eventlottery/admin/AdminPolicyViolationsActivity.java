package com.example.eventlottery.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.PolicyViolation;
import com.example.eventlottery.ui.PolicyViolationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminPolicyViolationsActivity extends AppCompatActivity {

    private static final String TAG = "PolicyViolations";

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private TextView tvTotalViolations;
    private PolicyViolationAdapter adapter;
    private FirebaseFirestore db;
    private List<PolicyViolation> violationList;

    /**
     * Entry point for the policy violations activity. Sets up the UI, Firestore, and initial loading.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_policy_violations);

        // Back button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        db = FirebaseFirestore.getInstance();
        violationList = new ArrayList<>();

        initViews();
        setupRecyclerView();

        // First, check ALL violations without filter to debug
        checkAllViolations();
    }

    /**
     * Initializes view references for the policy violations activity.
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewViolations);
        emptyState = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        tvTotalViolations = findViewById(R.id.tvTotalViolations);
    }

    /**
     * Sets up the RecyclerView adapter and layout manager for displaying violations.
     */
    private void setupRecyclerView() {
        adapter = new PolicyViolationAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Reads all policy violations from Firestore for debug logging, then loads only user violations.
     */
    private void checkAllViolations() {
        Log.d(TAG, "=== CHECKING ALL VIOLATIONS IN DATABASE ===");

        db.collection("policy_violations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Total violations in DB: " + querySnapshot.size());

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Log.d(TAG, "Violation ID: " + doc.getId());
                        Log.d(TAG, "  Data: " + doc.getData());
                        Log.d(TAG, "  violationType: " + doc.getString("violationType"));
                        Log.d(TAG, "  userName: " + doc.getString("userName"));
                        Log.d(TAG, "  reason: " + doc.getString("reason"));
                        Log.d(TAG, "---");
                    }

                    // Now load only user violations
                    loadUserViolations();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking violations", e);
                    loadUserViolations();
                });
    }

    /*private void loadUserViolations() {
        progressBar.setVisibility(View.VISIBLE);
        violationList.clear();

        // Load ONLY user violations
        db.collection("policy_violations")
                .whereEqualTo("violationType", "User Violation")
                .orderBy("deletedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    Log.d(TAG, "Found " + querySnapshot.size() + " USER policy violations");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        PolicyViolation violation = new PolicyViolation();
                        violation.setId(doc.getId());
                        violation.setUserId(doc.getString("userId"));
                        violation.setUserName(doc.getString("userName"));
                        violation.setUserEmail(doc.getString("userEmail"));

                        Object deletedAt = doc.get("deletedAt");
                        if (deletedAt instanceof Long) {
                            violation.setDeletedAt((Long) deletedAt);
                        }

                        violation.setDeletedBy(doc.getString("deletedBy"));
                        violation.setReason(doc.getString("reason"));
                        violation.setViolationType(doc.getString("violationType"));
                        violation.setContent(doc.getString("content"));

                        violationList.add(violation);
                        Log.d(TAG, "Loaded user violation: " + violation.getUserName() + " - " + violation.getReason());
                    }

                    updateUI();

                    if (violationList.isEmpty()) {
                        Toast.makeText(this, "No user violations found. Check if deleted users had 'Violated app policy' selected.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading violations", e);
                    Toast.makeText(this, "Error loading violations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }*/
    /**
     * Loads only user-related policy violations and updates the UI with the retrieved list.
     */
    private void loadUserViolations() {
        progressBar.setVisibility(View.VISIBLE);
        violationList.clear();

        // Load ONLY user violations WITHOUT orderBy to avoid index requirement
        db.collection("policy_violations")
                .whereEqualTo("violationType", "User Violation")
                // .orderBy("deletedAt", Query.Direction.DESCENDING)  // COMMENT OUT TEMPORARILY
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    Log.d(TAG, "Found " + querySnapshot.size() + " USER policy violations");

                    // Manually sort in memory
                    List<QueryDocumentSnapshot> docs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        docs.add(doc);
                    }
                    docs.sort((a, b) -> {
                        Long aTime = a.getLong("deletedAt");
                        Long bTime = b.getLong("deletedAt");
                        if (aTime == null) aTime = 0L;
                        if (bTime == null) bTime = 0L;
                        return bTime.compareTo(aTime); // Descending
                    });

                    for (QueryDocumentSnapshot doc : docs) {
                        PolicyViolation violation = new PolicyViolation();
                        violation.setId(doc.getId());
                        violation.setUserId(doc.getString("userId"));
                        violation.setUserName(doc.getString("userName"));
                        violation.setUserEmail(doc.getString("userEmail"));

                        Object deletedAt = doc.get("deletedAt");
                        if (deletedAt instanceof Long) {
                            violation.setDeletedAt((Long) deletedAt);
                        }

                        violation.setDeletedBy(doc.getString("deletedBy"));
                        violation.setReason(doc.getString("reason"));
                        violation.setViolationType(doc.getString("violationType"));
                        violation.setContent(doc.getString("content"));

                        violationList.add(violation);
                        Log.d(TAG, "Loaded user violation: " + violation.getUserName() + " - " + violation.getReason());
                    }

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading violations", e);
                    Toast.makeText(this, "Error loading violations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    /**
     * Refreshes the visible UI based on whether policy violations have been loaded.
     */
    private void updateUI() {
        if (violationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            tvTotalViolations.setText("Total: 0 violations");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            tvTotalViolations.setText("Total: " + violationList.size() + " violations");
            adapter.setViolations(violationList);
        }
    }

    /**
     * Reloads user policy violations each time the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserViolations();
    }

    /**
     * Handles the toolbar up button by finishing the activity.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}