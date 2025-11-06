package com.example.lotterysystemproject.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lotterysystemproject.Models.NotificationItem;

public final class NotificationsLocalStore {
    private static final String PREFS = "NotificationsPrefs";
    // Values: "NONE", "ACCEPTED", "DECLINED"
    private NotificationsLocalStore(){}

    public static void saveResponse(Context c, String notificationId, NotificationItem.InvitationResponse r) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString("resp_" + notificationId, r.name()).apply();
    }

    public static NotificationItem.InvitationResponse loadResponse(Context c, String notificationId) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String s = sp.getString("resp_" + notificationId, NotificationItem.InvitationResponse.NONE.name());
        try { return NotificationItem.InvitationResponse.valueOf(s); }
        catch (IllegalArgumentException e) { return NotificationItem.InvitationResponse.NONE; }
    }

    // For quick demo testing (press and hold to reset)
    public static void clearFor(Context c, String notificationId) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().remove("resp_" + notificationId).apply();
    }

    // For notification reset upon new user
    public static void clearAll(Context c) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

}
