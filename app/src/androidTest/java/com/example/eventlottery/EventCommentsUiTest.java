package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI test for event comments.
 *
 * Requires a device/emulator with at least one event in the list.
 *
 * User stories covered:
 * - US 01.08.01: Post a comment on an event
 * - US 01.08.02: View comments on an event
 *
 * @author Edwin David
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class EventCommentsUiTest {

    @Test
    public void commentSection_isDisplayed_onEventDetail() {
        ActivityScenario.launch(MainActivity.class);

        onView(withId(R.id.nav_events)).perform(click());

        onView(withId(R.id.recycler_events))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.recycler_comments)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_comment)).check(matches(isDisplayed()));
        onView(withId(R.id.button_post_comment)).check(matches(isDisplayed()));
    }
}
