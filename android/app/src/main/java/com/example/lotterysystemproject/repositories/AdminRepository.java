package com.example.matchamonday.repositories;

import com.example.matchamonday.models.Event;
import com.example.matchamonday.models.NotificationLog;
import com.example.matchamonday.models.User;

import java.util.List;

public interface AdminRepository {

    // Remove event
    void removeEvent(String eventId,
                     RepositoryCallback<Void> callback);

    // Remove profile
    void removeProfile(String userId,
                       RepositoryCallback<Void> callback);

    // Remove image
    void removeImage(String imageUrl,
                     RepositoryCallback<Void> callback);

    // Browse all events
    void getAllEventsAdmin(RepositoryCallback<List<Event>> callback);

    // Browse all profiles
    void getAllProfiles(RepositoryCallback<List<User>> callback);

    // Remove organizer (violates policy)
    void removeOrganizer(String userId, String reason,
                         RepositoryCallback<Void> callback);

    // Get notification audit log
    void getNotificationAuditLog(RepositoryCallback<List<NotificationLog>> callback);
}
