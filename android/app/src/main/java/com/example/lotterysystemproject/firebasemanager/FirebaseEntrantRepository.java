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
                .orderBy("joinedTimestamp", Query.Direction.ASCENDING)
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

                            // capture entrantID for notifications
                            String uid = doc.getString("id");
                            if (uid != null) {
                                entrantToUserId.put(entrant.getId(), uid);
                            }
                        }
                    }

                    if (waitingList.isEmpty()) {
                        listener.onFailure("Waiting list is empty");
                        return;
                    }

                    Collections.shuffle(waitingList);
                    int selected = Math.min(count, waitingList.size());
                    List<Entrant> winners = new ArrayList<>();
                    Set<String> winnerIds = new HashSet<>();

                    long now = System.currentTimeMillis();
                    NotificationRepository notifRepo = RepositoryProvider.getNotificationRepository();

                    for (int i = 0; i < selected; i++) {
                        Entrant winner = waitingList.get(i);
                        String newStatus = (i < selected / 4) ? "ENROLLED" : "INVITED";

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", newStatus);
                        updates.put("statusTimestamp", System.currentTimeMillis());

                        db.collection("entrants").document(winner.getId()).update(updates);

                        winner.setStatus(Entrant.Status.valueOf(newStatus));
                        winners.add(winner);

                        // US 01.04.01 - Notify chosen entrants
                        String uid = entrantToUserId.get(winner.getId());
                        if (uid != null) {
                            String notificationId = eventId + ":" + winner.getId();
                            String title = "You've been invited!";
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
                                    // log error later
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
                        String title = "Not selected in this draw";
                        String message = "You were not selected in the first draw, " +
                                "but you may still be chosen if a spot opens.";

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
                                // log error later
                            }
                        });
                    }

                    if (listener != null) {
                        listener.onComplete(winners);
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
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
                        rawUserId = doc.getString("id");   // fallback
                    }
                    final String finalUserId = rawUserId;

                    final String finalEventId = doc.getString("eventId");

                    long now = System.currentTimeMillis();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "CANCELLED");
                    updates.put("statusTimestamp", now);

                    // Update status
                    db.collection("entrants").document(entrantId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {

                                if (finalUserId != null) {

                                    NotificationRepository notifRepo =
                                            RepositoryProvider.getNotificationRepository();

                                    String notificationId =
                                            (finalEventId != null ? finalEventId : "event")
                                                    + ":" + entrantId;

                                    String title = "Your registration was cancelled";
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
                                                    // log error later
                                                }
                                            }
                                    );
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

                                // US 01.05.01 – notify second chance invitee
                                String uid = entrantToUserId.get(replacement.getId());
                                if (uid != null) {
                                    NotificationRepository notifRepo =
                                            RepositoryProvider.getNotificationRepository();

                                    String notificationId = eventId + ":" + replacement.getId();
                                    String title = "You’ve been invited from the waiting list!";
                                    String message = "A spot opened up because someone declined. " +
                                            "You now have a chance to join this event.";

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
                                            // log error later
                                        }
                                    });
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
    }



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
