package com.example.eventlottery;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;




/**
 * Instrumented UI test for US 02.01.01 (Create Event + QR generation).
 *
 * Verifies:
 * 1) Organizer tab opens organizer dashboard
 * 2) Create button navigates to CreateEventFragment
 * 3) Publishing generates a QR screen (QR image + payload visible)
 *
 * NOTE:
 * - This test does not assert Firestore content. It asserts wiring + QR UI.
 * - Firestore persistence is verified in EventsListShowsCreatedEventTest.
 *
 * @author Kenneth Joseph
 * @version 1.2
 */


@RunWith(AndroidJUnit4.class)
public class CreateEventUiTest {


    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void registerIdling() {
        IdlingRegistry.getInstance()
                .register(CreateEventFragment.getPublishIdlingResource());
    }

    @After
    public void unregisterIdling() {
        IdlingRegistry.getInstance()
                .unregister(CreateEventFragment.getPublishIdlingResource());
    }

    @Test
    public void createEvent_showsQrAfterPublish() {
        // Go to Organizer tab
        onView(withId(R.id.nav_organizer)).perform(click());

        // Click create event button on organizer dashboard
        onView(withId(R.id.btn_create_event)).perform(click());

        // Fill out Create Event form
        onView(withId(R.id.et_event_name)).perform(replaceText("UI Test Event"), closeSoftKeyboard());
        onView(withId(R.id.et_event_description)).perform(replaceText("Created by Espresso UI test"), closeSoftKeyboard());

        // Location + capacity optional (fill to match your form)
        onView(withId(R.id.et_event_location)).perform(replaceText("Edmonton"), closeSoftKeyboard());
        onView(withId(R.id.et_event_capacity)).perform(replaceText("10"), closeSoftKeyboard());

        // Registration dates (required in your implementation)
        onView(withId(R.id.et_event_start)).perform(replaceText("2026-03-10"), closeSoftKeyboard());
        onView(withId(R.id.et_event_end)).perform(replaceText("2026-03-12"), closeSoftKeyboard());

        // Scroll to button, then Publish
        onView(withId(R.id.btn_publish_event))
                .perform(scrollTo(), click());

        // QR screen should appear
        // We assert the QR ImageView and payload text exist
        onView(withId(R.id.iv_qr_code)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_qr_payload)).check(matches(isDisplayed()));
    }

    // Espresso "matches" helper (avoids extra imports sometimes missing in student projects)
    private static androidx.test.espresso.ViewAssertion matches(org.hamcrest.Matcher<android.view.View> matcher) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            assertTrue("View did not match", matcher.matches(view));
        };
    }
}