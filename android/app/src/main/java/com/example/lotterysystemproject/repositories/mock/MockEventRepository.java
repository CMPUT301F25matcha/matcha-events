package com.example.lotterysystemproject.repositories.mock;

import android.net.Uri;
import android.util.Log;

import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.repositories.EventRepository;
import com.example.lotterysystemproject.repositories.RepositoryCallback;
import com.example.lotterysystemproject.repositories.RepositoryListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Mock implementation of EventRepository for testing
 * Stores events in memory with image upload simulation
 */
public class MockEventRepository implements EventRepository {

    private static final String TAG = "MockEventRepo";

    // Storage
    private final Map<String, Event> events = new ConcurrentHashMap<>();
    private final Map<String, String> eventPosters = new ConcurrentHashMap<>(); // eventId -> imageUrl
    private final Map<String, RepositoryListener<Event>> eventListeners = new ConcurrentHashMap<>();

    private boolean simulateDelay = false;
    private long delayMs = 100;
    private boolean simulateFailure = false;

    public MockEventRepository() {
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
        eventPosters.clear();
        eventListeners.clear();
    }

    // ========================================================================
    // CREATE EVENT (US 02.01.01)
    // ========================================================================

    @Override
    public void createEvent(Event event, RepositoryCallback<String> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated event creation failure"));
                    return;
                }

                if (event == null || event.getEventId() == null) {
                    callback.onFailure(new Exception("Invalid event data"));
                    return;
                }

                // Set timestamps
                long now = System.currentTimeMillis();
                event.setCreatedAt(now);

                // Set default values
                if (event.getStatus() == null) {
                    event.setStatus("open");
                }
                event.setActive(true);

                // Initialize counts
                if (event.getCurrentEnrollmentCount() == 0) {
                    event.setCurrentEnrollmentCount(0);
                }
                if (event.getCurrentWaitingCount() == 0) {
                    event.setCurrentWaitingCount(0);
                }

                // Generate QR code URL
                String qrCodeUrl = "https://app.example.com/event/" + event.getEventId();
                event.setPromotionalQrCode(qrCodeUrl);

                // Store event
                events.put(event.getEventId(), event);

                Log.d(TAG, "Created event: " + event.getEventId());
                callback.onSuccess(event.getEventId());

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // UPDATE EVENT
    // ========================================================================

    @Override
    public void updateEvent(Event event, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated event update failure"));
                    return;
                }

                if (event == null || event.getEventId() == null) {
                    callback.onFailure(new Exception("Invalid event data"));
                    return;
                }

                if (!events.containsKey(event.getEventId())) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                // Update timestamp
                //TODO: Check if we need updated attribute
//                event.setUpdatedAt(System.currentTimeMillis());

                // Store updated event
                events.put(event.getEventId(), event);

                // Notify listener
                notifyListener(event.getEventId(), event);

                Log.d(TAG, "Updated event: " + event.getEventId());
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // DELETE EVENT (Admin)
    // ========================================================================

    @Override
    public void deleteEvent(String eventId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated event deletion failure"));
                    return;
                }

                if (!events.containsKey(eventId)) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                // Hard delete
                events.remove(eventId);
                eventPosters.remove(eventId);

