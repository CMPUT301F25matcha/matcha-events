package com.example.lotterysystemproject.firebasemanager;

import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Mock implementation of GeolocationRepository for testing
 * Stores geolocation data in memory without Firebase
 */
public class MockGeolocationRepository implements GeolocationRepository {

    private static final String TAG = "MockGeoRepo";

    // Storage: key = eventId_entrantId, value = LocationRecord
    private final Map<String, LocationRecord> locations = new ConcurrentHashMap<>();

    private boolean simulateDelay = false;
    private long delayMs = 100;
    private boolean simulateFailure = false;

    public MockGeolocationRepository() {
    }

    // ========================================================================
    // INTERNAL DATA MODEL
    // ========================================================================

    private static class LocationRecord {
        String eventId;
        String entrantId;
        double latitude;
        double longitude;
        long timestamp;

        LocationRecord(String eventId, String entrantId, double lat, double lng, long timestamp) {
            this.eventId = eventId;
            this.entrantId = entrantId;
            this.latitude = lat;
            this.longitude = lng;
            this.timestamp = timestamp;
        }

        GeoPoint toGeoPoint() {
            return new GeoPoint(latitude, longitude);
        }
    }

    // ========================================================================
    // CONFIGURATION (for testing)
    // ========================================================================

    public void setSimulateDelay(boolean simulate, long delayMs) {
        this.simulateDelay = simulate;
        this.delayMs = delayMs;
    }

    public void setSimulateFailure(boolean simulate) {
        this.simulateFailure = simulate;
    }

    public void clear() {
        locations.clear();
    }

    // ========================================================================
    // SAVE JOIN LOCATION (US 02.02.02)
    // ========================================================================

    @Override
    public void saveJoinLocation(String eventId, String entrantId,
                                 double lat, double lng,
                                 RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated geolocation save failure"));
                    return;
                }

                // Validate coordinates
                if (!isValidLatitude(lat) || !isValidLongitude(lng)) {
                    callback.onFailure(new Exception("Invalid coordinates: " + lat + ", " + lng));
                    return;
                }

                String key = generateKey(eventId, entrantId);
                long timestamp = System.currentTimeMillis();

                LocationRecord record = new LocationRecord(eventId, entrantId, lat, lng, timestamp);
                locations.put(key, record);

                Log.d(TAG, "Saved location for entrant " + entrantId +
                        " at event " + eventId + ": (" + lat + ", " + lng + ")");
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // GET JOIN LOCATIONS (US 02.02.02 - Organizer views map)
    // ========================================================================

    @Override
    public void getJoinLocations(String eventId,
                                 RepositoryCallback<List<GeoPoint>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated geolocation fetch failure"));
                    return;
                }

                List<GeoPoint> geoPoints = locations.values().stream()
                        .filter(record -> record.eventId.equals(eventId))
                        .map(LocationRecord::toGeoPoint)
                        .collect(Collectors.toList());

                Log.d(TAG, "Retrieved " + geoPoints.size() +
                        " locations for event " + eventId);
                callback.onSuccess(geoPoints);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String generateKey(String eventId, String entrantId) {
        return eventId + "_" + entrantId;
    }

    private boolean isValidLatitude(double lat) {
        return lat >= -90.0 && lat <= 90.0;
    }

    private boolean isValidLongitude(double lng) {
        return lng >= -180.0 && lng <= 180.0;
    }

    private void executeAsync(Runnable task) {
        if (simulateDelay) {
            new Thread(() -> {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                task.run();
            }).start();
        } else {
            task.run();
        }
    }

    // ========================================================================
    // TEST HELPER METHODS
    // ========================================================================

    /**
     * Get total number of stored locations
     */
    public int getLocationCount() {
        return locations.size();
    }

    /**
     * Get number of locations for a specific event
     */
    public int getLocationCountForEvent(String eventId) {
        return (int) locations.values().stream()
                .filter(record -> record.eventId.equals(eventId))
                .count();
    }

    /**
     * Check if location exists for an entrant at an event
     */
    public boolean hasLocation(String eventId, String entrantId) {
        String key = generateKey(eventId, entrantId);
        return locations.containsKey(key);
    }

    /**
     * Get location for specific entrant at event
     */
    public GeoPoint getLocation(String eventId, String entrantId) {
        String key = generateKey(eventId, entrantId);
        LocationRecord record = locations.get(key);
        return record != null ? record.toGeoPoint() : null;
    }

