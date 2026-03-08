package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotificationsNavigationTest {

    @Test
    public void notificationsTab_opensNotificationsScreen() {
        ActivityScenario.launch(MainActivity.class);

        // If bottom-nav item id works:
        onView(withId(R.id.nav_notifications)).perform(click());

        // Verify your Notifications UI is visible.
        onView(withText("Accept")).check(matches(isDisplayed()));
        onView(withText("Decline")).check(matches(isDisplayed()));
    }
}