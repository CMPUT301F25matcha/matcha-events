package com.example.lotterysystemproject.firebasemanager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lotterysystemproject.models.Entrant;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
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
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            entrant.setId(doc.getId());
                            waitingList.add(entrant);
                        }
                    }

                    if (waitingList.isEmpty()) {
                        listener.onFailure("Waiting list is empty");
                        return;
                    }

                    Collections.shuffle(waitingList);
                    int selected = Math.min(count, waitingList.size());
                    List<Entrant> winners = new ArrayList<>();

                    for (int i = 0; i < selected; i++) {
                        Entrant winner = waitingList.get(i);
                        String newStatus = (i < selected / 4) ? "ENROLLED" : "INVITED";

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", newStatus);
                        updates.put("statusTimestamp", System.currentTimeMillis());

                        db.collection("entrants").document(winner.getId()).update(updates);

                        winner.setStatus(Entrant.Status.valueOf(newStatus));
                        winners.add(winner);
                    }

                    listener.onComplete(winners);
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
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CANCELLED");
        updates.put("statusTimestamp", System.currentTimeMillis());

        db.collection("entrants").document(entrantId)
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
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
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            entrant.setId(doc.getId());
                            waitingList.add(entrant);
                        }
                    }

                    if (waitingList.isEmpty()) {
                        listener.onFailure("No entrants in waiting list");
                        return;
                    }

                    Collections.shuffle(waitingList);
                    Entrant replacement = waitingList.get(0);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "INVITED");
                    updates.put("statusTimestamp", System.currentTimeMillis());

                    db.collection("entrants").document(replacement.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                replacement.setStatus(Entrant.Status.INVITED);
                                listener.onSuccess(replacement);
                            })
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
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
