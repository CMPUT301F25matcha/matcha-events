package com.example.lotterysystemproject.repositories.mock;

import android.util.Log;

import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.NotificationLog;
import com.example.lotterysystemproject.repositories.NotificationRepository;
import com.example.lotterysystemproject.repositories.RegistrationRepository;
import com.example.lotterysystemproject.repositories.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Mock implementation of NotificationRepository for testing
 * Stores notifications in memory and tracks notification history
 */
public class MockNotificationRepository implements NotificationRepository {

    private static final String TAG = "MockNotificationRepo";

    // Storage
    private final Map<String, NotificationLog> notificationLogs = new ConcurrentHashMap<>();
    private final List<String> sentNotifications = new ArrayList<>();

    private final RegistrationRepository registrationRepo;

    private boolean simulateDelay = false;
    private long delayMs = 100;
    private boolean simulateFailure = false;

    public MockNotificationRepository(RegistrationRepository registrationRepo) {
        this.registrationRepo = registrationRepo;
    }

    public MockNotificationRepository() {
        this(new MockRegistrationRepository());
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
        notificationLogs.clear();
        sentNotifications.clear();
    }

    public int getNotificationCount() {
        return notificationLogs.size();
    }

    public List<String> getSentNotifications() {
        return new ArrayList<>(sentNotifications);
    }

    public List<NotificationLog> getAllLogs() {
        return new ArrayList<>(notificationLogs.values());
    }

    // ========================================================================
    // LOTTERY NOTIFICATIONS
    // ========================================================================

    @Override
    public void notifySelectedEntrants(String eventId, List<String> entrantIds,
                                       RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated notification failure"));
                    return;
                }

                Log.d(TAG, "Notifying " + entrantIds.size() + " selected entrants");

                for (String entrantId : entrantIds) {
                    NotificationLog log = new NotificationLog();
                    log.setLogId("log_" + System.currentTimeMillis() + "_" + entrantId);
                    log.setEventId(eventId);
                    log.setRecipientUserId(entrantId);
                    log.setNotificationType("LOTTERY_WIN");
                    log.setTitle("🎉 You've Been Selected!");
                    log.setBody("Congratulations! You've been selected for the event.");
                    log.setTimestamp(System.currentTimeMillis());
                    log.setSentSuccessfully(true);

                    notificationLogs.put(log.getLogId(), log);
                    sentNotifications.add("SELECTED: " + entrantId + " for event " + eventId);
                }

                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void notifyRejectedEntrants(String eventId, List<String> entrantIds,
                                       RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated notification failure"));
                    return;
                }

                Log.d(TAG, "Notifying " + entrantIds.size() + " rejected entrants");

                for (String entrantId : entrantIds) {
                    NotificationLog log = new NotificationLog();
                    log.setLogId("log_" + System.currentTimeMillis() + "_" + entrantId);
                    log.setEventId(eventId);
                    log.setRecipientUserId(entrantId);
                    log.setNotificationType("LOTTERY_LOSS");
                    log.setTitle("Lottery Update");
                    log.setBody("You weren't selected this time, but you're still on the waiting list.");
                    log.setTimestamp(System.currentTimeMillis());
                    log.setSentSuccessfully(true);

                    notificationLogs.put(log.getLogId(), log);
                    sentNotifications.add("REJECTED: " + entrantId + " for event " + eventId);
                }

                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // ORGANIZER BULK NOTIFICATIONS
    // ========================================================================

    @Override
    public void notifyWaitingList(String eventId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated notification failure"));
                    return;
                }

