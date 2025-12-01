package com.example.lotterysystemproject.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.lotterysystemproject.models.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages featured events with optional caching.
 * Note: Consider removing caching entirely if it causes issues with deleted events.
 */
public class FeaturedEventsManager {

    private static final String TAG = "FeaturedEventsManager";
    private static final String PREFS_NAME = "featured_events_prefs";
    private static final String KEY_EVENTS = "cached_events";
    private static final String KEY_LAST_UPDATE = "last_update_date";

    private final SharedPreferences prefs;
    private final Gson gson;

    public FeaturedEventsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Checks if cached events should be refreshed.
     * Returns true if events haven't been updated today.
     */
    public boolean shouldUpdate() {
        long lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0);
        long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        long lastUpdateDay = lastUpdate / (1000 * 60 * 60 * 24);

        return today > lastUpdateDay;
    }

    /**
     * Saves featured events to SharedPreferences.
     * WARNING: This caching may cause deleted events to persist.
     * Consider removing this feature if it causes issues.
     */
    public void saveFeaturedEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            Log.w(TAG, "Attempted to save null/empty events");
            return;
        }

        String json = gson.toJson(events);
        prefs.edit()
                .putString(KEY_EVENTS, json)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .apply();

        Log.d(TAG, "Saved " + events.size() + " featured events to cache");
    }

    /**
     * Loads cached featured events from SharedPreferences.
     * Returns empty list if none cached.
     */
    public List<Event> loadFeaturedEvents() {
        String json = prefs.getString(KEY_EVENTS, null);

        if (json == null) {
            Log.d(TAG, "No cached events found");
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<Event>>(){}.getType();
            List<Event> events = gson.fromJson(json, type);
            Log.d(TAG, "Loaded " + (events != null ? events.size() : 0) + " cached events");
            return events != null ? events : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse cached events", e);
            clearCache();
            return new ArrayList<>();
        }
    }

    /**
     * Clears all cached featured events.
     * Call this if you want to force a refresh.
     */
    public void clearCache() {
        prefs.edit()
                .remove(KEY_EVENTS)
                .remove(KEY_LAST_UPDATE)
                .apply();
        Log.d(TAG, "Cache cleared");
    }
}