                Log.d(TAG, "Deleted event: " + eventId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // GET EVENT BY ID
    // ========================================================================

    @Override
    public void getEvent(String eventId, RepositoryCallback<Event> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated event fetch failure"));
                    return;
                }

                Event event = events.get(eventId);

                if (event == null) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                Log.d(TAG, "Retrieved event: " + eventId);
                callback.onSuccess(event);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // GET ALL EVENTS (US 01.01.03)
    // ========================================================================

    @Override
    public void getAllEvents(RepositoryCallback<List<Event>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated events fetch failure"));
                    return;
                }

                // Only return active events
                List<Event> activeEvents = events.values().stream()
                        .filter(Event::isActive)
                        .filter(e -> "open".equals(e.getStatus()))
                        .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
                        .collect(Collectors.toList());

                Log.d(TAG, "Retrieved " + activeEvents.size() + " active events");
                callback.onSuccess(activeEvents);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // FILTER EVENTS (US 01.01.04)
    // ========================================================================

    @Override
    public void filterEvents(String interest, long startTime, long endTime,
                             RepositoryCallback<List<Event>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated filter failure"));
                    return;
                }

                List<Event> filtered = events.values().stream()
                        .filter(Event::isActive)
                        .filter(e -> "open".equals(e.getStatus()))
                        .filter(e -> {
                            // Filter by interest (if provided)
                            if (interest != null && !interest.isEmpty()) {
                                return e.getName().toLowerCase().contains(interest.toLowerCase()) ||
                                        (e.getDescription() != null &&
                                                e.getDescription().toLowerCase().contains(interest.toLowerCase()));
                            }
                            return true;
                        })
                        .filter(e -> {
                            // Filter by time range
                            if (startTime > 0 && endTime > 0) {
                                return e.getStartDate().getTime() >= startTime && e.getStartDate().getTime() <= endTime;
                            }
                            return true;
                        })
                        .sorted((a, b) -> Long.compare(a.getStartDate().getTime(), b.getStartDate().getTime()))
                        .collect(Collectors.toList());

                Log.d(TAG, "Filtered events: " + filtered.size() + " results");
                callback.onSuccess(filtered);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // UPLOAD EVENT POSTER (US 02.04.01)
    // ========================================================================

    @Override
    public void uploadEventPoster(String eventId, Uri imageUri,
                                  RepositoryCallback<String> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated upload failure"));
                    return;
                }

                Event event = events.get(eventId);

                if (event == null) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                // Simulate upload by generating URL
                String imageUrl = "mock://event_posters/" + eventId + ".jpg";

                eventPosters.put(eventId, imageUrl);
                event.setEventPosterUrl(imageUrl);
//                event.setUpdatedAt(System.currentTimeMillis());
                events.put(eventId, event);

                notifyListener(eventId, event);

                Log.d(TAG, "Uploaded poster for event: " + eventId);
                callback.onSuccess(imageUrl);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // DELETE EVENT POSTER (US 02.04.02, Admin)
    // ========================================================================

    @Override
    public void deleteEventPoster(String eventId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated delete failure"));
                    return;
                }

                Event event = events.get(eventId);

                if (event == null) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                eventPosters.remove(eventId);
                event.setEventPosterUrl(null);
//                event.setUpdatedAt(System.currentTimeMillis());
                events.put(eventId, event);

                notifyListener(eventId, event);

                Log.d(TAG, "Deleted poster for event: " + eventId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // GEOLOCATION (US 02.02.03)
    // ========================================================================

    @Override
    public void setGeolocationRequired(String eventId, boolean required,
                                       RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated update failure"));
                    return;
                }

                Event event = events.get(eventId);

                if (event == null) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                event.setGeolocationRequired(required);
//                event.setUpdatedAt(System.currentTimeMillis());
                events.put(eventId, event);

                notifyListener(eventId, event);

                Log.d(TAG, "Set geolocation for event " + eventId + ": " + required);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // QR CODE (US 02.01.01)
    // ========================================================================

    @Override
    public void getEventQrCodeUrl(String eventId, RepositoryCallback<String> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated QR fetch failure"));
                    return;
                }

                Event event = events.get(eventId);

                if (event == null) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                String qrUrl = event.getPromotionalQrCode();
                if (qrUrl == null) {
                    qrUrl = "https://app.example.com/event/" + eventId;
                }

                callback.onSuccess(qrUrl);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // REAL-TIME LISTENER
    // ========================================================================

    @Override
    public void listenToEvent(String eventId, RepositoryListener<Event> listener) {
        eventListeners.put(eventId, listener);

        // Immediately send current data
        Event event = events.get(eventId);
        if (event != null) {
            listener.onDataChanged(event);
        } else {
            listener.onError(new Exception("Event not found"));
        }
    }

    public void removeListener(String eventId) {
        eventListeners.remove(eventId);
    }

    public void removeAllListeners() {
        eventListeners.clear();
    }

    // ========================================================================
    // REGISTRATION STATUS CHECK
    // ========================================================================

    @Override
    public void isRegistrationOpen(String eventId, RepositoryCallback<Boolean> callback) {
        executeAsync(() -> {
            try {
                Event event = events.get(eventId);

                if (event == null) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                long now = System.currentTimeMillis();
                boolean isOpen = event.isActive() &&
                        "open".equals(event.getStatus()) &&
                        now >= event.getRegistrationStart().getTime() &&
                        now <= event.getRegistrationEnd().getTime();

                callback.onSuccess(isOpen);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // BATCH OPERATIONS
    // ========================================================================

    @Override
    public void deleteEventsByOrganizer(String organizerId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated batch delete failure"));
                    return;
                }

                List<String> toDelete = events.values().stream()
                        .filter(e -> e.getOrganizerId().equals(organizerId))
                        .map(Event::getEventId)
                        .collect(Collectors.toList());

                for (String eventId : toDelete) {
                    events.remove(eventId);
                    eventPosters.remove(eventId);
                }

                Log.d(TAG, "Deleted " + toDelete.size() + " events for organizer " + organizerId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // GET ORGANIZER'S EVENTS
    // ========================================================================

    @Override
    public void getOrganizerEvents(String organizerId, RepositoryCallback<List<Event>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated fetch failure"));
                    return;
                }

                List<Event> organizerEvents = events.values().stream()
                        .filter(e -> e.getOrganizerId().equals(organizerId))
                        .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
                        .collect(Collectors.toList());

                Log.d(TAG, "Retrieved " + organizerEvents.size() +
                        " events for organizer " + organizerId);
                callback.onSuccess(organizerEvents);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // SEARCH EVENTS
    // ========================================================================

    @Override
    public void searchEvents(String query, RepositoryCallback<List<Event>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated search failure"));
                    return;
                }

                String lowerQuery = query.toLowerCase();

                List<Event> results = events.values().stream()
                        .filter(Event::isActive)
                        .filter(e ->
                                e.getName().toLowerCase().contains(lowerQuery) ||
                                        (e.getDescription() != null &&
                                                e.getDescription().toLowerCase().contains(lowerQuery)) ||
                                        (e.getLocation() != null &&
                                                e.getLocation().toLowerCase().contains(lowerQuery)))
                        .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
                        .collect(Collectors.toList());

                Log.d(TAG, "Search for '" + query + "' found " + results.size() + " events");
                callback.onSuccess(results);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // NOTIFICATION PREFERENCES (stub methods)
    // ========================================================================

    @Override
    public void optOutOfNotifications(String userId, RepositoryCallback<Void> callback) {
        // This is typically handled by UserRepository
        // Included here for interface compliance
        callback.onSuccess(null);
    }

    @Override
    public void getNotificationPreferences(String userId, RepositoryCallback<Boolean> callback) {
        // This is typically handled by UserRepository
        callback.onSuccess(true);
    }

    @Override
    public void sendCustomNotification(String userId, String title, String message,
                                       RepositoryCallback<Void> callback) {
        // This is typically handled by NotificationRepository
        callback.onSuccess(null);
    }

    @Override
    public void sendBulkNotifications(List<String> userIds, String title, String message,
                                      RepositoryCallback<Void> callback) {
        // This is typically handled by NotificationRepository
        callback.onSuccess(null);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void notifyListener(String eventId, Event event) {
        RepositoryListener<Event> listener = eventListeners.get(eventId);
        if (listener != null) {
            listener.onDataChanged(event);
        }
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

    public int getEventCount() {
        return events.size();
    }

    public Event getEventDirect(String eventId) {
        return events.get(eventId);
    }

    public void addEvent(Event event) {
        events.put(event.getEventId(), event);
    }

    public boolean eventExists(String eventId) {
        return events.containsKey(eventId);
    }

    public int getActiveEventCount() {
        return (int) events.values().stream()
                .filter(Event::isActive)
                .count();
    }

    public int getInactiveEventCount() {
        return (int) events.values().stream()
                .filter(e -> !e.isActive())
                .count();
    }

    public List<Event> getEventsByStatus(String status) {
        return events.values().stream()
                .filter(e -> status.equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    public boolean hasPoster(String eventId) {
        return eventPosters.containsKey(eventId);
    }

    public String getPosterUrl(String eventId) {
        return eventPosters.get(eventId);
    }

    public List<Event> getEventsRequiringGeolocation() {
        return events.values().stream()
                .filter(Event::isGeolocationRequired)
                .collect(Collectors.toList());
    }

    public List<Event> getEventsInDateRange(long startTime, long endTime) {
        return events.values().stream()
                .filter(e -> e.getStartDate().getTime() >= startTime && e.getStartDate().getTime() <= endTime)
                .sorted((a, b) -> Long.compare(a.getStartDate().getTime(), b.getStartDate().getTime()))
                .collect(Collectors.toList());
    }

    public List<Event> getEventsWithOpenRegistration() {
        long now = System.currentTimeMillis();
        return events.values().stream()
                .filter(Event::isActive)
                .filter(e -> "open".equals(e.getStatus()))
                .filter(e -> now >= e.getRegistrationStart().getTime() && now <= e.getRegistrationEnd().getTime())
                .collect(Collectors.toList());
    }
}
