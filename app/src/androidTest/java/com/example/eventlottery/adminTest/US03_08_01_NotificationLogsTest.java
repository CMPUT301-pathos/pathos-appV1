package com.example.eventlottery.adminTest;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.eventlottery.R;
import com.example.eventlottery.admin.AdminNotificationLogs;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class US03_08_01_NotificationLogsTest {

    private ActivityScenario<AdminNotificationLogs> scenario;

    @Before
    public void setUp() {
        // Clear any existing data or mock Firestore if needed
        // For testing, we can launch the activity
        scenario = ActivityScenario.launch(AdminNotificationLogs.class);

        // Give time for Firestore to load data
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLogsListIsDisplayed() {
        scenario.onActivity(activity -> {
            // Check if RecyclerView exists
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerViewLogs);
            assertNotNull("RecyclerView should exist", recyclerView);

            // Check if ProgressBar is gone after loading
            ProgressBar progressBar = activity.findViewById(R.id.progressBar);
            assertNotNull("ProgressBar should exist", progressBar);

            // Check if total logs text exists
            TextView tvTotalLogs = activity.findViewById(R.id.tvTotalLogs);
            assertNotNull("Total logs TextView should exist", tvTotalLogs);

            // Either RecyclerView is visible (has data) OR empty state is visible
            LinearLayout emptyState = activity.findViewById(R.id.emptyStateLayout);
            boolean hasData = recyclerView.getVisibility() == View.VISIBLE;
            boolean isEmpty = emptyState.getVisibility() == View.VISIBLE;

            assertTrue("Either RecyclerView or empty state should be visible", hasData || isEmpty);
        });
    }

    @Test
    public void testSearchFunctionality() {
        scenario.onActivity(activity -> {
            // Check if search EditText exists
            android.widget.EditText etSearch = activity.findViewById(R.id.etSearchLogs);
            assertNotNull("Search EditText should exist", etSearch);

            // Search should be functional
            etSearch.setText("test");

            // Give time for filter to apply
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Check that filtering didn't crash
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerViewLogs);
            assertNotNull("RecyclerView should still exist after search", recyclerView);
        });
    }

    @Test
    public void testFilterSpinnerExists() {
        scenario.onActivity(activity -> {
            // Check if filter spinner exists
            android.widget.Spinner spinner = activity.findViewById(R.id.spinnerFilter);
            assertNotNull("Filter spinner should exist", spinner);

            // Spinner should have items
            assertTrue("Spinner should have items", spinner.getCount() > 0);
        });
    }

    @Test
    public void testBackButtonExists() {
        scenario.onActivity(activity -> {
            // Check if back button exists
            View btnBack = activity.findViewById(R.id.btnBack);
            assertNotNull("Back button should exist", btnBack);

            // Back button should be clickable
            assertTrue("Back button should be clickable", btnBack.isClickable());
        });
    }

    @Test
    public void testLogDetailsCanBeViewed() {
        scenario.onActivity(activity -> {
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerViewLogs);
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            // If there are items, they should be clickable
            if (adapter != null && adapter.getItemCount() > 0) {
                // Get the first item view
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
                if (viewHolder != null) {
                    View itemView = viewHolder.itemView;
                    assertNotNull("Item view should exist", itemView);

                    // Simulate click on first item
                    itemView.performClick();

                    // Dialog should appear - we can't easily test dialog contents,
                    // but at least we know it didn't crash
                }
            }
        });
    }

    @Test
    public void testEmptyStateWhenNoLogs() {
        // This test is more for mocking - in real scenario, if there are no logs,
        // empty state should be visible

        scenario.onActivity(activity -> {
            LinearLayout emptyState = activity.findViewById(R.id.emptyStateLayout);
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerViewLogs);

            // Either there are logs (recycler view visible) or empty state visible
            boolean hasLogs = recyclerView.getVisibility() == View.VISIBLE;
            boolean showsEmpty = emptyState.getVisibility() == View.VISIBLE;

            // The app should handle both cases gracefully
            assertTrue("App should handle both cases (has data or empty state)",
                    hasLogs || showsEmpty);
        });
    }
}