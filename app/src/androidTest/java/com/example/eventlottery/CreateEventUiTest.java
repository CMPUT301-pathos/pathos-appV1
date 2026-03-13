package com.example.eventlottery;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented UI test for US 02.01.01 (Create Event + QR generation).
 *
 * Verifies:
 * 1) Organizer tab opens organizer dashboard
 * 2) Create button navigates to CreateEventFragment
 * 3) Publishing generates a QR screen (QR image + payload visible)
 *
 * Notes:
 * - this test assumes the current user is allowed to create events
 * - it launches MainActivity with profileCompleted = true
 * - Firestore persistence is not asserted here
 *
 * @author Kenneth Joseph
 * @version 1.3
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventUiTest {

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
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                MainActivity.class
        );
        intent.putExtra("profileCompleted", true);

        ActivityScenario.launch(intent);

        onView(withId(R.id.nav_organizer)).perform(click());
        onView(withId(R.id.btn_create_event)).perform(click());

        onView(withId(R.id.et_event_name))
                .perform(replaceText("UI Test Event"), closeSoftKeyboard());
        onView(withId(R.id.et_event_description))
                .perform(replaceText("Created by Espresso UI test"), closeSoftKeyboard());
        onView(withId(R.id.et_event_location))
                .perform(replaceText("Edmonton"), closeSoftKeyboard());
        onView(withId(R.id.et_event_capacity))
                .perform(replaceText("10"), closeSoftKeyboard());
        onView(withId(R.id.et_event_start))
                .perform(replaceText("2026-03-10"), closeSoftKeyboard());
        onView(withId(R.id.et_event_end))
                .perform(replaceText("2026-03-12"), closeSoftKeyboard());

        onView(withId(R.id.btn_publish_event)).perform(click());

        onView(withId(R.id.iv_qr_code)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_qr_payload)).check(matches(isDisplayed()));
    }

    private static androidx.test.espresso.ViewAssertion matches(org.hamcrest.Matcher<android.view.View> matcher) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            assertTrue("View did not match", matcher.matches(view));
        };
    }
}