package com.example.lotterysystemproject.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lotterysystemproject.Models.NotificationItem;

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
     * Saves user’s response for specific notification.
     * @param c              Context used to access SharedPreferences.
     * @param notificationId Unique ID of the notification.
     * @param r              The InvitationResponse to store.
     */
    public static void saveResponse(Context c, String notificationId, NotificationItem.InvitationResponse r) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString("resp_" + notificationId, r.name()).apply();
    }

    /**
     * Loads the stored user response for a given notification.
     * @param c              Context used to access SharedPreferences.
     * @param notificationId Unique ID of the notification.
     * @return Saved NotificationItem.InvitationResponse or NONE if not found.
     */
    public static NotificationItem.InvitationResponse loadResponse(Context c, String notificationId) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String s = sp.getString("resp_" + notificationId, NotificationItem.InvitationResponse.NONE.name());
        try { return NotificationItem.InvitationResponse.valueOf(s); }
        catch (IllegalArgumentException e) { return NotificationItem.InvitationResponse.NONE; }
    }

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
