package com.example.eventlottery;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

/**
 * Instrumented test for list visibility after event creation.
 *
 * Ensures that a newly created event appears in:
 * - OrganizerDashboard ("Your Events" list)
 * - EventsFragment ("Event List")
 *
 * This test supports US 02.01.01 end-to-end UX expectations.
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class EventsListShowsCreatedEventTest {

    @Test
    public void createdEvent_appearsInOrganizerAndEventsLists() throws Exception {
        ActivityScenario.launch(MainActivity.class);

        String uniqueName = "Event_" + System.currentTimeMillis();

        // ---- Create event via Organizer tab ----
        onView(withId(R.id.nav_organizer)).perform(click());
        onView(withId(R.id.btn_create_event)).perform(click());

        onView(withId(R.id.et_event_name)).perform(replaceText(uniqueName), closeSoftKeyboard());
        onView(withId(R.id.et_event_description)).perform(replaceText("List verification test"), closeSoftKeyboard());
        onView(withId(R.id.et_event_location)).perform(replaceText("Edmonton"), closeSoftKeyboard());
        onView(withId(R.id.et_event_capacity)).perform(replaceText("5"), closeSoftKeyboard());
        onView(withId(R.id.et_event_start)).perform(replaceText("2026-03-10"), closeSoftKeyboard());
        onView(withId(R.id.et_event_end)).perform(replaceText("2026-03-12"), closeSoftKeyboard());

        onView(withId(R.id.btn_publish_event)).perform(click());

        // QR should show
        onView(withId(R.id.iv_qr_code)).check(matches(isDisplayed()));

        // Go back to organizer dashboard (back stack)
        pressBack();

        // ---- Assert event shows in Organizer "Your Events" list ----
        // Allow a moment for Firestore read to refresh (simple polling)
        assertTrue("Event did not appear in Organizer list in time",
                waitForTextToAppear(uniqueName, 8000));

        // ---- Now check Events tab list ----
        onView(withId(R.id.nav_events)).perform(click());

        assertTrue("Event did not appear in Events list in time",
                waitForTextToAppear(uniqueName, 8000));
    }

    /**
     * Polls the UI for a given text for up to timeoutMs.
     * This is a lightweight alternative to adding IdlingResources.
     */
    private boolean waitForTextToAppear(String text, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            try {
                onView(withText(text)).check(matches(isDisplayed()));
                return true;
            } catch (Throwable ignored) {
                Thread.sleep(250);
            }
        }
        return false;
    }

    // Espresso "matches" helper
    private static androidx.test.espresso.ViewAssertion matches(org.hamcrest.Matcher<android.view.View> matcher) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            assertTrue("View did not match", matcher.matches(view));
        };
    }
}