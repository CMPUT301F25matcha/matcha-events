package com.example.lotterysystemproject.firebasemanager;

import android.util.Log;

import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.models.Registration;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase implementation of RegistrationRepository
 * Handles all lottery and registration operations
 *
 * Collections used:
 * - entrants/: Individual entrant records per event
 * - registrations/: User's registration history
 * - events/: Updates denormalized counts
 */
public class FirebaseRegistrationRepository implements RegistrationRepository {

    private static final String TAG = "RegistrationRepo";
    private static final String COLLECTION_ENTRANTS = "entrants";
    private static final String COLLECTION_REGISTRATIONS = "registrations";
    private static final String COLLECTION_EVENTS = "events";

    private final FirebaseFirestore db;
    private final Map<String, ListenerRegistration> activeListeners;

    public FirebaseRegistrationRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.activeListeners = new HashMap<>();
    }

    // ========================================================================
    // JOIN & LEAVE WAITING LIST
    // ========================================================================

    @Override
    public void joinWaitingList(String eventId, Entrant entrant,
                                RepositoryCallback<Void> callback) {
        Log.d(TAG, "joinWaitingList: eventId=" + eventId + ", userId=" + entrant.getUserId());

        // Step 1: Check if event exists and has capacity
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

                    // Check if waiting list is full
                    if (event.getMaxWaitingList() != 0 &&
                            event.getCurrentWaitingCount() >= event.getMaxWaitingList()) {
                        callback.onFailure(new Exception("Waiting list is full"));
                        return;
                    }

                    // Step 2: Check if user is already on waiting list
                    checkIfAlreadyJoined(eventId, entrant.getUserId(), alreadyJoined -> {
                        if (alreadyJoined) {
                            callback.onFailure(new Exception("Already on waiting list"));
                            return;
                        }

                        // Step 3: Create entrant record
                        WriteBatch batch = db.batch();

                        String entrantId = entrant.getEntrantId();
                        batch.set(db.collection(COLLECTION_ENTRANTS).document(entrantId),
                                entrant.toMap());

                        // Step 4: Increment waiting list count
                        batch.update(db.collection(COLLECTION_EVENTS).document(eventId),
                                "currentWaitingCount", FieldValue.increment(1));

                        // Commit batch
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully joined waiting list");
                                    callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to join waiting list", e);
                                    callback.onFailure(e);
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch event", e);
                    callback.onFailure(e);
                });
    }

    @Override
    public void leaveWaitingList(String eventId, String entrantId,
                                 RepositoryCallback<Void> callback) {
        Log.d(TAG, "leaveWaitingList: eventId=" + eventId + ", entrantId=" + entrantId);

        // Step 1: Get entrant to verify status
        db.collection(COLLECTION_ENTRANTS).document(entrantId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onFailure(new Exception("Entrant not found"));
                        return;
                    }

                    Entrant entrant = doc.toObject(Entrant.class);
                    if (entrant == null || entrant.getStatus() != Entrant.Status.WAITING) {
                        callback.onFailure(new Exception("Not on waiting list"));
                        return;
                    }

                    // Step 2: Delete entrant record and update count
                    WriteBatch batch = db.batch();

                    batch.delete(db.collection(COLLECTION_ENTRANTS).document(entrantId));

                    batch.update(db.collection(COLLECTION_EVENTS).document(eventId),
                            "currentWaitingCount", FieldValue.increment(-1));

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Successfully left waiting list");
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to leave waiting list", e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch entrant", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    @Override
    public void getWaitingList(String eventId,
                               RepositoryCallback<List<Entrant>> callback) {
        Log.d(TAG, "getWaitingList: eventId=" + eventId);

        db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", Entrant.Status.WAITING.name())
                .orderBy("joinedTimestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Entrant> entrants = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            entrants.add(entrant);
                        }
                    }
                    Log.d(TAG, "Found " + entrants.size() + " entrants on waiting list");
                    callback.onSuccess(entrants);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get waiting list", e);
                    callback.onFailure(e);
                });
    }

    @Override
    public void getWaitingListCount(String eventId,
                                    RepositoryCallback<Integer> callback) {
        Log.d(TAG, "getWaitingListCount: eventId=" + eventId);

        // Use denormalized count from Event for efficiency
        db.collection(COLLECTION_EVENTS).document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onFailure(new Exception("Event not found"));
                        return;
                    }

                    Event event = doc.toObject(Event.class);
                    if (event != null) {
                        callback.onSuccess(event.getCurrentWaitingCount());
                    } else {
                        callback.onFailure(new Exception("Invalid event data"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get waiting list count", e);
                    callback.onFailure(e);
                });
    }

    @Override
    public void getSelectedEntrants(String eventId,
                                    RepositoryCallback<List<Entrant>> callback) {
        Log.d(TAG, "getSelectedEntrants: eventId=" + eventId);

        db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", Entrant.Status.INVITED.name())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Entrant> entrants = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            entrants.add(entrant);
                        }
                    }
                    Log.d(TAG, "Found " + entrants.size() + " selected entrants");
                    callback.onSuccess(entrants);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get selected entrants", e);
                    callback.onFailure(e);
                });
    }

    @Override
    public void getCancelledEntrants(String eventId,
                                     RepositoryCallback<List<Entrant>> callback) {
        Log.d(TAG, "getCancelledEntrants: eventId=" + eventId);

        db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", Entrant.Status.CANCELLED.name())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Entrant> entrants = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            entrants.add(entrant);
                        }
                    }
                    Log.d(TAG, "Found " + entrants.size() + " cancelled entrants");
                    callback.onSuccess(entrants);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get cancelled entrants", e);
                    callback.onFailure(e);
                });
    }

    @Override
    public void getFinalEnrolled(String eventId,
                                 RepositoryCallback<List<Entrant>> callback) {
        Log.d(TAG, "getFinalEnrolled: eventId=" + eventId);

        db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", Entrant.Status.ENROLLED.name())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Entrant> entrants = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            entrants.add(entrant);
                        }
                    }
                    Log.d(TAG, "Found " + entrants.size() + " enrolled entrants");
                    callback.onSuccess(entrants);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get enrolled entrants", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // LOTTERY OPERATIONS
    // ========================================================================

    @Override
    public void drawEntrants(String eventId, int numberOfEntrantsToSelect,
                             RepositoryCallback<List<Entrant>> callback) {
        Log.d(TAG, "drawEntrants: eventId=" + eventId + ", count=" + numberOfEntrantsToSelect);

        // Step 1: Get all waiting entrants
        getWaitingList(eventId, new RepositoryCallback<List<Entrant>>() {
            @Override
            public void onSuccess(List<Entrant> waitingList) {
                if (waitingList.isEmpty()) {
                    callback.onFailure(new Exception("No entrants on waiting list"));
                    return;
                }

                if (waitingList.size() < numberOfEntrantsToSelect) {
                    Log.w(TAG, "Requested " + numberOfEntrantsToSelect +
                            " but only " + waitingList.size() + " available");
                }

                // Step 2: Shuffle randomly
                List<Entrant> shuffled = new ArrayList<>(waitingList);
                Collections.shuffle(shuffled);

                // Step 3: Select top N
                int selectCount = Math.min(numberOfEntrantsToSelect, shuffled.size());
                List<Entrant> selected = shuffled.subList(0, selectCount);

                // Step 4: Update statuses in batch
                List<String> selectedIds = new ArrayList<>();
                for (Entrant e : selected) {
                    selectedIds.add(e.getEntrantId());
                }

                updateEntrantsStatus(eventId, selectedIds, Entrant.Status.INVITED,
                        new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // Step 5: Update event denormalized count
                                db.collection(COLLECTION_EVENTS).document(eventId)
                                        .update("currentWaitingCount",
                                                FieldValue.increment(-selectCount))
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Successfully drew " + selectCount + " entrants");
                                            callback.onSuccess(selected);
                                        })
                                        .addOnFailureListener(callback::onFailure);
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

    @Override
    public void drawReplacement(String eventId,
                                RepositoryCallback<Entrant> callback) {
        Log.d(TAG, "drawReplacement: eventId=" + eventId);

        // Get next person in waiting list (oldest first)
        db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", Entrant.Status.WAITING.name())
                .orderBy("joinedTimestamp", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onFailure(new Exception("No entrants available for replacement"));
                        return;
                    }

                    Entrant replacement = querySnapshot.getDocuments().get(0)
                            .toObject(Entrant.class);
                    if (replacement == null) {
                        callback.onFailure(new Exception("Invalid entrant data"));
                        return;
                    }

                    // Update status to INVITED
                    replacement.setStatus(Entrant.Status.INVITED);

                    WriteBatch batch = db.batch();

                    batch.update(db.collection(COLLECTION_ENTRANTS)
                                    .document(replacement.getEntrantId()),
                            "status", Entrant.Status.INVITED.name(),
                            "statusTimestamp", System.currentTimeMillis());

                    batch.update(db.collection(COLLECTION_EVENTS).document(eventId),
                            "currentWaitingCount", FieldValue.increment(-1));

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Successfully drew replacement");
                                callback.onSuccess(replacement);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to draw replacement", e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to query for replacement", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // ACCEPT / DECLINE OPERATIONS
    // ========================================================================

    @Override
    public void acceptInvitation(String eventId, String entrantId,
                                 RepositoryCallback<Void> callback) {
        Log.d(TAG, "acceptInvitation: eventId=" + eventId + ", entrantId=" + entrantId);

        WriteBatch batch = db.batch();

        // Update entrant status
        batch.update(db.collection(COLLECTION_ENTRANTS).document(entrantId),
                "status", Entrant.Status.ENROLLED.name(),
                "statusTimestamp", System.currentTimeMillis());

        // Increment enrollment count
        batch.update(db.collection(COLLECTION_EVENTS).document(eventId),
                "currentEnrollmentCount", FieldValue.increment(1));

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully accepted invitation");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to accept invitation", e);
                    callback.onFailure(e);
                });
    }

    @Override
    public void declineInvitation(String eventId, String entrantId,
                                  RepositoryCallback<Void> callback) {
        Log.d(TAG, "declineInvitation: eventId=" + eventId + ", entrantId=" + entrantId);

        // Update entrant status to CANCELLED
        db.collection(COLLECTION_ENTRANTS).document(entrantId)
                .update("status", Entrant.Status.CANCELLED.name(),
                        "statusTimestamp", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully declined invitation");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to decline invitation", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // REGISTRATION HISTORY
    // ========================================================================

    @Override
    public void getUserRegistrations(String userId,
                                     RepositoryCallback<List<Registration>> callback) {
        Log.d(TAG, "getUserRegistrations: userId=" + userId);

        db.collection(COLLECTION_REGISTRATIONS)
                .whereEqualTo("userId", userId)
                .orderBy("registeredAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Registration> registrations = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Registration reg = doc.toObject(Registration.class);
                        if (reg != null) {
                            registrations.add(reg);
                        }
                    }
                    Log.d(TAG, "Found " + registrations.size() + " registrations");
                    callback.onSuccess(registrations);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user registrations", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // REAL-TIME LISTENERS
    // ========================================================================

    @Override
    public void listenToWaitingList(String eventId,
                                    RepositoryListener<List<Entrant>> listener) {
        Log.d(TAG, "listenToWaitingList: eventId=" + eventId);

        String listenerKey = "waiting_" + eventId;

        // Remove old listener if exists
        removeListener(listenerKey);

        ListenerRegistration registration = db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", Entrant.Status.WAITING.name())
                .orderBy("joinedTimestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Waiting list listener error", error);
                        listener.onError(error);
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Entrant> entrants = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            if (entrant != null) {
                                entrants.add(entrant);
                            }
                        }
                        listener.onDataChanged(entrants);
                    }
                });

        activeListeners.put(listenerKey, registration);
    }

    @Override
    public void listenToSelectedEntrants(String eventId,
                                         RepositoryListener<List<Entrant>> listener) {
        Log.d(TAG, "listenToSelectedEntrants: eventId=" + eventId);

        String listenerKey = "selected_" + eventId;

        removeListener(listenerKey);

        ListenerRegistration registration = db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", Entrant.Status.INVITED.name())
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Selected entrants listener error", error);
                        listener.onError(error);
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Entrant> entrants = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            if (entrant != null) {
                                entrants.add(entrant);
                            }
                        }
                        listener.onDataChanged(entrants);
                    }
                });

        activeListeners.put(listenerKey, registration);
    }

    // ========================================================================
    // BATCH OPERATIONS
    // ========================================================================

    @Override
    public void updateEntrantsStatus(String eventId, List<String> entrantIds,
                                     Entrant.Status newStatus,
                                     RepositoryCallback<Void> callback) {
        Log.d(TAG, "updateEntrantsStatus: eventId=" + eventId +
                ", count=" + entrantIds.size() + ", status=" + newStatus);

        if (entrantIds.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        // Firestore batch limit is 500 operations
        final int BATCH_SIZE = 500;
        List<WriteBatch> batches = new ArrayList<>();
        WriteBatch currentBatch = db.batch();
        int operationCount = 0;

        long timestamp = System.currentTimeMillis();

        for (String entrantId : entrantIds) {
            currentBatch.update(db.collection(COLLECTION_ENTRANTS).document(entrantId),
                    "status", newStatus.name(),
                    "statusTimestamp", timestamp);

            operationCount++;

            if (operationCount >= BATCH_SIZE) {
                batches.add(currentBatch);
                currentBatch = db.batch();
                operationCount = 0;
            }
        }

        // Add final batch
        if (operationCount > 0) {
            batches.add(currentBatch);
        }

        // Commit all batches
        commitBatches(batches, 0, callback);
    }

    private void commitBatches(List<WriteBatch> batches, int index,
                               RepositoryCallback<Void> callback) {
        if (index >= batches.size()) {
            callback.onSuccess(null);
            return;
        }

        batches.get(index).commit()
                .addOnSuccessListener(aVoid -> commitBatches(batches, index + 1, callback))
                .addOnFailureListener(callback::onFailure);
    }

    // ========================================================================
    // UTILITY OPERATIONS
    // ========================================================================

    @Override
    public void setMaxEntrants(String eventId, int max,
                               RepositoryCallback<Void> callback) {
        Log.d(TAG, "setMaxEntrants: eventId=" + eventId + ", max=" + max);

        db.collection(COLLECTION_EVENTS).document(eventId)
                .update("maxWaitingList", max)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully set max waiting list");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to set max waiting list", e);
                    callback.onFailure(e);
                });
    }

    @Override
    public void isOnWaitingList(String eventId, String entrantId,
                                RepositoryCallback<Boolean> callback) {
        db.collection(COLLECTION_ENTRANTS).document(entrantId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onSuccess(false);
                        return;
                    }

                    Entrant entrant = doc.toObject(Entrant.class);
                    boolean onList = entrant != null &&
                            entrant.getEventId().equals(eventId) &&
                            entrant.getStatus() == Entrant.Status.WAITING;
                    callback.onSuccess(onList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check waiting list status", e);
                    callback.onFailure(e);
                });
    }

    @Override
    public void exportFinalListCsv(String eventId,
                                   RepositoryCallback<String> callback) {
        Log.d(TAG, "exportFinalListCsv: eventId=" + eventId);

        getFinalEnrolled(eventId, new RepositoryCallback<List<Entrant>>() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                StringBuilder csv = new StringBuilder();
                csv.append("Name,Email,Phone,Joined At,Status\n");

                for (Entrant entrant : entrants) {
                    csv.append(escapeCsv(entrant.getUserId())).append(",");
                    csv.append(escapeCsv("")).append(","); // Email from User table
                    csv.append(escapeCsv("")).append(","); // Phone from User table
                    csv.append(entrant.getJoinedTimestamp()).append(",");
                    csv.append(entrant.getStatus().name()).append("\n");
                }

                Log.d(TAG, "Generated CSV with " + entrants.size() + " rows");
                callback.onSuccess(csv.toString());
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void checkIfAlreadyJoined(String eventId, String userId,
                                      RepositoryCallback<Boolean> callback) {
        db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .whereIn("status", List.of(
                        Entrant.Status.WAITING.name(),
                        Entrant.Status.INVITED.name(),
                        Entrant.Status.ENROLLED.name()
                ))
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        callback.onSuccess(!querySnapshot.isEmpty()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void removeListener(String key) {
        ListenerRegistration registration = activeListeners.remove(key);
        if (registration != null) {
            registration.remove();
            Log.d(TAG, "Removed listener: " + key);
        }
    }

    /**
     * Clean up all active listeners
     * Call this in onDestroy() of Activities/Fragments
     */
    public void removeAllListeners() {
        for (ListenerRegistration registration : activeListeners.values()) {
            registration.remove();
        }
        activeListeners.clear();
        Log.d(TAG, "Removed all listeners");
    }
}
