package com.example.lotterysystemproject;

import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.Espresso.onView;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.lotterysystemproject.controllers.AdminBrowseProfiles;
import com.example.lotterysystemproject.controllers.AdminDashboard;
import com.example.lotterysystemproject.views.admin.AdminLoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminIntentNavigationTest {

    @Rule
    public ActivityScenarioRule<AdminLoginActivity> activityRule =
            new ActivityScenarioRule<>(AdminLoginActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testOpenBrowseProfiles() {
        // simulate click on "Browse Profiles" button
        onView(withId(R.id.btn_browse_profiles)).perform(click());

        // verify that AdminBrowseProfiles activity was started
        onView(withId(R.id.recycler_profiles)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackArrowFromBrowseProfiles() {

        // Go to Browse Profiles
        onView(withId(R.id.btn_browse_profiles)).perform(click());

        // Press back
        pressBack();

        // Verify that the AdminDashboard fragment is visible again
        onView(withId(R.id.btn_browse_profiles)).check(matches(isDisplayed()));
    }

    @Test
    public void testOpenBroseEvents() {
        onView(withId(R.id.btn_browse_events)).perform(click());

        onView(withId(R.id.recycler_events)).check(matches(isDisplayed()));
    }









}