    /**
     * Get raw location record (for detailed testing)
     */
    public LocationRecord getLocationRecord(String eventId, String entrantId) {
        String key = generateKey(eventId, entrantId);
        return locations.get(key);
    }

    /**
     * Get all locations for an event (with details)
     */
    public List<LocationRecord> getLocationRecordsForEvent(String eventId) {
        return locations.values().stream()
                .filter(record -> record.eventId.equals(eventId))
                .collect(Collectors.toList());
    }

    /**
     * Calculate distance between two GeoPoints (in kilometers)
     * Uses Haversine formula
     */
    public static double calculateDistance(GeoPoint point1, GeoPoint point2) {
        final double EARTH_RADIUS_KM = 6371.0;

        double lat1 = Math.toRadians(point1.getLatitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double deltaLat = Math.toRadians(point2.getLatitude() - point1.getLatitude());
        double deltaLng = Math.toRadians(point2.getLongitude() - point1.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Get locations within radius of a point (in kilometers)
     */
    public List<GeoPoint> getLocationsWithinRadius(String eventId,
                                                   GeoPoint center,
                                                   double radiusKm) {
        return locations.values().stream()
                .filter(record -> record.eventId.equals(eventId))
                .map(LocationRecord::toGeoPoint)
                .filter(point -> calculateDistance(center, point) <= radiusKm)
                .collect(Collectors.toList());
    }

    /**
     * Get average location (center point) for an event
     */
    public GeoPoint getAverageLocation(String eventId) {
        List<LocationRecord> eventLocations = locations.values().stream()
                .filter(record -> record.eventId.equals(eventId))
                .collect(Collectors.toList());

        if (eventLocations.isEmpty()) {
            return null;
        }

        double avgLat = eventLocations.stream()
                .mapToDouble(r -> r.latitude)
                .average()
                .orElse(0.0);

        double avgLng = eventLocations.stream()
                .mapToDouble(r -> r.longitude)
                .average()
                .orElse(0.0);

        return new GeoPoint(avgLat, avgLng);
    }

    /**
     * Get timestamp of when location was saved
     */
    public long getLocationTimestamp(String eventId, String entrantId) {
        String key = generateKey(eventId, entrantId);
        LocationRecord record = locations.get(key);
        return record != null ? record.timestamp : -1;
    }

    /**
     * Delete location for specific entrant
     */
    public void deleteLocation(String eventId, String entrantId) {
        String key = generateKey(eventId, entrantId);
        locations.remove(key);
    }

    /**
     * Delete all locations for an event
     */
    public void deleteLocationsForEvent(String eventId) {
        locations.entrySet().removeIf(entry ->
                entry.getValue().eventId.equals(eventId));
    }

    /**
     * Get bounding box for all locations at an event
     * Returns [minLat, maxLat, minLng, maxLng]
     */
    public double[] getBoundingBox(String eventId) {
        List<LocationRecord> eventLocations = locations.values().stream()
                .filter(record -> record.eventId.equals(eventId))
                .collect(Collectors.toList());

        if (eventLocations.isEmpty()) {
            return null;
        }

        double minLat = eventLocations.stream()
                .mapToDouble(r -> r.latitude)
                .min()
                .orElse(0.0);

        double maxLat = eventLocations.stream()
                .mapToDouble(r -> r.latitude)
                .max()
                .orElse(0.0);

        double minLng = eventLocations.stream()
                .mapToDouble(r -> r.longitude)
                .min()
                .orElse(0.0);

        double maxLng = eventLocations.stream()
                .mapToDouble(r -> r.longitude)
                .max()
                .orElse(0.0);

        return new double[]{minLat, maxLat, minLng, maxLng};
    }

    /**
     * Common test locations (for convenience)
     */
    public static class TestLocations {
        // Edmonton, Alberta
        public static final GeoPoint EDMONTON = new GeoPoint(53.5461, -113.4938);

        // Calgary, Alberta
        public static final GeoPoint CALGARY = new GeoPoint(51.0447, -114.0719);

        // Toronto, Ontario
        public static final GeoPoint TORONTO = new GeoPoint(43.6532, -79.3832);

        // Vancouver, BC
        public static final GeoPoint VANCOUVER = new GeoPoint(49.2827, -123.1207);

        // Montreal, Quebec
        public static final GeoPoint MONTREAL = new GeoPoint(45.5017, -73.5673);

        // New York City
        public static final GeoPoint NEW_YORK = new GeoPoint(40.7128, -74.0060);
    }
}