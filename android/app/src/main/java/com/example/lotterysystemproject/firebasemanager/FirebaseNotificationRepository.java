package com.example.lotterysystemproject.firebasemanager;

import android.util.Log;

import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.models.NotificationLog;
import com.example.lotterysystemproject.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all notification operations
 * Uses Firebase Cloud Messaging (FCM) for push notifications
 * Maintains audit log for admin review (US 03.08.01)
 */
public class FirebaseNotificationRepository implements NotificationRepository {

    private static final String TAG = "NotificationRepo";
    private static final String COLLECTION_NOTIFICATION_LOGS = "notification_logs";
    private static final String COLLECTION_ENTRANTS = "entrants";
    private static final String COLLECTION_EVENTS = "events";
    private static final String COLLECTION_USERS = "users";

    private final FirebaseFirestore db;
    private final FirebaseMessaging messaging;
    private final RegistrationRepository registrationRepo;

    public FirebaseNotificationRepository(RegistrationRepository registrationRepo) {
        this.db = FirebaseFirestore.getInstance();
        this.messaging = FirebaseMessaging.getInstance();
        this.registrationRepo = registrationRepo;
    }

    public FirebaseNotificationRepository() {
        this(new FirebaseRegistrationRepository());
    }

    // ========================================================================
    // LOTTERY NOTIFICATIONS (US 01.04.01, US 01.04.02, US 02.05.01)
    // ========================================================================

