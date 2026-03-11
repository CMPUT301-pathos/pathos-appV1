package com.example.eventlottery.adminTest;


import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.example.eventlottery.R;
import com.example.eventlottery.admin.AdminNotificationLogs;

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

/**
 * Test for US 03.08.01 - As an administrator, I want to review logs of all notifications.
 * @author hasratsinghchauhan
 * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 */
@RunWith(AndroidJUnit4.class)
public class US03_08_01_NotificationLogsTest {

    @Before
    public void setUp() {
        ActivityScenario.launch(AdminNotificationLogs.class);
    }

    @Test
    public void testLogsListIsDisplayed() {
        onView(withId(R.id.recyclerViewLogs)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchFunctionality() {
        onView(withId(R.id.etSearchLogs))
                .perform(typeText("Kenneth"))
                .check(matches(withText("Kenneth")));
    }

    @Test
    public void testFilterSpinnerExists() {
        onView(withId(R.id.spinnerFilter)).check(matches(isDisplayed()));
    }

    @Test
    public void testTotalLogsTextIsDisplayed() {
        onView(withId(R.id.tvTotalLogs)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackButtonReturnsToDashboard() {
        onView(withId(R.id.btnBack)).perform(click());
    }
}