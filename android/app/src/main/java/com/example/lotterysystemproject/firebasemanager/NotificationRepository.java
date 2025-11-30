package com.example.lotterysystemproject.firebasemanager;

import com.example.lotterysystemproject.models.NotificationItem;

import java.util.List;

public interface NotificationRepository {

    void createNotification(String userId,
                            NotificationItem item,
                            RepositoryCallback<Void> callback);

    void getNotificationsForUser(String userId,
                                 RepositoryCallback<List<NotificationItem>> callback);

    void listenUserNotifications(String userId,
                                 RepositoryListener<List<NotificationItem>> listener);

    void deleteNotification(String userId,
                            String notificationId,
                            RepositoryCallback<Void> callback);

    void stopListeningUserNotifications();

    void getAllNotifications(RepositoryCallback<List<NotificationItem>> callback);
}
