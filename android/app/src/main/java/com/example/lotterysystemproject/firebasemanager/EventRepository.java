package com.example.lotterysystemproject.firebasemanager;

import android.net.Uri;

import com.example.lotterysystemproject.models.Event;

import java.util.List;

public interface EventRepository {

    // Create event
    void createEvent(Event event, RepositoryCallback<String> callback);
    // returns eventId

    // Update event
    void updateEvent(Event event, RepositoryCallback<Void> callback);

    // Remove event (admin)
    void deleteEvent(String eventId, RepositoryCallback<Void> callback);

    // Get event by id
    void getEvent(String eventId, RepositoryCallback<Event> callback);

    // Browse events
    void getAllEvents(RepositoryCallback<List<Event>> callback);

    // Filter events by interest/availability
    void filterEvents(String interest, long startTime, long endTime,
                      RepositoryCallback<List<Event>> callback);

    // Upload event poster
    void uploadEventPoster(String eventId, Uri imageUri,
                           RepositoryCallback<String> callback);
    // returns download URL

    // Remove event poster (admin)
    void deleteEventPoster(String eventId, RepositoryCallback<Void> callback);

    // Enable/disable geolocation
    void setGeolocationRequired(String eventId, boolean required,
                                RepositoryCallback<Void> callback);

    // Generate QR code target URL
    void getEventQrCodeUrl(String eventId, RepositoryCallback<String> callback);

    void listenToEvent(String eventId,
                       RepositoryListener<Event> listener);

    // Check if registration is open
    void isRegistrationOpen(String eventId,
                            RepositoryCallback<Boolean> callback);

    // Batch operations (admin cleanup)
    void deleteEventsByOrganizer(String organizerId,
                                 RepositoryCallback<Void> callback);

    // Get organizer's events
    void getOrganizerEvents(String organizerId,
                            RepositoryCallback<List<Event>> callback);

    // Search events (better UX)
    void searchEvents(String query,
                      RepositoryCallback<List<Event>> callback);

    // Opt-out from notifications
    void optOutOfNotifications(String userId,
                               RepositoryCallback<Void> callback);

    // Get user's notification preferences
    void getNotificationPreferences(String userId,
                                    RepositoryCallback<Boolean> callback);

    // Send custom notification (organizer to entrant)
    void sendCustomNotification(String userId, String title, String message,
                                RepositoryCallback<Void> callback);

    // Batch notification send (more efficient)
    void sendBulkNotifications(List<String> userIds, String title, String message,
                               RepositoryCallback<Void> callback);
}

