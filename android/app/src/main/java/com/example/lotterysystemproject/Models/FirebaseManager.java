package com.example.lotterysystemproject.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * FirebaseManager - central handler for Firestore and Storage operations.
 * Includes support for Users, Events, and Registrations (US 01.02.03).
 */
public class FirebaseManager {

    /** Firestore database instance. */
    private final FirebaseFirestore db;

    /** Singleton instance of the FirebaseManager. */
    private static FirebaseManager instance;

    /** Firebase Storage instance. */
    private final FirebaseStorage storage;

    /** Active Firestore listener for registration updates. */
    private ListenerRegistration activeRegsListener;

    /** Private constructor (singleton pattern). */
    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Returns the singleton instance of FirebaseManager.
     * Creates it if it does not exist.
     */
    public static FirebaseManager getInstance() {
        if (instance == null) {
            synchronized (FirebaseManager.class) {
                if (instance == null) {
                    instance = new FirebaseManager();
                }
            }
        }
        return instance;
    }

    /** Gets the Firestore database reference. */
    public FirebaseFirestore getDatabase() {
        return db;
    }

    /** Gets the Firebase Storage reference. */
    public FirebaseStorage getStorage() {
        return storage;
    }

    /** Retrieves the collection reference for a specific Firestore collection. */
    public CollectionReference getCollection(String collectionName) {
        return db.collection(collectionName);
    }

    // ===============================================================
    //  User Operations
    // ===============================================================

    /** Callback interface used for Firebase operations that do not return data. */
    public interface FirebaseCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /** Add a new user document to the "users" collection in Firestore. */
    public void addUser(User user, FirebaseCallback callback) {
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    /** Update an existing user's fields in Firestore. */
    public void updateUser(String userId, Map<String, Object> updates, FirebaseCallback callback) {
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    /** Retrieve a single user document from the "users" collection. */
    public void getUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (onSuccess != null) onSuccess.accept(user);
                    } else {
                        if (onError != null) onError.accept(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    /** Delete a user document from Firestore. */
    public void deleteUser(String userId, FirebaseCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null)
                callback.onError(new IllegalArgumentException("User ID cannot be null or empty"));
            return;
        }
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    // ===============================================================
    //  Registration / Event History Support (US 01.02.03)
    // ===============================================================

    /** Interface for real-time updates of a user's registrations. */
    public interface RegistrationsListener {
        void onChanged(List<Registration> items);
        void onError(Exception e);
    }

    /**
     * Listen to a user's registration history (real-time updates).
     * @param userId   The user's unique ID
     * @param listener Callback to receive updates
     */
    public void listenUserRegistrations(String userId, RegistrationsListener listener) {
        if (activeRegsListener != null) {
            activeRegsListener.remove();
            activeRegsListener = null;
        }

        activeRegsListener = db.collection("registrations")
                .whereEqualTo("userId", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        if (listener != null) listener.onError(err);
                        return;
                    }

                    List<Registration> out = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Registration r = d.toObject(Registration.class);
                            if (r != null) out.add(r);
                        }
                    }
                    if (listener != null) listener.onChanged(out);
                });
    }

    /** Stop listening for registration updates. */
    public void stopListeningUserRegistrations() {
        if (activeRegsListener != null) {
            activeRegsListener.remove();
            activeRegsListener = null;
        }
    }

    /**
     * Create or merge a registration document when a user joins a waiting list.
     * @param userId ID of the entrant
     * @param eventId ID of the event
     * @param eventTitleSnapshot event title at the time of registration
     * @param callback callback for success or failure
     */
    public void upsertRegistrationOnJoin(String userId, String eventId,
                                         String eventTitleSnapshot, FirebaseCallback callback) {

        String docId = userId + "_" + eventId;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("eventId", eventId);
        data.put("eventTitleSnapshot", eventTitleSnapshot);
        data.put("status", "JOINED");
        data.put("registeredAt", Timestamp.now());
        data.put("updatedAt", Timestamp.now());

        db.collection("registrations").document(docId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    /**
     * Update a user's registration status (e.g., SELECTED, DECLINED, NOT_SELECTED).
     * @param userId the user's ID
     * @param eventId the event's ID
     * @param status the new status
     * @param callback success/failure callback
     */
    public void updateRegistrationStatus(String userId, String eventId,
                                         String status, FirebaseCallback callback) {

        String docId = userId + "_" + eventId;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", Timestamp.now());

        db.collection("registrations").document(docId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }
}
