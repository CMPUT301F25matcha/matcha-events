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

    private CollectionReference notifications() {
        return db.collection("notifications");
    }

    private DocumentReference userDoc(String userId) {
        return db.collection("users").document(userId);
    }

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

    @Override
    public void listenUserNotifications(String userId,
                                        RepositoryListener<List<NotificationItem>> listener) {
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

                    // Fetch notifications by their IDs
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
                                // sort by timestamp desc
                                Collections.sort(items, Comparator.comparingLong(NotificationItem::getTimestamp).reversed());

                                if (listener != null) listener.onDataChanged(items);
                            })
                            .addOnFailureListener(e -> {
                                if (listener != null) listener.onError(e);
                            });
                });
    }

    @Override
    public void deleteNotification(String userId,
                                   String notificationId,
                                   RepositoryCallback<Void> callback) {

        // First: remove id from the user's notifications array
        userDoc(userId)
                .update("notifications", FieldValue.arrayRemove(notificationId))
                .addOnSuccessListener(v -> {
                    // Second: delete the notification document itself
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

    @Override
    public void stopListeningUserNotifications() {
        if (userRegistration != null) {
            userRegistration.remove();
            userRegistration = null;
        }
    }

    // HELPERS

    @SuppressWarnings("unchecked")
    private List<String> getNotificationIdList(DocumentSnapshot doc) {
        Object raw = doc.get("notifications");
        if (raw instanceof List) {
            return new ArrayList<>((List<String>) raw);
        }
        return new ArrayList<>();
    }

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
                    Collections.sort(items, Comparator.comparingLong(NotificationItem::getTimestamp).reversed());
                    if (callback != null) callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

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
        // Persist decision
        if (item.getDecision() != null) {
            data.put("decision", item.getDecision().name());
        }

        return data;
    }

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