    @Override
    public void notifySelectedEntrants(String eventId, List<String> entrantIds,
                                       RepositoryCallback<Void> callback) {
        Log.d(TAG, "notifySelectedEntrants: eventId=" + eventId +
                ", count=" + entrantIds.size());

        if (entrantIds.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        // Get event details for notification
        db.collection(COLLECTION_EVENTS).document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        callback.onFailure(new Exception("Event not found"));
                        return;
                    }

                    Event event = eventDoc.toObject(Event.class);
                    if (event == null) {
                        callback.onFailure(new Exception("Invalid event data"));
                        return;
                    }

                    // Get entrant details
                    getEntrantsByIds(entrantIds, new RepositoryCallback<List<Entrant>>() {
                        @Override
                        public void onSuccess(List<Entrant> entrants) {
                            // Get user details to check notification preferences
                            getUsersForEntrants(entrants, new RepositoryCallback<Map<String, User>>() {
                                @Override
                                public void onSuccess(Map<String, User> userMap) {
                                    sendWinnerNotifications(event, entrants, userMap, callback);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    callback.onFailure(e);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void notifyRejectedEntrants(String eventId, List<String> entrantIds,
                                       RepositoryCallback<Void> callback) {
        Log.d(TAG, "notifyRejectedEntrants: eventId=" + eventId +
                ", count=" + entrantIds.size());

        if (entrantIds.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        db.collection(COLLECTION_EVENTS).document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        callback.onFailure(new Exception("Event not found"));
                        return;
                    }

                    Event event = eventDoc.toObject(Event.class);
                    if (event == null) {
                        callback.onFailure(new Exception("Invalid event data"));
                        return;
                    }

                    getEntrantsByIds(entrantIds, new RepositoryCallback<List<Entrant>>() {
                        @Override
                        public void onSuccess(List<Entrant> entrants) {
                            getUsersForEntrants(entrants, new RepositoryCallback<Map<String, User>>() {
                                @Override
                                public void onSuccess(Map<String, User> userMap) {
                                    sendRejectionNotifications(event, entrants, userMap, callback);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    callback.onFailure(e);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ========================================================================
    // ORGANIZER BULK NOTIFICATIONS (US 02.07.01, 02.07.02, 02.07.03)
    // ========================================================================

    @Override
    public void notifyWaitingList(String eventId, RepositoryCallback<Void> callback) {
        Log.d(TAG, "notifyWaitingList: eventId=" + eventId);

        registrationRepo.getWaitingList(eventId, new RepositoryCallback<List<Entrant>>() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                if (entrants.isEmpty()) {
                    callback.onSuccess(null);
                    return;
                }

                List<String> entrantIds = new ArrayList<>();
                for (Entrant e : entrants) {
                    entrantIds.add(e.getEntrantId());
                }

                sendCustomOrganizerNotification(eventId, entrantIds,
                        "Waiting List Update",
                        "New update about your event registration",
                        callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void notifyChosen(String eventId, RepositoryCallback<Void> callback) {
        Log.d(TAG, "notifyChosen: eventId=" + eventId);

        registrationRepo.getSelectedEntrants(eventId, new RepositoryCallback<List<Entrant>>() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                if (entrants.isEmpty()) {
                    callback.onSuccess(null);
                    return;
                }

                List<String> entrantIds = new ArrayList<>();
                for (Entrant e : entrants) {
                    entrantIds.add(e.getEntrantId());
                }

                sendCustomOrganizerNotification(eventId, entrantIds,
                        "Selected Entrant Update",
                        "Important update about your event participation",
                        callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void notifyCancelled(String eventId, RepositoryCallback<Void> callback) {
        Log.d(TAG, "notifyCancelled: eventId=" + eventId);

        registrationRepo.getCancelledEntrants(eventId, new RepositoryCallback<List<Entrant>>() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                if (entrants.isEmpty()) {
                    callback.onSuccess(null);
                    return;
                }

                List<String> entrantIds = new ArrayList<>();
                for (Entrant e : entrants) {
                    entrantIds.add(e.getEntrantId());
                }

                sendCustomOrganizerNotification(eventId, entrantIds,
                        "Cancelled Registration Update",
                        "Information about your cancelled registration",
                        callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // ADMIN AUDIT LOG (US 03.08.01)
    // ========================================================================

    @Override
    public void getNotificationLog(RepositoryCallback<List<String>> callback) {
        Log.d(TAG, "getNotificationLog");

        db.collection(COLLECTION_NOTIFICATION_LOGS)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> logs = new ArrayList<>();

                    querySnapshot.forEach(doc -> {
                        NotificationLog log = doc.toObject(NotificationLog.class);
                        if (log != null) {
                            logs.add(log.toString());
                        }
                    });

                    Log.d(TAG, "Retrieved " + logs.size() + " notification logs");
                    callback.onSuccess(logs);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get notification logs", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private void sendWinnerNotifications(Event event, List<Entrant> entrants,
                                         Map<String, User> userMap,
                                         RepositoryCallback<Void> callback) {
        List<NotificationLog> logs = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (Entrant entrant : entrants) {
            User user = userMap.get(entrant.getUserId());

            // Check if user has opted out (US 01.04.03)
            if (user != null && user.isNotificationsEnabled()) {
                String title = "🎉 You've Been Selected!";
                String body = "Congratulations! You've been selected for " + event.getName() +
                        ". Please accept or decline your invitation.";

                boolean sent = sendPushNotification(
                        entrant.getUserId(),
                        title,
                        body,
                        createNotificationData(event.getEventId(), "SELECTED")
                );

                if (sent) {
                    successCount++;
                    logs.add(createLog(event, entrant, "LOTTERY_WIN", title, body, true));
                } else {
                    failCount++;
                    logs.add(createLog(event, entrant, "LOTTERY_WIN", title, body, false));
                }
            }
        }

        // Save logs to Firestore for audit trail
        saveNotificationLogs(logs);

        Log.d(TAG, "Sent " + successCount + " winner notifications, " +
                failCount + " failed");
        callback.onSuccess(null);
    }

    private void sendRejectionNotifications(Event event, List<Entrant> entrants,
                                            Map<String, User> userMap,
                                            RepositoryCallback<Void> callback) {
        List<NotificationLog> logs = new ArrayList<>();
        int successCount = 0;

        for (Entrant entrant : entrants) {
            User user = userMap.get(entrant.getUserId());

            if (user != null && user.isNotificationsEnabled()) {
                String title = "Lottery Update";
                String body = "You weren't selected this time for " + event.getName() +
                        ", but you're still on the waiting list if spots open up!";

                boolean sent = sendPushNotification(
                        entrant.getUserId(),
                        title,
                        body,
                        createNotificationData(event.getEventId(), "NOT_SELECTED")
                );

                if (sent) {
                    successCount++;
                    logs.add(createLog(event, entrant, "LOTTERY_LOSS", title, body, true));
                } else {
                    logs.add(createLog(event, entrant, "LOTTERY_LOSS", title, body, false));
                }
            }
        }

        saveNotificationLogs(logs);

        Log.d(TAG, "Sent " + successCount + " rejection notifications");
        callback.onSuccess(null);
    }

    private void sendCustomOrganizerNotification(String eventId, List<String> entrantIds,
                                                 String title, String body,
                                                 RepositoryCallback<Void> callback) {
        getEntrantsByIds(entrantIds, new RepositoryCallback<List<Entrant>>() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                getUsersForEntrants(entrants, new RepositoryCallback<Map<String, User>>() {
                    @Override
                    public void onSuccess(Map<String, User> userMap) {
                        List<NotificationLog> logs = new ArrayList<>();
                        int successCount = 0;

                        for (Entrant entrant : entrants) {
                            User user = userMap.get(entrant.getUserId());

                            if (user != null && user.isNotificationsEnabled()) {
                                boolean sent = sendPushNotification(
                                        entrant.getUserId(),
                                        title,
                                        body,
                                        createNotificationData(eventId, "ORGANIZER_MESSAGE")
                                );

                                if (sent) {
                                    successCount++;
                                }

                                logs.add(createLogSimple(eventId, entrant.getUserId(),
                                        "ORGANIZER_MESSAGE", title, body, sent));
                            }
                        }

                        saveNotificationLogs(logs);

                        Log.d(TAG, "Sent " + successCount + " organizer notifications");
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Send push notification via FCM
     * In production, this would use FCM server API with user's device token
     * For now, returns true to simulate success
     */
    private boolean sendPushNotification(String userId, String title, String body,
                                         Map<String, String> data) {
        try {
            // In real implementation:
            // 1. Get user's FCM device token from User document
            // 2. Send via FCM REST API or Admin SDK
            // 3. Handle errors and retry logic

            Log.d(TAG, "Sending notification to " + userId + ": " + title);

            // Simulated for now - would use FCM in production
            // FirebaseMessaging.getInstance().send(message);

            return true; // Simulate success
        } catch (Exception e) {
            Log.e(TAG, "Failed to send notification", e);
            return false;
        }
    }

    private Map<String, String> createNotificationData(String eventId, String type) {
        Map<String, String> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("notificationType", type);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return data;
    }

    private NotificationLog createLog(Event event, Entrant entrant, String type,
                                      String title, String body, boolean success) {
        NotificationLog log = new NotificationLog();
        log.setLogId(db.collection(COLLECTION_NOTIFICATION_LOGS).document().getId());
        log.setEventId(event.getEventId());
        log.setEventName(event.getName());
        log.setRecipientUserId(entrant.getUserId());
        log.setNotificationType(type);
        log.setTitle(title);
        log.setBody(body);
        log.setTimestamp(System.currentTimeMillis());
        log.setSentSuccessfully(success);
        log.setOrganizerId(event.getOrganizerId());
        return log;
    }

    private NotificationLog createLogSimple(String eventId, String userId, String type,
                                            String title, String body, boolean success) {
        NotificationLog log = new NotificationLog();
        log.setLogId(db.collection(COLLECTION_NOTIFICATION_LOGS).document().getId());
        log.setEventId(eventId);
        log.setRecipientUserId(userId);
        log.setNotificationType(type);
        log.setTitle(title);
        log.setBody(body);
        log.setTimestamp(System.currentTimeMillis());
        log.setSentSuccessfully(success);
        return log;
    }

    private void saveNotificationLogs(List<NotificationLog> logs) {
        if (logs.isEmpty()) return;

        WriteBatch batch = db.batch();

        for (NotificationLog log : logs) {
            batch.set(db.collection(COLLECTION_NOTIFICATION_LOGS).document(log.getLogId()),
                    log.toMap());
        }

        batch.commit()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Saved " + logs.size() + " notification logs"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to save notification logs", e));
    }

    private void getEntrantsByIds(List<String> entrantIds,
                                  RepositoryCallback<List<Entrant>> callback) {
        if (entrantIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Firestore 'in' query limited to 10 items
        List<Entrant> allEntrants = new ArrayList<>();
        final int BATCH_SIZE = 10;

        fetchEntrantsBatch(entrantIds, 0, BATCH_SIZE, allEntrants, callback);
    }

    private void fetchEntrantsBatch(List<String> allIds, int startIndex, int batchSize,
                                    List<Entrant> accumulated,
                                    RepositoryCallback<List<Entrant>> callback) {
        if (startIndex >= allIds.size()) {
            callback.onSuccess(accumulated);
            return;
        }

        int endIndex = Math.min(startIndex + batchSize, allIds.size());
        List<String> batchIds = allIds.subList(startIndex, endIndex);

        db.collection(COLLECTION_ENTRANTS)
                .whereIn("entrantId", batchIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            accumulated.add(entrant);
                        }
                    }

                    // Fetch next batch
                    fetchEntrantsBatch(allIds, endIndex, batchSize, accumulated, callback);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void getUsersForEntrants(List<Entrant> entrants,
                                     RepositoryCallback<Map<String, User>> callback) {
        List<String> userIds = new ArrayList<>();
        for (Entrant e : entrants) {
            userIds.add(e.getUserId());
        }

        Map<String, User> userMap = new HashMap<>();

        if (userIds.isEmpty()) {
            callback.onSuccess(userMap);
            return;
        }

        fetchUsersBatch(userIds, 0, 10, userMap, callback);
    }

    private void fetchUsersBatch(List<String> allIds, int startIndex, int batchSize,
                                 Map<String, User> accumulated,
                                 RepositoryCallback<Map<String, User>> callback) {
        if (startIndex >= allIds.size()) {
            callback.onSuccess(accumulated);
            return;
        }

        int endIndex = Math.min(startIndex + batchSize, allIds.size());
        List<String> batchIds = allIds.subList(startIndex, endIndex);

        db.collection(COLLECTION_USERS)
                .whereIn("userId", batchIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            accumulated.put(user.getUserId(), user);
                        }
                    }

                    fetchUsersBatch(allIds, endIndex, batchSize, accumulated, callback);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
