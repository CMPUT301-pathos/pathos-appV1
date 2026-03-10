package com.example.eventlottery;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.eventlottery.service.DeviceIdentityService;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test verifying:
 * - US 01.02.01: Signup saves profile to Firestore
 * - US 01.07.01: Router auto-routes user when profile exists
 *
 * Uses real Firestore (collection "users") and the current deviceId.
 *
 * @author Kenneth Joseph
 * @version 1.1
 */
@RunWith(AndroidJUnit4.class)
public class SignupCreatesProfileAndAutoLoginTest {

    private FirebaseFirestore db;
    private String deviceId;

    @Before
    public void setup() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        deviceId = DeviceIdentityService.getDeviceId(context);
        db = FirebaseFirestore.getInstance();

        // Remove any existing profile before running test
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("users").document(deviceId)
                .delete()
                .addOnSuccessListener(v -> latch.countDown())
                .addOnFailureListener(e -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void signup_createsFirestoreDoc_andRouterSkipsSignupAfterwards() throws Exception {

        // Launch Router -> should land on Signup
        ActivityScenario<RouterActivity> routerScenario = ActivityScenario.launch(RouterActivity.class);

        // Fill fields on Signup
        onView(withId(R.id.signup_name)).perform(clearText(), typeText("Kenneth Test"), closeSoftKeyboard());
        onView(withId(R.id.signup_email)).perform(clearText(), typeText("kenneth@test.com"), closeSoftKeyboard());
        onView(withId(R.id.signup_phone)).perform(clearText(), typeText(""), closeSoftKeyboard());

        // Tap "Entrant" signup
        onView(withId(R.id.btnSignupEntrant)).perform(click());

        // Wait until Firestore has the document
        assertTrue("Profile doc was not created in Firestore",
                waitForUserDocExists(deviceId, 8));

        // Close and relaunch Router to simulate app restart
        routerScenario.close();

        ActivityScenario<RouterActivity> routerScenario2 = ActivityScenario.launch(RouterActivity.class);

        // Assert we are NOT back on signup by checking a view from MainActivity.
        // Replace bottom_nav with whatever ID exists on your MainActivity.
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));

        routerScenario2.close();
    }

    private boolean waitForUserDocExists(String id, int timeoutSeconds) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < deadline) {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] exists = {false};

            db.collection("users").document(id)
                    .get()
                    .addOnSuccessListener(doc -> {
                        exists[0] = (doc != null && doc.exists());
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        exists[0] = false;
                        latch.countDown();
                    });

            latch.await(2, TimeUnit.SECONDS);

            if (exists[0]) return true;

            Thread.sleep(250);
        }

        return false;
    }

    // Espresso "matches" helper
    private static androidx.test.espresso.ViewAssertion matches(org.hamcrest.Matcher<android.view.View> matcher) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            org.junit.Assert.assertTrue("View did not match", matcher.matches(view));
        };
    }
}