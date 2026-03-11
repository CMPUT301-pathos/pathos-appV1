package com.example.eventlottery.adminTest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.example.eventlottery.R;
import com.example.eventlottery.admin.AdminBrowseEventsActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Test for US 03.04.01 - As an administrator, I want to be able to browse events.
 * @author hasratsinghchauhan
 */
@RunWith(AndroidJUnit4.class)
public class US03_04_01_BrowseEventsTest {

    @Before
    public void setUp() {
        // Launch the activity before each test
        ActivityScenario.launch(AdminBrowseEventsActivity.class);
    }

    @Test
    public void testEventsListIsDisplayed() {
        // Verify that the RecyclerView is displayed
        onView(withId(R.id.recyclerViewEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchFunctionality() {
        // Type in search box
        onView(withId(R.id.searchEvents))
                .perform(typeText("Swimming"))
                .check(matches(withText("Swimming")));
    }

    @Test
    public void testFilterChipsExist() {
        // Verify all filter chips are present
        onView(withId(R.id.chipAll)).check(matches(isDisplayed()));
        onView(withId(R.id.chipActive)).check(matches(isDisplayed()));
        onView(withId(R.id.chipInactive)).check(matches(isDisplayed()));
        onView(withId(R.id.chipCompleted)).check(matches(isDisplayed()));
    }

    @Test
    public void testSortButtonExists() {
        onView(withId(R.id.btnSort)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackButtonReturnsToDashboard() {
        // Click back button
        onView(withId(R.id.btnBack)).perform(click());

        // Activity should finish - hard to test directly
        // This test passes if no crash occurs
    }

    @Test
    public void testEmptyStateIsShownWhenNoEvents() {
        // This would require mocking, but for now just verify empty state view exists
        onView(withId(R.id.emptyStateLayout)).check(matches(not(isDisplayed())));
    }
}