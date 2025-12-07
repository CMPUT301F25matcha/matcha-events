package com.example.lotterysystemproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.views.entrant.NotificationsActivity;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

// TODO: fix test so that it makes it's own notification instead of depending on an existing notification
//    Perhaps use a mock database
// @RunWith(AndroidJUnit4.class)
public class EntrantNotifTest {

    /** Click a child view inside a RecyclerView item at adapter position */
    private static ViewAction clickRecyclerChildAt(int position, int childId) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isAssignableFrom(RecyclerView.class); }
            @Override public String getDescription() { return "Click child " + childId + " at adapter pos " + position; }
            @Override public void perform(UiController ui, View view) {
                RecyclerView rv = (RecyclerView) view;
                rv.scrollToPosition(position);
                ui.loopMainThreadUntilIdle();
                RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(position);
                if (vh == null) {
                    // try again after scroll settles
                    ui.loopMainThreadUntilIdle();
                    vh = rv.findViewHolderForAdapterPosition(position);
                }
                if (vh == null) throw new AssertionError("ViewHolder at pos " + position + " not found");
                View child = vh.itemView.findViewById(childId);
                if (child == null) throw new AssertionError("Child " + childId + " not found in item");
                child.performClick();
                ui.loopMainThreadUntilIdle();
            }
        };
    }

//    @Test
//    public void acceptInvitation_persistsAcrossRelaunch() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), NotificationsActivity.class);
//
//        try (ActivityScenario<NotificationsActivity> s = ActivityScenario.launch(intent)) {
//            // Row 0 → tap Accept
//            onView(withId(R.id.recycler_notifications))
//                    .perform(clickRecyclerChildAt(0, R.id.btn_accept));
//
//            // Status shows “accepted”
//            onView(allOf(withId(R.id.invite_status), withText(containsString("accepted"))))
//                    .check(matches(isDisplayed()));
//        }
//
//        // Relaunch → status persists from SharedPreferences
//        try (ActivityScenario<NotificationsActivity> s2 = ActivityScenario.launch(intent)) {
//            onView(allOf(withId(R.id.invite_status), withText(containsString("accepted"))))
//                    .check(matches(isDisplayed()));
//        }
//    }
//
//    @Test
//    public void declineInvitation_persistsAcrossRelaunch() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), NotificationsActivity.class);
//
//        try (ActivityScenario<NotificationsActivity> s = ActivityScenario.launch(intent)) {
//            // Row 1 → tap Decline
//            onView(withId(R.id.recycler_notifications))
//                    .perform(clickRecyclerChildAt(1, R.id.btn_decline));
//
//            onView(allOf(withId(R.id.invite_status), withText(containsString("declined"))))
//                    .check(matches(isDisplayed()));
//        }
//
//        try (ActivityScenario<NotificationsActivity> s2 = ActivityScenario.launch(intent)) {
//            onView(allOf(withId(R.id.invite_status), withText(containsString("declined"))))
//                    .check(matches(isDisplayed()));
//        }
//    }
}
