package com.example.eventlottery.adminTest;


import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlottery.R;
import com.example.eventlottery.admin.AdminBrowseImages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Test for US 03.06.01 - Browse uploaded images
 * @author hasratsinghchauhan
 */
@RunWith(AndroidJUnit4.class)
public class US03_06_01_BrowseImagesTest {

    @Before
    public void setUp() {
        ActivityScenario.launch(AdminBrowseImages.class);
    }

    //we have no images initially
//    @Test
//    public void testImagesGridIsDisplayed() {
//        onView(withId(R.id.recyclerViewImages)).check(matches(isDisplayed()));
//    }
    @Test
    public void testRecyclerViewExists() {
        onView(withId(R.id.recyclerViewImages)).check(matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)
        ));
    }

    @Test
    public void testBackButtonWorks() {
        onView(withId(R.id.btnBack)).perform(click());
    }
}
