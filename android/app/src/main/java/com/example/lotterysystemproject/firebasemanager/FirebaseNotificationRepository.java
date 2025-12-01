package com.example.lotterysystemproject.firebasemanager;

import androidx.annotation.Nullable;

import com.example.lotterysystemproject.models.NotificationItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Firestore implementation for notifications
 * Each user document has [notificationId1, notificationId2, ...]
 */
public class FirebaseNotificationRepository implements NotificationRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Nullable
    private ListenerRegistration userRegistration;

    /** @return reference to top-level "notifications" collection. */
    private CollectionReference notifications() {
        return db.collection("notifications");
    }

    /**
     * Gets a reference to the user document.
     *
     * @param userId user document ID
     * @return document reference for that user
     */
    private DocumentReference userDoc(String userId) {
        return db.collection("users").document(userId);
    }

    /**
     * Creates and persists a notification for the given user.
     *
     * @param userId user receiving the notification
     * @param item notification to store
     * @param callback callback for success/failure
     */
    @Override
    public void createNotification(String userId,
                                   NotificationItem item,
                                   RepositoryCallback<Void> callback) {

        if (userId == null || userId.trim().isEmpty()) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("userId is required"));
            }
            return;
        }

        // Ensure NotificationItem knows its user and type
        item.setUserId(userId);

        // Use existing id or let Firestore create one
        DocumentReference docRef;
        if (item.getId() == null || item.getId().trim().isEmpty()) {
            docRef = notifications().document(); // auto-id
        } else {
            docRef = notifications().document(item.getId());
        }

        Map<String, Object> data = toMapForWrite(docRef.getId(), userId, item);

        // Write notification document
        docRef.set(data)
                .addOnSuccessListener(v -> {
                    // Add notification id to user's notifications array
                    userDoc(userId)
                            .update("notifications", FieldValue.arrayUnion(docRef.getId()))
                            .addOnSuccessListener(u -> {
                                if (callback != null) callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Fetches notifications for the given user based on the notifications array stored in the user document.
     *
     * @param userId user whose notifications should be loaded
     * @param callback invoked with a sorted list or error
     */
    @Override
    public void getNotificationsForUser(String userId,
                                        RepositoryCallback<List<NotificationItem>> callback) {

        userDoc(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> ids = getNotificationIdList(doc);
                    if (ids.isEmpty()) {
                        if (callback != null) callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    fetchNotificationsByIds(ids, callback);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Attaches a listener to the given user's document and observes changes to notifications array.
     *
     * @param userId user to listen for
     * @param listener listener that receives updated notification lists or errors
     */
    @Override
    public void listenUserNotifications(String userId,
                                        RepositoryListener<List<NotificationItem>> listener) {
        // Ensure only one listener active at a time
        stopListeningUserNotifications();

        userRegistration = userDoc(userId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        if (listener != null) listener.onError(error);
                        return;
                    }

                    if (doc == null || !doc.exists()) {
                        if (listener != null) listener.onDataChanged(Collections.emptyList());
                        return;
                    }

                    List<String> ids = getNotificationIdList(doc);
                    if (ids.isEmpty()) {
                        if (listener != null) listener.onDataChanged(Collections.emptyList());
                        return;
                    }

                    // Optionally limit number of notifications pulled
                    List<String> limitedIds = ids.size() > 10
                            ? ids.subList(ids.size() - 10, ids.size())
                            : ids;

                    notifications()
                            .whereIn(FieldPath.documentId(), limitedIds)
                            .get()
                            .addOnSuccessListener(snap -> {
                                List<NotificationItem> items = new ArrayList<>();
                                for (DocumentSnapshot nDoc : snap.getDocuments()) {
                                    NotificationItem item = fromDoc(nDoc);
                                    if (item != null) items.add(item);
                                }
                                // sort by timestamp desc, newest first
                                Collections.sort(items, Comparator.comparingLong(NotificationItem::getTimestamp).reversed());

                                if (listener != null) listener.onDataChanged(items);
                            })
                            .addOnFailureListener(e -> {
                                if (listener != null) listener.onError(e);
                            });
                });
    }

    /**
     * Deletes a notification document and removes its ID from the user's notifications array.
     *
     * @param userId user whose notifications list should be updated
     * @param notificationId ID of notification to remove
     * @param callback invoked when both operations complete or upon error
     */
    @Override
    public void deleteNotification(String userId,
                                   String notificationId,
                                   RepositoryCallback<Void> callback) {

        // First: remove id from user notifications array
        userDoc(userId)
                .update("notifications", FieldValue.arrayRemove(notificationId))
                .addOnSuccessListener(v -> {
                    // Second: delete notification document itself
                    notifications()
                            .document(notificationId)
                            .delete()
                            .addOnSuccessListener(v2 -> {
                                if (callback != null) callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    // Report if fail to update user doc
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Stops the active user notifications listener (if any)
     * Should be called from the Activity/Fragment lifecycle.
     */
    @Override
    public void stopListeningUserNotifications() {
        if (userRegistration != null) {
            userRegistration.remove();
            userRegistration = null;
        }
    }

    // HELPERS

    /**
     * Extracts list of notification IDs from a user document.
     *
     * @param doc snapshot of the user document
     * @return copy of the stored notifications list
     */
    @SuppressWarnings("unchecked")
    private List<String> getNotificationIdList(DocumentSnapshot doc) {
        Object raw = doc.get("notifications");
        if (raw instanceof List) {
            return new ArrayList<>((List<String>) raw);
        }
        return new ArrayList<>();
    }

    /**
     * Fetches all NotificationItem instances identified by given ID list.
     *
     * @param ids full list of notification IDs from the user document
     * @param callback callback with sorted items or error
     */
    private void fetchNotificationsByIds(List<String> ids,
                                         RepositoryCallback<List<NotificationItem>> callback) {

        List<String> limitedIds = ids.size() > 10
                ? ids.subList(ids.size() - 10, ids.size())
                : ids;

        notifications()
                .whereIn(FieldPath.documentId(), limitedIds)
                .get()
                .addOnSuccessListener(snap -> {
                    List<NotificationItem> items = new ArrayList<>();
                    for (DocumentSnapshot nDoc : snap.getDocuments()) {
                        NotificationItem item = fromDoc(nDoc);
                        if (item != null) items.add(item);
                    }
                    // Sort by timestamp descending
                    Collections.sort(items, Comparator.comparingLong(NotificationItem::getTimestamp).reversed());
                    if (callback != null) callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Converts a NotificationItem into a Firestore-friendly map.
     * Place to define the notification document schema.
     *
     * @param id document ID
     * @param userId receiving user id
     * @param item source model object
     * @return map ready for DocumentReference(Object)
     */
    private Map<String, Object> toMapForWrite(String id,
                                              String userId,
                                              NotificationItem item) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("userId", userId);
        data.put("title", item.getTitle());
        data.put("message", item.getMessage());
        data.put("timestamp", item.getTimestamp());

        if (item.getNotificationType() != null) {
            data.put("notificationType", item.getNotificationType().name());
        }
        if (item.getOrganizerId() != null) {
            data.put("organizerId", item.getOrganizerId());
        }
        // Persist decision for invitation-type notifications
        if (item.getDecision() != null) {
            data.put("decision", item.getDecision().name());
        }
        return data;
    }

    /**
     * Converts a Firestore document in notifications into a NotificationItem instance.
     *
     * @param doc Firestore document snapshot
     * @return a populated NotificationItem or null if required data missing
     */
    @Nullable
    private NotificationItem fromDoc(DocumentSnapshot doc) {
        String id = doc.getString("id");
        if (id == null || id.trim().isEmpty()) {
            id = doc.getId();
        }

        String title = doc.getString("title");
        String message = doc.getString("message");
        Long ts = doc.getLong("timestamp");
        String typeStr = doc.getString("notificationType");
        String organizerId = doc.getString("organizerId");
        String userId = doc.getString("userId");
        String decisionStr = doc.getString("decision");

        if (title == null) title = "";
        if (message == null) message = "";
        if (ts == null) ts = System.currentTimeMillis();

        NotificationItem.NotificationType type = null;
        if (typeStr != null) {
            try {
                type = NotificationItem.NotificationType.valueOf(typeStr);
            } catch (IllegalArgumentException ignored) {}
        }

        NotificationItem.Decision decision = NotificationItem.Decision.NONE;
        if (decisionStr != null) {
            try {
                decision = NotificationItem.Decision.valueOf(decisionStr);
            } catch (IllegalArgumentException ignored) {}
        }

        NotificationItem item = new NotificationItem(
                id,
                type,
                organizerId,
                userId,
                title,
                message,
                ts
        );
        item.setDecision(decision);
        return item;
    }
}
