package com.example.lotterysystemproject.Models;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.Timestamp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;
import java.util.function.Consumer;


public class FirebaseManager {


    /** Firestore database instance. */
    private final FirebaseFirestore db;

    /** Singleton instance of the FirebaseManager. */
    private static FirebaseManager instance;

    /** Firebase Storage instance. */
    private final FirebaseStorage storage;

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }


    /**
     * Returns the singleton instance of FirebaseManager.
     * Create it if it does not exist.
     * @return the shared FirebaseManager instance
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

    /**
     * Gets the Firestore database reference.
     *
     * @return the FirebaseFirestore instance.
     */
    public FirebaseFirestore getDatabase() {
        return db;
    }


    /**
     * Gets the Firebase Storage reference.
     *
     * @return the FirebaseStorage instance
     */
    public FirebaseStorage getStorage() {
        return storage;
    }

    /**
     * Retrieves the collection reference for a specific Firestore collection.
     * @param collectionName the name fo the collection
     * @return a collectionReference to the specified collection
     */
    public CollectionReference getCollection(String collectionName) {
        return db.collection(collectionName);
    }

    /**
     * Callback interface used for Firebase operations that do not return data.
     *
     */
    public interface FirebaseCallback {

        /**
         * Called when the Firebase operation completes successfully.
         */
        void onSuccess();

        /**
         * Called when the Firebase operation fails.
         * @param e the exception describing the failure
         */
        void onError(Exception e);
    }

    /**
     * Add a new user document to the "userse" collection in Firestore.
     * @param user the user object to be added
     * @param callback a callback to handle success or failures
     */
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


    /**
     * Update an existing user's field in Firestore
     * @param userId the ID of the user document to update
     * @param updates the map of fields and their new values
     * @param callback a callback to handle success or failure
     */
    public void updateUser(String userId, Map<String, Object> updates, FirebaseCallback callback){
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    /**
     * Retrieve a single user document from the "users" collection.
     *
     * @param userId the ID of the user to retrieve
     * @param onSuccess a callback with the User object if it is found
     * @param onError a callback with an exception if User is not found or failure
     */
    public void getUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        //  Convert Firestore document into a User object
                        User user = doc.toObject(User.class);

                        if (onSuccess != null) {
                            onSuccess.accept(user);
                        }
                    } else {
                        // Document not found on Firestore
                        if (onError != null) {
                            onError.accept(new Exception("User not found"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
    }


    public void deleteUser(String userId, FirebaseCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("User ID cannot be null or empty"));
            }
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

    /** Anonymize all registrations for a user (but keeps event stats, just removes personal linkage). */
    public void anonymizeRegistrationsForUser(String userId, FirebaseCallback callback) {
        db.collection("registrations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(q -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot d : q) {
                        batch.update(d.getReference(), new java.util.HashMap<String, Object>() {{
                            put("userId", "DELETED");          // break linkage
                            put("userDeleted", true);          // deletion marker
                            put("updatedAt", Timestamp.now());
                        }});
                    }
                    batch.commit()
                            .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(); })
                            .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
                })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
    }

    /** Best-effort FCM token revoke. Call from Activity. */
    public void revokeFcmToken(FirebaseCallback callback) {
        FirebaseMessaging.getInstance().deleteToken()
                .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
    }




}
