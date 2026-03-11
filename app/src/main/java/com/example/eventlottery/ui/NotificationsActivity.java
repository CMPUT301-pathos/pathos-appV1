package com.example.eventlottery.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.MainActivity;
import com.example.eventlottery.R;
import com.example.eventlottery.domain.NotificationType;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
/**
 *
 @author : hasrat
 */
public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        rv = findViewById(R.id.rvNotifications);
        bottomNav = findViewById(R.id.bottomNav);

        // RecyclerView setup
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new NotificationCardAdapter(buildDemoNotifications()));

        // Bottom nav setup (matches your mockup)
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_notifications);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_notifications) {
                    return true; // already here
                }
                if (id == R.id.nav_home) {
                    // TODO: replace with your real Home/Dashboard activity
                    startActivity(new Intent(this, MainActivity.class));
                    return true;
                }
                if (id == R.id.nav_events) {
                    Toast.makeText(this, "Scan screen not wired yet", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (id == R.id.nav_profile) {
                    Toast.makeText(this, "Profile screen not wired yet", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * Demo data so the UI matches your mockup right away.
     * Later: replace with Firestore-backed data via NotificationController.
     */
    private List<NotificationCard> buildDemoNotifications() {
        List<NotificationCard> items = new ArrayList<>();

        // WIN example (matches your mockup: shows Accept/Decline)
        items.add(NotificationCard.win("Piano Lessons", "event_piano_001"));
        items.add(NotificationCard.win("Swimming Lessons", "event_swim_002"));

        // Optional LOSE example (no buttons) – keep for later if you implement US 01.04.02
        items.add(NotificationCard.lose("Interpretive Dance", "event_dance_003"));

        return items;
    }

    /** Simple view-model for the card UI. */
    private static class NotificationCard {
        final String eventName;
        final String eventId;
        final NotificationType type;
        final String message;

        private NotificationCard(String eventName, String eventId, NotificationType type, String message) {
            this.eventName = eventName;
            this.eventId = eventId;
            this.type = type;
            this.message = message;
        }

        static NotificationCard win(String eventName, String eventId) {
            return new NotificationCard(eventName, eventId, NotificationType.WIN,
                    "You have won the lottery for this event!");
        }

        // Placeholder for future LOSE type; keeping it as a card without buttons
        static NotificationCard lose(String eventName, String eventId) {
            return new NotificationCard(eventName, eventId, null,
                    "You have not won the lottery for this event.");
        }

        boolean isWin() {
            return type == NotificationType.WIN;
        }
    }

    /**
     * Minimal adapter that binds `NotificationCard` into `item_notification.xml`.
     */
    private class NotificationCardAdapter extends RecyclerView.Adapter<NotificationCardAdapter.VH> {

        private final List<NotificationCard> items;

        NotificationCardAdapter(List<NotificationCard> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_notification, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            NotificationCard item = items.get(position);

            h.tvEventName.setText(item.eventName);
            h.tvMessage.setText(item.message);

            if (item.isWin()) {
                h.winButtonsRow.setVisibility(View.VISIBLE);
                h.btnAccept.setOnClickListener(v -> {
                    Toast.makeText(NotificationsActivity.this,
                            "Accepted: " + item.eventName,
                            Toast.LENGTH_SHORT).show();
                    // TODO: call WaitingListController / RaffleService to accept invite
                });
                h.btnDecline.setOnClickListener(v -> {
                    Toast.makeText(NotificationsActivity.this,
                            "Declined: " + item.eventName,
                            Toast.LENGTH_SHORT).show();
                    // TODO: call PathosRaffleService to decline and draw replacement
                });
            } else {
                h.winButtonsRow.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView tvEventName;
            final TextView tvMessage;
            final LinearLayout winButtonsRow;
            final Button btnAccept;
            final Button btnDecline;

            VH(@NonNull View itemView) {
                super(itemView);
                tvEventName = itemView.findViewById(R.id.tvEventName);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                winButtonsRow = itemView.findViewById(R.id.winButtonsRow);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                btnDecline = itemView.findViewById(R.id.btnDecline);
            }
        }
    }
}
