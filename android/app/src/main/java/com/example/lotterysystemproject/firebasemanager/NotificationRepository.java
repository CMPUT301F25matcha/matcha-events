package com.example.lotterysystemproject.firebasemanager;

import com.example.lotterysystemproject.models.NotificationItem;

import java.util.List;


/**
 * Abstraction layer for all notification-related data operations.
 */
public interface NotificationRepository {

    /**
     * Persists a new notification for the given user.
     *
     * @param userId id of the user who will receive the notification
     * @param item populated NotificationItem to store
     * @param callback callback invoked when write succeeds or fails
     */
    void createNotification(String userId,
                            NotificationItem item,
                            RepositoryCallback<Void> callback);

    /**
     * Fetch of all notifications for a given user.
     *
     * @param userId id of the user whose notifications should be fetched
     * @param callback callback returning a list of NotificationItem or an error
     */
    void getNotificationsForUser(String userId,
                                 RepositoryCallback<List<NotificationItem>> callback);

    /**
     * Subscribes to real-time changes in a user's notifications.
     *
     * @param userId id of the user whose notifications will be observed
     * @param listener listener that receives updated lists or errors
     */
    void listenUserNotifications(String userId,
                                 RepositoryListener<List<NotificationItem>> listener);

    /**
     * Deletes a single notification and cleans up any user references to it.
     *
     * @param userId id of user who owned the notification
     * @param notificationId id of notification to delete
     * @param callback callback invoked when deletion is complete or fails
     */
    void deleteNotification(String userId,
                            String notificationId,
                            RepositoryCallback<Void> callback);

    /**
     * Stops any active real-time listener
     */
    void stopListeningUserNotifications();

    void getAllNotifications(RepositoryCallback<List<NotificationItem>> callback);
}
