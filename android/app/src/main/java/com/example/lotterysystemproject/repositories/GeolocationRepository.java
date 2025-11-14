package com.example.lotterysystemproject.repositories;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public interface GeolocationRepository {

    // Save location of entrant joining waiting list
    void saveJoinLocation(String eventId, String entrantId,
                          double lat, double lng,
                          RepositoryCallback<Void> callback);

    // Organizer views map of join locations
    void getJoinLocations(String eventId,
                          RepositoryCallback<List<GeoPoint>> callback);
}

