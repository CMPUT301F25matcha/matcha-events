package com.example.lotterysystemproject.utils;

import android.os.SystemClock;

import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.models.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manager responsible for providing the top recent events.
 * Caches results briefly to avoid repeated queries during short sessions.
 */
public class RecentEventsManager {

    private static final long CACHE_TTL_MS = 60 * 1000L; // 1 minute cache (tunable)

    private final EventRepository repo;
    private List<Event> cached = new ArrayList<>();
    private long lastFetchTime = 0;

    public RecentEventsManager(EventRepository repository) {
        this.repo = repository;
    }

    /**
     * Loads recent events; results are delivered via the provided callbacks.
     * If cache is fresh, returns cached results immediately.
     *
     * @param limit    how many recent events to fetch
     * @param onSuccess consumer for the resulting list
     * @param onError   consumer for exceptions
     */
    public void loadRecentEvents(int limit, Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        long now = SystemClock.elapsedRealtime();
        if (!cached.isEmpty() && now - lastFetchTime < CACHE_TTL_MS) {
            // return cached copy
            onSuccess.accept(new ArrayList<>(cached.size() > limit ? cached.subList(0, limit) : cached));
            return;
        }

        // Ask repository for recent events (ordered by createdAt desc)
        repo.getRecentEvents(limit * 2, // fetch more then trim locally to be safe
                events -> {
                    if (events == null) {
                        onSuccess.accept(new ArrayList<>());
                        return;
                    }
                    // sort by createdAt desc (most recent first)
                    Collections.sort(events, (a, b) -> {
                        if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                        if (a.getCreatedAt() == null) return 1;
                        if (b.getCreatedAt() == null) return -1;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    });

                    List<Event> top = events.size() > limit ? new ArrayList<>(events.subList(0, limit)) : new ArrayList<>(events);
                    cached = new ArrayList<>(top);
                    lastFetchTime = SystemClock.elapsedRealtime();

                    onSuccess.accept(top);
                },
                onError
        );
    }
}
