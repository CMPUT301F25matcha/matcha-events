package com.example.lotterysystemproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lotterysystemproject.models.NotificationItem;

/**
 * Provides persistent local storage    for user responses to invitation-type notifications.
 * Allows app to remember whether a user has accepted or declined an event invitation across sessions.
 */
public final class NotificationsLocalStore {
    private static final String PREFS = "NotificationsPrefs";
    // Values: "NONE", "ACCEPTED", "DECLINED"

    /** Private constructor to prevent instantiation. */
    private NotificationsLocalStore(){}


    /**
     * Clears the stored response for a specific notification.
     * Used for demo purposes via long-press gesture in UI
     * @param c              Context used to access SharedPreferences
     * @param notificationId Unique ID of the notification.
     */
    // For quick demo testing (press and hold to reset)
    public static void clearFor(Context c, String notificationId) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().remove("resp_" + notificationId).apply();
    }

    /**
     * Clears all stored responses for all notifications.
     * Intended for when a new entrant profile is created or existing user logs out.
     * @param c Context used to access SharedPreferences
     */
    // For notification reset upon new user
    public static void clearAll(Context c) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

}
