package com.example.lotterysystemproject.firebasemanager;

import android.util.Log;

import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.Registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Mock implementation of RegistrationRepository for testing
 * Uses in-memory storage instead of Firebase
 */
public class MockRegistrationRepository implements RegistrationRepository {

    private static final String TAG = "MockRegRepo";

    // In-memory storage
    private final Map<String, Entrant> entrants = new ConcurrentHashMap<>();
    private final Map<String, Registration> registrations = new ConcurrentHashMap<>();
    private final Map<String, Integer> eventWaitingCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> eventMaxWaiting = new ConcurrentHashMap<>();

    // Listeners
    private final Map<String, List<RepositoryListener<List<Entrant>>>> waitingListeners =
            new ConcurrentHashMap<>();
    private final Map<String, List<RepositoryListener<List<Entrant>>>> selectedListeners =
            new ConcurrentHashMap<>();

    // Simulate async behavior
    private boolean simulateDelay = false;
    private long delayMs = 100;

    public MockRegistrationRepository() {
    }

    // ========================================================================
    // CONFIGURATION (for testing)
    // ========================================================================

    public void setSimulateDelay(boolean simulate, long delayMs) {
        this.simulateDelay = simulate;
        this.delayMs = delayMs;
    }

    public void clear() {
        entrants.clear();
        registrations.clear();
        eventWaitingCounts.clear();
        eventMaxWaiting.clear();
        waitingListeners.clear();
        selectedListeners.clear();
    }

    public int getEntrantCount() {
        return entrants.size();
    }

    public Entrant getEntrant(String entrantId) {
        return entrants.get(entrantId);
    }

    // ========================================================================
    // JOIN & LEAVE WAITING LIST
    // ========================================================================

