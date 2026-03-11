//package com.example.eventlottery;

//import android.os.Bundle;

//import androidx.appcompat.app.AppCompatActivity;

/**
 * @author hasratsinghchauhan
 *  * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 * @version: 1.0
 * @see: activity_admin_main.xml
 */
/**public class AdminMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
    }
}*/
package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.eventlottery.admin.AdminBrowseImages;
import android.content.Intent;
import com.example.eventlottery.admin.AdminBrowseEventsActivity;
import com.example.eventlottery.admin.AdminBrowseUsersActivity;
import com.example.eventlottery.admin.AdminNotificationLogs;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Admin Main Dashboard Activity
 * This activity serves as the central hub for all administrative functions in the application.
 * It provides a dashboard with platform statistics and navigation to various admin management screens.
 * Administrators can monitor key metrics and access tools for managing events, users, and content.
 *
 * Features include:
 *     Platform statistics display (total events, users, organizers)
 *     Navigation to event browsing and management (US 03.04.01)
 *     Navigation to user browsing and management (US 03.05.01)
 *     Placeholder for image management functionality (US 03.06.01)
 *     Real-time statistics loading from Firebase
 *User Stories Supported:
 *     US 03.04.01 - As an administrator, I want to be able to browse events
 *     US 03.05.01 - As an administrator, I want to be able to browse profiles
 *     US 03.06.01 - As an administrator, I want to be able to browse images
 * @author hasratsinghchauhan
 * @version 1.0
 * @see AdminBrowseEventsActivity
 * @see AdminBrowseUsersActivity
 * @see R.layout#activity_admin_main
 */
public class AdminMainActivity extends AppCompatActivity {

    private TextView tvEventsCount, tvUsersCount, tvOrganizersCount;
    private CardView cardBrowseEvents, cardBrowseUsers, cardBrowseImages;
    private FirebaseFirestore db;
    private CardView cardNotificationLogs;  // ADD THIS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);  // This layout already exists!

        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        loadStatistics();
    }
    /**
     * Initializes all view references used in the dashboard.
     * Binds the layout elements to their corresponding member variables.
     */
    private void initViews() {
        tvEventsCount = findViewById(R.id.tvEventsCount);
        tvUsersCount = findViewById(R.id.tvUsersCount);
        tvOrganizersCount = findViewById(R.id.tvOrganizersCount);
        cardBrowseEvents = findViewById(R.id.cardBrowseEvents);
        cardBrowseUsers = findViewById(R.id.cardBrowseUsers);
        cardBrowseImages = findViewById(R.id.cardBrowseImages);
        cardNotificationLogs = findViewById(R.id.cardNotificationLogs);
    }
    /**
     * Configures click listeners for all navigation cards.
     * Each card launches its corresponding admin management activity.
     */
    private void setupClickListeners() {
        cardBrowseEvents.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseEventsActivity.class)));

        cardBrowseUsers.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseUsersActivity.class)));

        cardBrowseImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseImages.class)));
        cardNotificationLogs.setOnClickListener(v ->
                startActivity(new Intent(this, AdminNotificationLogs.class)));
    }
    /**
     * Loads and displays platform statistics from Firebase.
     * Fetches and displays:
     *
     *    Total number of events in the system
     *    Total number of user profiles
     *     Total number of users with organizer role
     *
     * Each query runs independently to prevent one failure from affecting others.
     */
    private void loadStatistics() {
        // Count events
        db.collection("events")
                .get()
                .addOnSuccessListener(query ->
                        tvEventsCount.setText(String.valueOf(query.size())));

        // Count users
        db.collection("users")
                .get()
                .addOnSuccessListener(query ->
                        tvUsersCount.setText(String.valueOf(query.size())));

        // Count organizers
        db.collection("users")
                .whereEqualTo("role", "organizer")
                .get()
                .addOnSuccessListener(query ->
                        tvOrganizersCount.setText(String.valueOf(query.size())));
    }

    /**
     * Handles the Up navigation button in the action bar.
     * Closes the current activity and returns to the previous screen.
     *
     * @return true to indicate the navigation was handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Refreshes statistics when the activity resumes.
     * Ensures the dashboard always shows current data when returning to it.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

}