package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileUpdateUiTest {

    @Test
    public void profileTab_showsEditableFields_andSaveButton() {
        ActivityScenario.launch(MainActivity.class);

        // Go to Profile tab
        onView(withId(R.id.nav_profile)).perform(click());

        // Confirm fields exist (US 01.02.02)
        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_phone)).check(matches(isDisplayed()));

        // Confirm save button exists
        onView(withId(R.id.button_save_profile)).check(matches(isDisplayed()));
    }
}