                // Get waiting list from RegistrationRepository
                registrationRepo.getWaitingList(eventId, new RepositoryCallback<>() {
                    @Override
                    public void onSuccess(List<Entrant> entrants) {
                        Log.d(TAG, "Notifying " + entrants.size() + " waiting list entrants");

                        for (Entrant entrant : entrants) {
                            NotificationLog log = new NotificationLog();
                            log.setLogId("log_" + System.currentTimeMillis() + "_" + entrant.getEntrantId());
                            log.setEventId(eventId);
                            log.setRecipientUserId(entrant.getUserId());
                            log.setNotificationType("ORGANIZER_MESSAGE");
                            log.setTitle("Waiting List Update");
                            log.setBody("New update about your event registration");
                            log.setTimestamp(System.currentTimeMillis());
                            log.setSentSuccessfully(true);

                            notificationLogs.put(log.getLogId(), log);
                            sentNotifications.add("WAITING_LIST: " + entrant.getUserId() +
                                    " for event " + eventId);
                        }

                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void notifyChosen(String eventId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated notification failure"));
                    return;
                }

                registrationRepo.getSelectedEntrants(eventId, new RepositoryCallback<>() {
                    @Override
                    public void onSuccess(List<Entrant> entrants) {
                        Log.d(TAG, "Notifying " + entrants.size() + " selected entrants");

                        for (Entrant entrant : entrants) {
                            NotificationLog log = new NotificationLog();
                            log.setLogId("log_" + System.currentTimeMillis() + "_" + entrant.getEntrantId());
                            log.setEventId(eventId);
                            log.setRecipientUserId(entrant.getUserId());
                            log.setNotificationType("ORGANIZER_MESSAGE");
                            log.setTitle("Selected Entrant Update");
                            log.setBody("Important update about your event participation");
                            log.setTimestamp(System.currentTimeMillis());
                            log.setSentSuccessfully(true);

                            notificationLogs.put(log.getLogId(), log);
                            sentNotifications.add("CHOSEN: " + entrant.getUserId() +
                                    " for event " + eventId);
                        }

                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void notifyCancelled(String eventId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated notification failure"));
                    return;
                }

                registrationRepo.getCancelledEntrants(eventId, new RepositoryCallback<>() {
                    @Override
                    public void onSuccess(List<Entrant> entrants) {
                        Log.d(TAG, "Notifying " + entrants.size() + " cancelled entrants");

                        for (Entrant entrant : entrants) {
                            NotificationLog log = new NotificationLog();
                            log.setLogId("log_" + System.currentTimeMillis() + "_" + entrant.getEntrantId());
                            log.setEventId(eventId);
                            log.setRecipientUserId(entrant.getUserId());
                            log.setNotificationType("ORGANIZER_MESSAGE");
                            log.setTitle("Cancelled Registration Update");
                            log.setBody("Information about your cancelled registration");
                            log.setTimestamp(System.currentTimeMillis());
                            log.setSentSuccessfully(true);

                            notificationLogs.put(log.getLogId(), log);
                            sentNotifications.add("CANCELLED: " + entrant.getUserId() +
                                    " for event " + eventId);
                        }

                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // ADMIN AUDIT LOG
    // ========================================================================

    @Override
    public void getNotificationLog(RepositoryCallback<List<String>> callback) {
        executeAsync(() -> {
            try {
                List<String> logs = notificationLogs.values().stream()
                        .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                        .map(NotificationLog::toString)
                        .collect(Collectors.toList());

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
     * Check if a specific user was notified for an event
     */
    public boolean wasUserNotified(String userId, String eventId) {
        return notificationLogs.values().stream()
                .anyMatch(log -> log.getRecipientUserId().equals(userId) &&
                        log.getEventId().equals(eventId) &&
                        log.isSentSuccessfully());
    }

    /**
     * Get notification count for a specific event
     */
    public int getNotificationCountForEvent(String eventId) {
        return (int) notificationLogs.values().stream()
                .filter(log -> log.getEventId().equals(eventId))
                .count();
    }

    /**
     * Get notifications by type
     */
    public List<NotificationLog> getNotificationsByType(String type) {
        return notificationLogs.values().stream()
                .filter(log -> log.getNotificationType().equals(type))
                .collect(Collectors.toList());
    }

    /**
     * Get notifications for a specific user
     */
    public List<NotificationLog> getNotificationsForUser(String userId) {
        return notificationLogs.values().stream()
                .filter(log -> log.getRecipientUserId().equals(userId))
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get successful notification count
     */
    public int getSuccessfulNotificationCount() {
        return (int) notificationLogs.values().stream()
                .filter(NotificationLog::isSentSuccessfully)
                .count();
    }

    /**
     * Get failed notification count
     */
    public int getFailedNotificationCount() {
        return (int) notificationLogs.values().stream()
                .filter(log -> !log.isSentSuccessfully())
                .count();
    }
}
