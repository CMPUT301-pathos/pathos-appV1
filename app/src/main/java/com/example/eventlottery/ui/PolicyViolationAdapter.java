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
        private TextView tvUserName, tvUserEmail, tvDate, tvReason, tvDeletedBy;
        private CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvDeletedBy = itemView.findViewById(R.id.tvDeletedBy);
            cardView = (CardView) itemView;
        }

        void bind(PolicyViolation violation) {
            tvUserName.setText(violation.getUserName());
            tvUserEmail.setText(violation.getUserEmail());
            tvDate.setText(dateFormat.format(new Date(violation.getDeletedAt())));
            tvReason.setText("Reason: " + violation.getReason());
            tvDeletedBy.setText("Deleted by: " + violation.getDeletedBy());
        }
    }
}
