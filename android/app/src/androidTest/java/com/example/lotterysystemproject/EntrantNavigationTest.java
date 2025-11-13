package com.example.lotterysystemproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.views.entrant.EntrantMainActivity;
import com.example.lotterysystemproject.views.entrant.ProfileHostActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EntrantNavigationTest {

    /**
     * Flow for test:
     * 1) Launch Home (EntrantMainActivity)
     * 2) Tap Notifications in bottom nav → NotificationsActivity visible
     * 3) Tap Profile in bottom nav → ProfileHostActivity/fragment visible
     * 4) Tap Settings button on Profile → Settings fragment visible
     * 5) Press back → returns to Profile fragment
     */
    @Test
    public void bottomNav_toNotifications_toProfile_openSettings_thenBack() {
        try (ActivityScenario<EntrantMainActivity> scenario =
                     ActivityScenario.launch(new Intent(
                             ApplicationProvider.getApplicationContext(),
                             EntrantMainActivity.class))) {

            // Home → Notifications
            onView(withId(R.id.nav_notifications)).perform(click());
            onView(withId(R.id.recycler_notifications)).check(matches(isDisplayed()));

            // Notifications → Profile
            onView(withId(R.id.nav_profile)).perform(click());
            onView(withId(R.id.profile_name)).check(matches(isDisplayed()));

            // Profile → Settings
            onView(withId(R.id.btn_settings)).check(matches(isDisplayed())).perform(click());
            onView(withId(R.id.settings_toolbar)).check(matches(isDisplayed()));

            // Back to Profile
            pressBack();
            onView(withId(R.id.profile_name)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_settings)).check(matches(isDisplayed()));
        }
    }


    // Checks that tapping Events History from profile displays history
    @Test
    public void openHistory_fromProfile_displaysMockItems() {
        try (ActivityScenario<ProfileHostActivity> s =
                     ActivityScenario.launch(new Intent(
                             ApplicationProvider.getApplicationContext(),
                             ProfileHostActivity.class))) {

            // Profile screen visible
            onView(withId(R.id.profile_name)).check(matches(isDisplayed()));

            // Tap the "Events History" button on your profile (ensure this id exists there)
            onView(withId(R.id.btn_events_history)).perform(click());

            // History list is shown
            onView(withId(R.id.history_recycler)).check(matches(isDisplayed()));

            // Mock item from EventRegistrationHistoryFragment should be visible
            onView(withText("Piano Lessons")).check(matches(isDisplayed()));
        }
    }
}
