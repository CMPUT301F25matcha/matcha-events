package com.example.lotterysystemproject.firebasemanager;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lotterysystemproject.models.Entrant;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.lotterysystemproject.firebasemanager.NotificationRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.firebasemanager.RepositoryCallback;
import com.example.lotterysystemproject.models.NotificationItem;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Firebase implementation of EntrantRepository for production use.
 * Connects to real Firestore backend for entrant and lottery operations.
 */
public class FirebaseEntrantRepository implements EntrantRepository {
    private final FirebaseFirestore db;
    private final MutableLiveData<List<Entrant>> entrantsLiveData;

    /**
     * Initializes Firebase instances.
     */
    public FirebaseEntrantRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.entrantsLiveData = new MutableLiveData<>();
    }

    /**
     * Returns LiveData list of entrants for the given event ID.
     * Sets up real-time Firestore listener.
     * @param eventId ID of the event to fetch entrants for.
     * @return LiveData containing list of entrants.
     */
    @Override
    public LiveData<List<Entrant>> getEntrants(String eventId) {
        db.collection("entrants")
                .whereEqualTo("eventId", eventId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        entrantsLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Entrant> entrants = new ArrayList<>();
                    if (snapshots != null) {
                        Log.d("EntrantsRepo", "snapshot size = " + snapshots.size());
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Log.d("EntrantsRepo", "doc " + doc.getId()
                                    + " eventId=" + doc.getString("eventId")
                                    + " status=" + doc.getString("status"));
                            Entrant entrant = doc.toObject(Entrant.class);
                            if (entrant != null) {
                                // Preserve document ID
                                entrant.setId(doc.getId());
                                entrants.add(entrant);
                            }
                        }
                    }
                    entrantsLiveData.setValue(entrants);
                });

        return entrantsLiveData;
    }

    /**
     * Performs random lottery draw. Updates entrant statuses in Firestore.
     * @param eventId ID of the event for lottery draw.
     * @param count Number of entrants to select.
     * @param listener Callback to report completion or errors.
     */
    @Override
    public void drawLottery(String eventId, int count, OnLotteryCompleteListener listener) {
        // Fetch event to get name
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {

                    // Wrap eventName so it can be used inside lambdas
                    final String[] eventNameHolder = new String[1];
                    String rawName = eventDoc.getString("name");
                    if (rawName == null || rawName.trim().isEmpty()) {
                        eventNameHolder[0] = "this";
                    } else {
                        eventNameHolder[0] = rawName;
                    }

                    // Fetch all WAITING entrants for this event
                    db.collection("entrants")
                            .whereEqualTo("eventId", eventId)
                            .whereEqualTo("status", "WAITING")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<Entrant> waitingList = new ArrayList<>();
                                Map<String, String> entrantToUserId = new HashMap<>();

                                // Build local waiting list and map entrantId to userId
                                for (DocumentSnapshot doc : querySnapshot.getDocuments())    {
                                    Entrant entrant = doc.toObject(Entrant.class);
                                    if (entrant != null) {
                                        entrant.setId(doc.getId());
                                        waitingList.add(entrant);

                                        // capture entrantID for notifications
                                        String uid = doc.getString("userId");
                                        if (uid != null) {
                                            entrantToUserId.put(entrant.getId(), uid);
                                        }
                                    }
                                }

                                if (waitingList.isEmpty()) {
                                    listener.onFailure("Waiting list is empty");
                                    return;
                                }

                                // Shuffle to randomize selection
                                Collections.shuffle(waitingList);
                                int selected = Math.min(count, waitingList.size());
                                List<Entrant> winners = new ArrayList<>();
                                Set<String> winnerIds = new HashSet<>();

                                long now = System.currentTimeMillis();
                                NotificationRepository notifRepo = RepositoryProvider.getNotificationRepository();

                                // Handle winners: update status + send INVITED notifications
                                for (int i = 0; i < selected; i++) {
                                    Entrant winner = waitingList.get(i);
                                    String newStatus = (i < selected / 4) ? "ENROLLED" : "INVITED";

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("status", newStatus);
                                    updates.put("statusTimestamp", System.currentTimeMillis());

                                    db.collection("entrants").document(winner.getId()).update(updates);

                                    winner.setStatus(Entrant.Status.valueOf(newStatus));
                                    winners.add(winner);
                                    winnerIds.add(winner.getId());

                                    // US 01.04.01 - Notify chosen entrants
                                    String uid = entrantToUserId.get(winner.getId());
                                    if (uid != null) {
                                        String notificationId = eventId + ":" + winner.getId();
                                        String title = "You've been invited to " + eventNameHolder[0] + " event!";
                                        String message = "You were selected in the lottery for this event.";

                                        NotificationItem item = new NotificationItem(
                                                notificationId,
                                                NotificationItem.NotificationType.INVITED,
                                                null,
                                                uid,
                                                title,
                                                message,
                                                now
                                        );
                                        notifRepo.createNotification(uid, item, new RepositoryCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                            }
                                        });
                                    }
                                }

                                // US 01.04.02 - Notify entrants who were NOT chosen
                                for (Entrant entrant : waitingList) {
                                    if (winnerIds.contains(entrant.getId())) {
                                        continue;
                                    }
                                    String uid = entrantToUserId.get(entrant.getId());
                                    if (uid == null) continue;

                                    String notificationId = eventId + ":" + entrant.getId();
                                    String title = "Not selected in the " + eventNameHolder[0] + " draw.";
                                    String message = "You were not selected in the first draw, " +
                                            "but you may still be chosen if a spot opens.";

                                    NotificationItem item = new NotificationItem(
                                            notificationId,
                                            NotificationItem.NotificationType.WAITING,
                                            null,
                                            uid,
                                            title,
                                            message,
                                            now
                                    );

                                    notifRepo.createNotification(uid, item, new RepositoryCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                        }
                                    });
                                }

                                if (listener != null) {
                                    listener.onComplete(winners);
                                }
                            })
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });

    }

    /**
     * Cancels an entrant's participation by ID. Updates status in Firestore.
     * @param entrantId ID of the entrant to cancel.
     * @param listener Callback to signal success or failure.
     */
    @Override
    public void cancelEntrant(String entrantId, OnActionCompleteListener listener) {

        db.collection("entrants")
                .document(entrantId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        if (listener != null) {
                            listener.onFailure("Entrant not found");
                        }
                        return;
                    }

                    // Extract user + event IDs
                    String rawUserId = doc.getString("userId");
                    if (rawUserId == null) {
                        rawUserId = doc.getString("id"); // fallback
                    }
                    final String finalUserId = rawUserId;
                    final String finalEventId = doc.getString("eventId");
                    long now = System.currentTimeMillis();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "CANCELLED");
                    updates.put("statusTimestamp", now);

                    // Update entrant status to CANCELLED
                    db.collection("entrants").document(entrantId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {

                                if (finalUserId != null) {

                                    // Fetch event name for notification text
                                    final String[] eventNameHolder = new String[1];

                                    db.collection("events")
                                            .document(finalEventId)
                                            .get()
                                            .addOnSuccessListener(eventDoc -> {
                                                String eventName = eventDoc.getString("name");
                                                if (eventName == null || eventName.trim().isEmpty()) {
                                                    eventNameHolder[0] = "this";
                                                } else {
                                                    eventNameHolder[0] = eventName;
                                                }

                                                NotificationRepository notifRepo =
                                                        RepositoryProvider.getNotificationRepository();

                                                String notificationId =
                                                        (finalEventId != null ? finalEventId : "event")
                                                                + ":" + entrantId;

                                                String title = "Your registration for " + eventNameHolder[0] + " event was cancelled.";
                                                String message = "Your spot for this event has been cancelled.";

                                                NotificationItem item = new NotificationItem(
                                                        notificationId,
                                                        NotificationItem.NotificationType.CANCELLED,
                                                        null,
                                                        finalUserId,
                                                        title,
                                                        message,
                                                        now
                                                );

                                                notifRepo.createNotification(
                                                        finalUserId,
                                                        item,
                                                        new RepositoryCallback<Void>() {
                                                            @Override
                                                            public void onSuccess(Void result) {
                                                            }

                                                            @Override
                                                            public void onFailure(Exception e) {
                                                            }
                                                        }
                                                );
                                            });
                                }
                                if (listener != null) listener.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                if (listener != null) listener.onFailure(e.getMessage());
                            });

                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }


    /**
     * Draws replacement entrant from waiting list. Updates status in Firestore.
     * @param eventId ID of the event for replacement draw.
     * @param listener Callback to report result or errors.
     */
    @Override
    public void drawReplacement(String eventId, OnReplacementDrawnListener listener) {
        db.collection("entrants")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "WAITING")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Entrant> waitingList = new ArrayList<>();
                    Map<String, String> entrantToUserId = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            entrant.setId(doc.getId());
                            waitingList.add(entrant);

                            String uid = doc.getString("userId");
                            if (uid != null) {
                                entrantToUserId.put(entrant.getId(), uid);
                            }
                        }
                    }

                    if (waitingList.isEmpty()) {
                        if (listener != null) {
                            listener.onFailure("No entrants in waiting list");
                        }
                        return;
                    }

                    // Pick a random WAITING entrant
                    Collections.shuffle(waitingList);
                    Entrant replacement = waitingList.get(0);

                    long now = System.currentTimeMillis();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "INVITED");
                    updates.put("statusTimestamp", now);

                    db.collection("entrants").document(replacement.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                try {
                                    replacement.setStatus(Entrant.Status.INVITED);
                                } catch (IllegalArgumentException e) {
                                    // ignore if enum doesn't match
                                }

                                // Lookup event name before sending the notification
                                final String[] eventNameHolder = new String[1];

                                db.collection("events")
                                        .document(eventId)
                                        .get()
                                        .addOnSuccessListener(eventDoc -> {

                                            String raw = eventDoc.getString("name");
                                            if (raw == null || raw.trim().isEmpty()) {
                                                eventNameHolder[0] = "this event";
                                            } else {
                                                eventNameHolder[0] = raw;
                                            }

                                            // US 01.05.01 – notify second chance invitee
                                            String uid = entrantToUserId.get(replacement.getId());
                                            if (uid != null) {
                                                NotificationRepository notifRepo =
                                                        RepositoryProvider.getNotificationRepository();

                                                String notificationId = eventId + ":" + replacement.getId();
                                                String title = "You’ve been invited from the waiting list!";
                                                String message = "A spot opened up because someone declined. " +
                                                        "You now have a chance to join the " + eventNameHolder[0] + " event.";

                                                NotificationItem item = new NotificationItem(
                                                        notificationId,
                                                        NotificationItem.NotificationType.INVITED,
                                                        null,
                                                        uid,
                                                        title,
                                                        message,
                                                        now
                                                );

                                                notifRepo.createNotification(uid, item, new RepositoryCallback<Void>() {
                                                            @Override
                                                            public void onSuccess(Void result) {
                                                            }

                                                            @Override
                                                            public void onFailure(Exception e) {
                                                            }
                                                        }
                                                );
                                            }

                                            if (listener != null) {
                                                listener.onSuccess(replacement);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            if (listener != null) {
                                                listener.onFailure(e.getMessage());
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                if (listener != null) {
                                    listener.onFailure(e.getMessage());
                                }
                            });
                });
    }

    /**
     * Helper that looks up the current user document by id field
     *
     * @param deviceId device/user identifier to look up.
     * @param listener callback with id, name, and role if found.
     */
    @Override
    public void getCurrentUserInfo(String deviceId, OnUserInfoListener listener) {
        db.collection("users")
                .whereEqualTo("id", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        String hostId = userDoc.getString("id");
                        String hostName = userDoc.getString("name");
                        String role = userDoc.getString("role");
                        listener.onSuccess(hostId, hostName, role);
                    } else {
                        listener.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
}
