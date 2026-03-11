package com.example.eventlottery.adminTest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.example.eventlottery.R;
import com.example.eventlottery.admin.AdminBrowseUsersActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Test for US 03.02.01 - Delete user profiles
 * @author hasratsinghchauhan
 *  * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 */
@RunWith(AndroidJUnit4.class)
public class US03_02_01_DeleteUsersTest {

    @Before
    public void setUp() {
        ActivityScenario.launch(AdminBrowseUsersActivity.class);
    }

    @Test
    public void testDeleteButtonExists() {
        onView(withId(R.id.recyclerViewUsers)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackButtonWorks() {
        onView(withId(R.id.btnBack)).perform(click());
    }
}