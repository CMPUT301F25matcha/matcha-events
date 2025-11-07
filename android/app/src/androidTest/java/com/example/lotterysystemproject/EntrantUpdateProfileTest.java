package com.example.lotterysystemproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Views.Entrant.EditProfileActivity;
import com.example.lotterysystemproject.Views.Entrant.ProfileHostActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EntrantUpdateProfileTest {

    private Context ctx;

    @Before
    public void seedUser() {
        ctx = ApplicationProvider.getApplicationContext();
        SharedPreferences sp = ctx.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sp.edit()
                .putString("userId", "device-123")
                .putString("userName", "Old Name")
                .putString("userEmail", "old@example.com")
                .putString("userPhone", "000-0000")
                .apply();
    }

    @Test
    public void editProfile_savesAndShowsOnProfile() {
        // Launch EditProfileActivity with known user id
        Intent edit = new Intent(ctx, EditProfileActivity.class)
                .putExtra(EditProfileActivity.EXTRA_USER_ID, "device-123");

        try (ActivityScenario<EditProfileActivity> s = ActivityScenario.launch(edit)) {
            // Enter new details
            onView(withId(R.id.input_name)).perform(clearText(), replaceText("June Hu"), closeSoftKeyboard());
            onView(withId(R.id.input_email)).perform(clearText(), replaceText("june@example.com"), closeSoftKeyboard());
            onView(withId(R.id.input_phone)).perform(clearText(), replaceText("780-555-0123"), closeSoftKeyboard());

            // Save
            onView(withId(R.id.btn_save)).perform(click());
        }

        // Open profile and verify updated fields are displayed
        try (ActivityScenario<ProfileHostActivity> s2 =
                     ActivityScenario.launch(new Intent(ctx, ProfileHostActivity.class))) {
            onView(withId(R.id.profile_name)).check(matches(withText("June Hu")));
            onView(withId(R.id.profile_email)).check(matches(withText("june@example.com")));
            onView(withId(R.id.profile_phone)).check(matches(withText("780-555-0123")));
        }
    }
}
