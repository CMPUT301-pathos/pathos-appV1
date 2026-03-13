package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
/*
 * UI test for US 01.02.03 (Event History).
 *NOT IMPLEMENTED
 */
@RunWith(AndroidJUnit4.class)
public class EventHistoryUiTest {

    @Test
    public void profileEventHistoryButton_opensEventHistoryScreen() {
        ActivityScenario.launch(MainActivity.class);

        // Go to Profile tab
        onView(withId(R.id.nav_profile)).perform(click());

        // Click View Event History
        onView(withId(R.id.button_event_history)).perform(click());

        // Verify Event History screen loaded.
        onView(withId(R.id.history_list)).check(matches(isDisplayed()));
    }
}