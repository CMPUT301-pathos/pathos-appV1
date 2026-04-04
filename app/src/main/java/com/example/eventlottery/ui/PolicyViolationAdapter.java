package com.example.eventlottery.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.domain.PolicyViolation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PolicyViolationAdapter extends RecyclerView.Adapter<PolicyViolationAdapter.ViewHolder> {

    private List<PolicyViolation> violations = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public void setViolations(List<PolicyViolation> violations) {
        this.violations = violations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_policy_violation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PolicyViolation violation = violations.get(position);
        holder.bind(violation);
    }

    @Override
    public int getItemCount() {
        return violations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvUserEmail, tvReason, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        void bind(PolicyViolation violation) {
            // User Name
            if (violation.getUserName() != null && !violation.getUserName().isEmpty()) {
                tvUserName.setText(violation.getUserName());
            } else {
                tvUserName.setText("Unknown User");
            }

            // User Email
            if (violation.getUserEmail() != null && !violation.getUserEmail().isEmpty()) {
                tvUserEmail.setText(violation.getUserEmail());
            } else {
                tvUserEmail.setText("Email not provided");
            }

            // Reason
            if (violation.getReason() != null && !violation.getReason().isEmpty()) {
                tvReason.setText("Reason: " + violation.getReason());
            } else {
                tvReason.setText("Reason: Not specified");
            }

            // Date
            if (violation.getDeletedAt() > 0) {
                tvDate.setText(dateFormat.format(new Date(violation.getDeletedAt())));
            } else {
                tvDate.setText("Date unknown");
            }
        }
    }
}