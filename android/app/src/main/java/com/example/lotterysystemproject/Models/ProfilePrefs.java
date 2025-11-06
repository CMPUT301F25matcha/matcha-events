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

    /**
     * Constructs a ProfilePrefs instance.
     * @param ctx Application or activity context used to access SharedPreferences.
     */
    public ProfilePrefs(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /**
     * Saves profile information for a specific user ID.
     * @param userId Unique identifier for the user.
     * @param name   User’s name.
     * @param email  User’s email address.
     * @param phone  User’s phone number.
     */
    public void saveProfile(String userId, String name, String email, String phone) {
        String value = name + "|" + email + "|" + phone;
        prefs.edit().putString("profile:" + userId, value).apply();
    }

    /**
     * Retrieves saved profile data for given user ID.
     * @param userId Unique identifier for the user.
     * @return Stored profile string, or null if not found.
     */
    public String getProfile(String userId) {
        return prefs.getString("profile:" + userId, null);
    }

    /**
     * Deletes stored profile associated with specified user ID.
     * @param userId Unique identifier of the user whose profile should be deleted.
     */
    public void deleteUser(String userId) {
        prefs.edit().remove("profile:" + userId).apply();
    }
}