    @Override
    public void joinWaitingList(String eventId, Entrant entrant,
                                RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                // Check if already joined
                for (Entrant e : entrants.values()) {
                    if (e.getEventId().equals(eventId) &&
                            e.getUserId().equals(entrant.getUserId()) &&
                            (e.getStatus() == Entrant.Status.WAITING ||
                                    e.getStatus() == Entrant.Status.INVITED ||
                                    e.getStatus() == Entrant.Status.ENROLLED)) {
                        callback.onFailure(new Exception("Already on waiting list"));
                        return;
                    }
                }

                // Check if waiting list is full
                Integer maxWaiting = eventMaxWaiting.get(eventId);
                Integer currentCount = eventWaitingCounts.getOrDefault(eventId, 0);

                if (maxWaiting != null && currentCount >= maxWaiting) {
                    callback.onFailure(new Exception("Waiting list is full"));
                    return;
                }

                // Add entrant
                entrants.put(entrant.getEntrantId(), entrant);

                // Increment count
                eventWaitingCounts.put(eventId, currentCount + 1);

                // Notify listeners
                notifyWaitingListListeners(eventId);

                Log.d(TAG, "Joined waiting list: " + entrant.getEntrantId());
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void leaveWaitingList(String eventId, String entrantId,
                                 RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                Entrant entrant = entrants.get(entrantId);

                if (entrant == null) {
                    callback.onFailure(new Exception("Entrant not found"));
                    return;
                }

                if (entrant.getStatus() != Entrant.Status.WAITING) {
                    callback.onFailure(new Exception("Not on waiting list"));
                    return;
                }

                // Remove entrant
                entrants.remove(entrantId);

                // Decrement count
                int count = eventWaitingCounts.getOrDefault(eventId, 0);
                eventWaitingCounts.put(eventId, Math.max(0, count - 1));

                // Notify listeners
                notifyWaitingListListeners(eventId);

                Log.d(TAG, "Left waiting list: " + entrantId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    @Override
    public void getWaitingList(String eventId,
                               RepositoryCallback<List<Entrant>> callback) {
        executeAsync(() -> {
            try {
                List<Entrant> waiting = entrants.values().stream()
                        .filter(e -> e.getEventId().equals(eventId))
                        .filter(e -> e.getStatus() == Entrant.Status.WAITING)
                        .sorted((a, b) -> Long.compare(a.getJoinedTimestamp(),
                                b.getJoinedTimestamp()))
                        .collect(Collectors.toList());

                Log.d(TAG, "Found " + waiting.size() + " entrants on waiting list");
                callback.onSuccess(waiting);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void getWaitingListCount(String eventId,
                                    RepositoryCallback<Integer> callback) {
        executeAsync(() -> {
            try {
                int count = eventWaitingCounts.getOrDefault(eventId, 0);
                callback.onSuccess(count);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void getSelectedEntrants(String eventId,
                                    RepositoryCallback<List<Entrant>> callback) {
        executeAsync(() -> {
            try {
                List<Entrant> selected = entrants.values().stream()
                        .filter(e -> e.getEventId().equals(eventId))
                        .filter(e -> e.getStatus() == Entrant.Status.INVITED)
                        .collect(Collectors.toList());

                Log.d(TAG, "Found " + selected.size() + " selected entrants");
                callback.onSuccess(selected);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void getCancelledEntrants(String eventId,
                                     RepositoryCallback<List<Entrant>> callback) {
        executeAsync(() -> {
            try {
                List<Entrant> cancelled = entrants.values().stream()
                        .filter(e -> e.getEventId().equals(eventId))
                        .filter(e -> e.getStatus() == Entrant.Status.CANCELLED)
                        .collect(Collectors.toList());

                Log.d(TAG, "Found " + cancelled.size() + " cancelled entrants");
                callback.onSuccess(cancelled);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void getFinalEnrolled(String eventId,
                                 RepositoryCallback<List<Entrant>> callback) {
        executeAsync(() -> {
            try {
                List<Entrant> enrolled = entrants.values().stream()
                        .filter(e -> e.getEventId().equals(eventId))
                        .filter(e -> e.getStatus() == Entrant.Status.ENROLLED)
                        .collect(Collectors.toList());

                Log.d(TAG, "Found " + enrolled.size() + " enrolled entrants");
                callback.onSuccess(enrolled);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // LOTTERY OPERATIONS
    // ========================================================================

    @Override
    public void drawEntrants(String eventId, int numberOfEntrantsToSelect,
                             RepositoryCallback<List<Entrant>> callback) {
        executeAsync(() -> {
            try {
                // Get waiting list
                List<Entrant> waiting = entrants.values().stream()
                        .filter(e -> e.getEventId().equals(eventId))
                        .filter(e -> e.getStatus() == Entrant.Status.WAITING)
                        .collect(Collectors.toList());

                if (waiting.isEmpty()) {
                    callback.onFailure(new Exception("No entrants on waiting list"));
                    return;
                }

                // Shuffle and select
                Collections.shuffle(waiting);
                int selectCount = Math.min(numberOfEntrantsToSelect, waiting.size());
                List<Entrant> selected = waiting.subList(0, selectCount);

                // Update statuses
                long timestamp = System.currentTimeMillis();
                for (Entrant entrant : selected) {
                    entrant.setStatus(Entrant.Status.INVITED);
                    entrant.setStatusTimestamp(timestamp);
                    entrants.put(entrant.getEntrantId(), entrant);
                }

                // Update count
                int count = eventWaitingCounts.getOrDefault(eventId, 0);
                eventWaitingCounts.put(eventId, Math.max(0, count - selectCount));

                // Notify listeners
                notifyWaitingListListeners(eventId);
                notifySelectedListeners(eventId);

                Log.d(TAG, "Drew " + selectCount + " entrants");
                callback.onSuccess(new ArrayList<>(selected));

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void drawReplacement(String eventId,
                                RepositoryCallback<Entrant> callback) {
        executeAsync(() -> {
            try {
                // Get next person in waiting list (oldest first)
                Entrant replacement = entrants.values().stream()
                        .filter(e -> e.getEventId().equals(eventId))
                        .filter(e -> e.getStatus() == Entrant.Status.WAITING)
                        .min((a, b) -> Long.compare(a.getJoinedTimestamp(),
                                b.getJoinedTimestamp()))
                        .orElse(null);

                if (replacement == null) {
                    callback.onFailure(new Exception("No entrants available for replacement"));
                    return;
                }

                // Update status
                replacement.setStatus(Entrant.Status.INVITED);
                replacement.setStatusTimestamp(System.currentTimeMillis());
                entrants.put(replacement.getEntrantId(), replacement);

                // Update count
                int count = eventWaitingCounts.getOrDefault(eventId, 0);
                eventWaitingCounts.put(eventId, Math.max(0, count - 1));

                // Notify listeners
                notifyWaitingListListeners(eventId);
                notifySelectedListeners(eventId);

                Log.d(TAG, "Drew replacement: " + replacement.getEntrantId());
                callback.onSuccess(replacement);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // ACCEPT / DECLINE OPERATIONS
    // ========================================================================

    @Override
    public void acceptInvitation(String eventId, String entrantId,
                                 RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                Entrant entrant = entrants.get(entrantId);

                if (entrant == null) {
                    callback.onFailure(new Exception("Entrant not found"));
                    return;
                }

                if (entrant.getStatus() != Entrant.Status.INVITED) {
                    callback.onFailure(new Exception("Not invited"));
                    return;
                }

                // Update status
                entrant.setStatus(Entrant.Status.ENROLLED);
                entrant.setStatusTimestamp(System.currentTimeMillis());
                entrants.put(entrantId, entrant);

                // Notify listeners
                notifySelectedListeners(eventId);

                Log.d(TAG, "Accepted invitation: " + entrantId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void declineInvitation(String eventId, String entrantId,
                                  RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                Entrant entrant = entrants.get(entrantId);

                if (entrant == null) {
                    callback.onFailure(new Exception("Entrant not found"));
                    return;
                }

                // Update status
                entrant.setStatus(Entrant.Status.CANCELLED);
                entrant.setStatusTimestamp(System.currentTimeMillis());
                entrants.put(entrantId, entrant);

                // Notify listeners
                notifySelectedListeners(eventId);

                Log.d(TAG, "Declined invitation: " + entrantId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // REGISTRATION HISTORY
    // ========================================================================

    @Override
    public void getUserRegistrations(String userId,
                                     RepositoryCallback<List<Registration>> callback) {
        executeAsync(() -> {
            try {
                List<Registration> userRegs = registrations.values().stream()
                        .filter(r -> r.getUserId().equals(userId))
                        .sorted((a, b) -> Long.compare(b.getRegisteredAt(),
                                a.getRegisteredAt()))
                        .collect(Collectors.toList());

                Log.d(TAG, "Found " + userRegs.size() + " registrations");
                callback.onSuccess(userRegs);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // REAL-TIME LISTENERS
    // ========================================================================

    @Override
    public void listenToWaitingList(String eventId,
                                    RepositoryListener<List<Entrant>> listener) {
        waitingListeners.computeIfAbsent(eventId, k -> new ArrayList<>()).add(listener);

        // Immediately send current data
        getWaitingList(eventId, new RepositoryCallback<List<Entrant>>() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                listener.onDataChanged(entrants);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onError(e);
            }
        });
    }

    @Override
    public void listenToSelectedEntrants(String eventId,
                                         RepositoryListener<List<Entrant>> listener) {
        selectedListeners.computeIfAbsent(eventId, k -> new ArrayList<>()).add(listener);

        // Immediately send current data
        getSelectedEntrants(eventId, new RepositoryCallback<List<Entrant>>() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                listener.onDataChanged(entrants);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onError(e);
            }
        });
    }

    public void removeAllListeners() {
        waitingListeners.clear();
        selectedListeners.clear();
    }

    // ========================================================================
    // BATCH OPERATIONS
    // ========================================================================

    @Override
    public void updateEntrantsStatus(String eventId, List<String> entrantIds,
                                     Entrant.Status newStatus,
                                     RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                long timestamp = System.currentTimeMillis();

                for (String entrantId : entrantIds) {
                    Entrant entrant = entrants.get(entrantId);
                    if (entrant != null) {
                        entrant.setStatus(newStatus);
                        entrant.setStatusTimestamp(timestamp);
                        entrants.put(entrantId, entrant);
                    }
                }

                // Notify listeners
                notifyWaitingListListeners(eventId);
                notifySelectedListeners(eventId);

                Log.d(TAG, "Updated " + entrantIds.size() + " entrants");
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // UTILITY OPERATIONS
    // ========================================================================

    @Override
    public void setMaxEntrants(String eventId, int max,
                               RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                eventMaxWaiting.put(eventId, max);
                Log.d(TAG, "Set max waiting list: " + max);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void isOnWaitingList(String eventId, String entrantId,
                                RepositoryCallback<Boolean> callback) {
        executeAsync(() -> {
            try {
                Entrant entrant = entrants.get(entrantId);
                boolean onList = entrant != null &&
                        entrant.getEventId().equals(eventId) &&
                        entrant.getStatus() == Entrant.Status.WAITING;
                callback.onSuccess(onList);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void exportFinalListCsv(String eventId,
                                   RepositoryCallback<String> callback) {
        executeAsync(() -> {
            try {
                List<Entrant> enrolled = entrants.values().stream()
                        .filter(e -> e.getEventId().equals(eventId))
                        .filter(e -> e.getStatus() == Entrant.Status.ENROLLED)
                        .collect(Collectors.toList());

                StringBuilder csv = new StringBuilder();
                csv.append("EntrantId,UserId,Status,JoinedAt\n");

                for (Entrant e : enrolled) {
                    csv.append(e.getEntrantId()).append(",");
                    csv.append(e.getUserId()).append(",");
                    csv.append(e.getStatus().name()).append(",");
                    csv.append(e.getJoinedTimestamp()).append("\n");
                }

                Log.d(TAG, "Generated CSV with " + enrolled.size() + " rows");
                callback.onSuccess(csv.toString());

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void notifyWaitingListListeners(String eventId) {
        List<RepositoryListener<List<Entrant>>> listeners = waitingListeners.get(eventId);
        if (listeners != null) {
            getWaitingList(eventId, new RepositoryCallback<List<Entrant>>() {
                @Override
                public void onSuccess(List<Entrant> entrants) {
                    for (RepositoryListener<List<Entrant>> listener : listeners) {
                        listener.onDataChanged(entrants);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    for (RepositoryListener<List<Entrant>> listener : listeners) {
                        listener.onError(e);
                    }
                }
            });
        }
    }

    private void notifySelectedListeners(String eventId) {
        List<RepositoryListener<List<Entrant>>> listeners = selectedListeners.get(eventId);
        if (listeners != null) {
            getSelectedEntrants(eventId, new RepositoryCallback<List<Entrant>>() {
                @Override
                public void onSuccess(List<Entrant> entrants) {
                    for (RepositoryListener<List<Entrant>> listener : listeners) {
                        listener.onDataChanged(entrants);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    for (RepositoryListener<List<Entrant>> listener : listeners) {
                        listener.onError(e);
                    }
                }
            });
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

    public void addRegistration(Registration registration) {
        registrations.put(registration.getRegistrationId(), registration);
    }
}
