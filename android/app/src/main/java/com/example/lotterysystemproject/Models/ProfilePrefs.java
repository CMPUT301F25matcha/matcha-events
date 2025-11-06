package com.example.lotterysystemproject.Models;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Local profile storage using SharedPreferences.
 * Stores small JSON-like strings (no remote sync).
 */
public final class ProfilePrefs {
    private static final String PREFS = "local_profile_store";
    private final SharedPreferences prefs;

    public ProfilePrefs(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveProfile(String userId, String name, String email, String phone) {
        String value = name + "|" + email + "|" + phone;
        prefs.edit().putString("profile:" + userId, value).apply();
    }

    public String getProfile(String userId) {
        return prefs.getString("profile:" + userId, null);
    }

    public void deleteUser(String userId) {
        prefs.edit().remove("profile:" + userId).apply();
    }
}