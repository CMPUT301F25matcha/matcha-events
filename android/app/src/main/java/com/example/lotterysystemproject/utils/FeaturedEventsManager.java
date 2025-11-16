package com.example.lotterysystemproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lotterysystemproject.models.Event;

import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.Calendar;
import java.util.List;

public class FeaturedEventsManager {

    private static final String PREF_NAME = "featured_events_prefs";
    private static final String KEY_LAST_UPDATE_DAY = "last_update_day";
    private static final String KEY_FEATURED_EVENTS_JSON = "featured_events_json";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public FeaturedEventsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean shouldUpdate() {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int lastDay = prefs.getInt(KEY_LAST_UPDATE_DAY, -1);
        return today != lastDay;
    }

    public void saveFeaturedEvents(List<Event> events) {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        prefs.edit()
                .putInt(KEY_LAST_UPDATE_DAY, today)
                .putString(KEY_FEATURED_EVENTS_JSON, gson.toJson(events))
                .apply();
    }

    public List<Event> loadFeaturedEvents() {
        String json = prefs.getString(KEY_FEATURED_EVENTS_JSON, null);
        if (json == null) return new ArrayList<>();

        Type listType = new TypeToken<List<Event>>() {}.getType();
        return gson.fromJson(json, listType);
    }
}

