package com.example.eventlottery;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.example.eventlottery.service.DeviceIdentityService;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.junit.Rule;

/**
 * Instrumented test for RouterActivity routing logic.
 *
 * Validates US 01.07.01 behavior:
 * - If no profile exists for deviceId, Router should route to SignupActivity.
 *
 * Note:
 * - This test uses real Firestore and assumes collection "users".
 * - Ensure Firestore rules allow reads/writes during testing.
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class RouterActivityRoutingTest {

    private FirebaseFirestore db;
    private String deviceId;

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setup() throws Exception {

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        deviceId = DeviceIdentityService.getDeviceId(context);

        db = FirebaseFirestore.getInstance();

        CountDownLatch latch = new CountDownLatch(1);

        db.collection("users").document(deviceId)
                .delete()
                .addOnSuccessListener(v -> latch.countDown())
                .addOnFailureListener(e -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void router_withoutExistingProfile_opensSignup() {

        // Launch AFTER deletion
        ActivityScenario.launch(RouterActivity.class);

        onView(withId(R.id.signup_name)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSignUpUser)).check(matches(isDisplayed()));
    }

    // Espresso "matches" import helper:
    private static androidx.test.espresso.ViewAssertion matches(org.hamcrest.Matcher<android.view.View> matcher) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            assertTrue("View did not match", matcher.matches(view));
        };
    }
}