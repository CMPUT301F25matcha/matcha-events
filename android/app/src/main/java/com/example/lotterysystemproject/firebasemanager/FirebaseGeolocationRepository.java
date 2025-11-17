package com.example.lotterysystemproject.firebasemanager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseGeolocationRepository implements GeolocationRepository {

    private final FirebaseFirestore db;

    public FirebaseGeolocationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void saveJoinLocation(String eventId, String entrantId, double lat, double lng, RepositoryCallback<Void> callback) {
        Map<String, Object> location = new HashMap<>();
        location.put("entrantId", entrantId);
        location.put("geopoint", new GeoPoint(lat, lng));
        location.put("timestamp", System.currentTimeMillis());

        db.collection("events").document(eventId)
                .collection("join_locations").document(entrantId)
                .set(location)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    @Override
    public void getJoinLocations(String eventId, RepositoryCallback<List<GeoPoint>> callback) {
        db.collection("events").document(eventId)
                .collection("join_locations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<GeoPoint> locations = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        GeoPoint geopoint = doc.getGeoPoint("geopoint");
                        if (geopoint != null) {
                            locations.add(geopoint);
                        }
                    });
                    if (callback != null) callback.onSuccess(locations);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }
}
