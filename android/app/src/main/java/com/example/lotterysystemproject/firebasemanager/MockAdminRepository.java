package com.example.lotterysystemproject.firebasemanager;

import android.util.Log;

import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.models.NotificationLog;
import com.example.lotterysystemproject.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Mock implementation of AdminRepository for testing
 * Stores admin operations in memory with audit trail
 */
public class MockAdminRepository implements AdminRepository {

    private static final String TAG = "MockAdminRepo";

    // Storage
    private final Map<String, Event> events = new ConcurrentHashMap<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> images = new ConcurrentHashMap<>();
    private final List<NotificationLog> notificationLogs = new ArrayList<>();
    private final List<String> adminAuditLog = new ArrayList<>();

    private boolean simulateDelay = false;
    private long delayMs = 100;
    private boolean simulateFailure = false;

    public MockAdminRepository() {
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
        events.clear();
        users.clear();
        images.clear();
        notificationLogs.clear();
        adminAuditLog.clear();
    }

    // ========================================================================
    // REMOVE EVENT
    // ========================================================================

    @Override
    public void removeEvent(String eventId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated admin operation failure"));
                    return;
                }

                Event event = events.get(eventId);
                if (event == null) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                // Soft delete
                event.setActive(false);
                event.setStatus("cancelled");
                events.put(eventId, event);

                // Log action
                logAdminAction("REMOVE_EVENT", eventId,
                        "Removed event: " + event.getName());

                Log.d(TAG, "Removed event: " + eventId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // REMOVE PROFILE
    // ========================================================================

    @Override
    public void removeProfile(String userId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated admin operation failure"));
                    return;
                }

                User user = users.get(userId);
                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                // Soft delete
                user.setActive(false);
                users.put(userId, user);

                // Log action
                logAdminAction("REMOVE_PROFILE", userId,
                        "Removed profile: " + user.getName());

                Log.d(TAG, "Removed profile: " + userId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // REMOVE IMAGE
    // ========================================================================

    @Override
    public void removeImage(String imageUrl, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated admin operation failure"));
                    return;
                }

                if (imageUrl == null || imageUrl.isEmpty()) {
                    callback.onFailure(new Exception("Invalid image URL"));
                    return;
                }

                // Remove from storage
                images.remove(imageUrl);

                // Remove from events using this image
                for (Event event : events.values()) {
                    if (imageUrl.equals(event.getEventPosterUrl())) {
                        event.setEventPosterUrl(null);
                        events.put(event.getEventId(), event);
                    }
                }

                // Log action
                logAdminAction("REMOVE_IMAGE", imageUrl, "Removed image from storage");

                Log.d(TAG, "Removed image: " + imageUrl);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // BROWSE EVENTS
    // ========================================================================

    @Override
    public void getAllEventsAdmin(RepositoryCallback<List<Event>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated admin operation failure"));
                    return;
                }

                // Admin sees ALL events (including inactive)
                List<Event> allEvents = new ArrayList<>(events.values());
                allEvents.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

                Log.d(TAG, "Retrieved " + allEvents.size() + " events");
                callback.onSuccess(allEvents);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // BROWSE PROFILES
    // ========================================================================

    @Override
    public void getAllProfiles(RepositoryCallback<List<User>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated admin operation failure"));
                    return;
                }

                // Admin sees ALL profiles (including inactive)
                List<User> allUsers = new ArrayList<>(users.values());
                allUsers.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

                Log.d(TAG, "Retrieved " + allUsers.size() + " profiles");
                callback.onSuccess(allUsers);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // REMOVE ORGANIZER
    // ========================================================================

    @Override
    public void removeOrganizer(String userId, String reason,
                                RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated admin operation failure"));
                    return;
                }

                User user = users.get(userId);
                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                if (!user.isOrganizer()) {
                    callback.onFailure(new Exception("User is not an organizer"));
                    return;
                }

                // Downgrade to entrant
                user.setRole("entrant");
                users.put(userId, user);

                // Cancel all their events
                for (Event event : events.values()) {
                    if (event.getOrganizerId().equals(userId) && event.isActive()) {
                        event.setActive(false);
                        event.setStatus("cancelled");
                        events.put(event.getEventId(), event);
                    }
                }

                // Log action with reason
                logAdminAction("REMOVE_ORGANIZER", userId,
                        "Removed organizer: " + user.getName() + ". Reason: " + reason);

                Log.d(TAG, "Removed organizer: " + userId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // NOTIFICATION AUDIT LOG
    // ========================================================================

    @Override
    public void getNotificationAuditLog(RepositoryCallback<List<NotificationLog>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated admin operation failure"));
                    return;
                }

                List<NotificationLog> logs = new ArrayList<>(notificationLogs);
                logs.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                Log.d(TAG, "Retrieved " + logs.size() + " notification logs");
                callback.onSuccess(logs);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void logAdminAction(String action, String targetId, String description) {
        String logEntry = String.format("[%d] %s | Target: %s | %s",
                System.currentTimeMillis(),
                action,
                targetId,
                description);

        adminAuditLog.add(logEntry);
        Log.d(TAG, "Admin action logged: " + action);
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
    // TEST HELPER METHODS - Data Setup
    // ========================================================================

    public void addEvent(Event event) {
        events.put(event.getEventId(), event);
    }

    public void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public void addImage(String imageUrl) {
        images.put(imageUrl, imageUrl);
    }

    public void addNotificationLog(NotificationLog log) {
        notificationLogs.add(log);
    }

    // ========================================================================
    // TEST HELPER METHODS - Verification
    // ========================================================================

    public Event getEvent(String eventId) {
        return events.get(eventId);
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public boolean imageExists(String imageUrl) {
        return images.containsKey(imageUrl);
    }

    public int getEventCount() {
        return events.size();
    }

    public int getUserCount() {
        return users.size();
    }

    public int getActiveEventCount() {
        return (int) events.values().stream()
                .filter(Event::isActive)
                .count();
    }

    public int getActiveUserCount() {
        return (int) users.values().stream()
                .filter(User::isActive)
                .count();
    }

    public List<String> getAdminAuditLog() {
        return new ArrayList<>(adminAuditLog);
    }

    public int getAuditLogCount() {
        return adminAuditLog.size();
    }

    public boolean wasActionLogged(String action, String targetId) {
        return adminAuditLog.stream()
                .anyMatch(log -> log.contains(action) && log.contains(targetId));
    }

    /**
     * Get all cancelled events by a specific organizer
     */
    public List<Event> getCancelledEventsByOrganizer(String organizerId) {
        return events.values().stream()
                .filter(e -> e.getOrganizerId().equals(organizerId))
                .filter(e -> "cancelled".equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Get all events using a specific image
     */
    public List<Event> getEventsUsingImage(String imageUrl) {
        return events.values().stream()
                .filter(e -> imageUrl.equals(e.getEventPosterUrl()))
                .collect(Collectors.toList());
    }

    /**
     * Check if a user's role was downgraded
     */
    public boolean wasOrganizerDowngraded(String userId) {
        User user = users.get(userId);
        return user != null && "entrant".equals(user.getRole());
    }
}
