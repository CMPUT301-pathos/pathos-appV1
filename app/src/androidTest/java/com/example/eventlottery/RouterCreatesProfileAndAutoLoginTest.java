package com.example.eventlottery;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.example.eventlottery.service.DeviceIdentityService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test verifying:
 * - launching RouterActivity with no existing profile creates a default Firestore profile
 * - relaunching RouterActivity still finds that profile for the same device
 *
 * Uses real Firestore (collection "users") and the current deviceId.
 *
 * @author Kenneth Joseph
 * @version 1.3
 */
@RunWith(AndroidJUnit4.class)
public class RouterCreatesProfileAndAutoLoginTest {

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    private FirebaseFirestore db;
    private String deviceId;

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
    public void router_createsDefaultFirestoreDoc_andFindsItAgainAfterRelaunch() throws Exception {
        ActivityScenario<RouterActivity> scenario1 = ActivityScenario.launch(RouterActivity.class);

        DocumentSnapshot firstDoc = waitForUserDoc(deviceId, 10);
        assertNotNull("Profile document should be created", firstDoc);
        assertTrue("Profile document should exist after first launch", firstDoc.exists());
        assertEquals(deviceId, firstDoc.getString("deviceId"));
        assertEquals("user", firstDoc.getString("role"));

        Boolean firstCompleted = firstDoc.getBoolean("profileCompleted");
        assertNotNull("profileCompleted should exist", firstCompleted);
        assertFalse("Default created profile should be incomplete", firstCompleted);

        scenario1.close();

        ActivityScenario<RouterActivity> scenario2 = ActivityScenario.launch(RouterActivity.class);

        DocumentSnapshot secondDoc = waitForUserDoc(deviceId, 10);
        assertNotNull("Profile document should still exist after relaunch", secondDoc);
        assertTrue("Profile document should still exist after relaunch", secondDoc.exists());
        assertEquals(deviceId, secondDoc.getString("deviceId"));
        assertEquals("user", secondDoc.getString("role"));

        Boolean secondCompleted = secondDoc.getBoolean("profileCompleted");
        assertNotNull("profileCompleted should still exist after relaunch", secondCompleted);
        assertFalse("Profile should still be incomplete until user fills it", secondCompleted);

        scenario2.close();
    }

    private DocumentSnapshot waitForUserDoc(String id, int timeoutSeconds) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < deadline) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<DocumentSnapshot> docRef = new AtomicReference<>();

            db.collection("users").document(id)
                    .get()
                    .addOnSuccessListener(doc -> {
                        docRef.set(doc);
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> latch.countDown());

            latch.await(2, TimeUnit.SECONDS);

            DocumentSnapshot doc = docRef.get();
            if (doc != null && doc.exists()) {
                return doc;
            }

            Thread.sleep(250);
        }

        return null;
    }
}