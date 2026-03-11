package com.example.eventlottery.adminTest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.example.eventlottery.R;
import com.example.eventlottery.admin.AdminBrowseUsersActivity;

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
 * Test for US 03.05.01 - As an administrator, I want to be able to browse profiles.
 * @author hasratsinghchauhan
 *  * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 */
@RunWith(AndroidJUnit4.class)
public class US03_05_01_BrowseUsersTest {

    @Before
    public void setUp() {
        ActivityScenario.launch(AdminBrowseUsersActivity.class);
    }

    @Test
    public void testUsersListIsDisplayed() {
        onView(withId(R.id.recyclerViewUsers)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchFunctionality() {
        onView(withId(R.id.searchUsers))
                .perform(typeText("John"))
                .check(matches(withText("John")));
    }

    @Test
    public void testBackButtonReturnsToDashboard() {
        onView(withId(R.id.btnBack)).perform(click());
    }
}