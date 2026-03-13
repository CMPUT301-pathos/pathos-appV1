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

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private TextView tvTotalViolations;
    private PolicyViolationAdapter adapter;
    private FirebaseFirestore db;
    private List<PolicyViolation> violationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_policy_violations);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        violationList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadViolations();
        checkFirestoreData();

    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewViolations);
        emptyState = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        tvTotalViolations = findViewById(R.id.tvTotalViolations);
    }

    private void setupRecyclerView() {
        adapter = new PolicyViolationAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /*private void loadViolations() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("policy_violations")
                .orderBy("deletedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    violationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        PolicyViolation violation = doc.toObject(PolicyViolation.class);
                        violationList.add(violation);
                        Log.d("Policy", "Loaded: " + violation.getUserName()); // Debug log
                    }
                    updateUI();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading violations", Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }*/
    private void loadViolations() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("policy_violations")
                .orderBy("deletedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    violationList.clear();

                    Log.d("PolicyViolations", "Found " + querySnapshot.size() + " documents");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        PolicyViolation violation = doc.toObject(PolicyViolation.class);
                        violationList.add(violation);
                        Log.d("PolicyViolations", "Loaded: " + violation.getUserName());
                    }

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("PolicyViolations", "Error loading", e);
                    Toast.makeText(this, "Error loading violations", Toast.LENGTH_SHORT).show();
                });
    }

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
    private void checkFirestoreData() {
        db.collection("policy_violations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("PolicyViolations", "Found " + querySnapshot.size() + " documents");
                    if (querySnapshot.size() > 0) {
                        // Force update UI
                        violationList.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            violationList.add(doc.toObject(PolicyViolation.class));
                        }
                        updateUI();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}