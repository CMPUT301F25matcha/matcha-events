package com.example.lotterysystemproject.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small helper around the same SharedPreferences file used by the UserInfo controller ("UserPrefs").
 * UserInfo persists: userId, userName, userEmail, userPhone, userRole, signedUp.
 *
 * Use cases:
 *  - Clear all user session data after account deletion
 *  - Check or toggle the signed-up flag
 *  - Read/write the current userId
 */
public final class AuthState {

    // Must match UserInfo controller
    private static final String PREFS_NAME   = "UserPrefs";
    private static final String KEY_USER_ID  = "userId";
    private static final String KEY_SIGNED_UP = "signedUp";

    private AuthState() {}

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Remove ALL keys written by UserInfo (userId/name/email/phone/role/signedUp). */
    public static void clearUserPrefs(Context c) {
        sp(c).edit().clear().apply();
    }

    /** Returns true if a userId exists (same logic used by UserInfo.isUserAlreadySignedUp). */
    public static boolean hasUser(Context c) {
        return sp(c).contains(KEY_USER_ID);
    }

    /** Get the current userId, or null if none. */
    public static String getUserId(Context c) {
        return sp(c).getString(KEY_USER_ID, null);
    }

    /** Set/replace the current userId (rarely needed; normally UserInfo persists this). */
    public static void setUserId(Context c, String userId) {
        sp(c).edit().putString(KEY_USER_ID, userId).apply();
    }

    /** Read the "signedUp" flag (defaults to false if unset). */
    public static boolean isSignedUp(Context c) {
        return sp(c).getBoolean(KEY_SIGNED_UP, false);
    }

    /** Write the "signedUp" flag. */
    public static void setSignedUp(Context c, boolean value) {
        sp(c).edit().putBoolean(KEY_SIGNED_UP, value).apply();
    }
}
