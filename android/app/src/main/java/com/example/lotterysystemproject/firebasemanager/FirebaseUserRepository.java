package com.example.lotterysystemproject.firebasemanager;

import android.net.Uri;

import com.example.lotterysystemproject.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore Storage implementation of UserRepository.
 */
public class FirebaseUserRepository implements UserRepository {

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    /**
     * Default constructor initializes Firestore and Storage singletons.
     */
    public FirebaseUserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Creates or updates a User document in the users collection.
     *
     * @param user user object to create or overwrite
     * @param callback callback invoked on success/failure
     */
    @Override
    public void createOrUpdateUser(User user, RepositoryCallback<Void> callback) {
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Loads a single User by document ID.
     *
     * @param userId Firestore user document ID
     * @param callback callback with User or error if failed
     */
    @Override
    public void getUserById(String userId, RepositoryCallback<User> callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (callback != null) callback.onSuccess(user);
                    } else {
                        if (callback != null) {
                            callback.onFailure(new Exception("User not found"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Hard-deletes a user document from the users collection.
     *
     * @param userId user document ID to delete
     * @param callback callback for success/failure
     */
    @Override
    public void deleteUser(String userId, RepositoryCallback<Void> callback) {
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Fetches all active users (where isActive == true).
     *
     * @param callback callback with list of Users or error
     */
    @Override
    public void getAllUsers(RepositoryCallback<List<User>> callback) {
        db.collection("users")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null) users.add(user);
                    });
                    if (callback != null) callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Updates the user's notification opt-in flag stored under the notificationsEnabled field on the user document.
     *
     * @param userId target user id
     * @param enabled true if notifications are enabled, false otherwise
     * @param callback callback for success/failure
     */
    @Override
    public void updateNotificationPreferences(String userId, boolean enabled, RepositoryCallback<Void> callback) {
        db.collection("users").document(userId)
                .update("notificationsEnabled", enabled)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Uploads a profile picture to Firebase Storage and updates user document with resulting download URL.
     *
     * @param userId user whose profile picture is being uploaded
     * @param imageUri local URI of image to upload
     * @param callback callback returning download URL on success
     */
    @Override
    public void uploadProfilePicture(String userId, Uri imageUri, RepositoryCallback<String> callback) {
        StorageReference ref = storage.getReference("profile_pictures/" + userId + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                db.collection("users").document(userId)
                                        .update("profilePictureUrl", imageUrl)
                                        .addOnSuccessListener(v -> {
                                            if (callback != null) callback.onSuccess(imageUrl);
                                        })
                                        .addOnFailureListener(e -> {
                                            if (callback != null) callback.onFailure(e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Deletes the user's profile picture from Firebase Storage and clears the profilePictureUrl field from user document.
     *
     * @param userId user whose picture should be removed
     * @param callback callback for success/failure
     */
    @Override
    public void deleteProfilePicture(String userId, RepositoryCallback<Void> callback) {
        StorageReference ref = storage.getReference("profile_pictures/" + userId + ".jpg");

        ref.delete()
                .addOnSuccessListener(v -> {
                    db.collection("users").document(userId)
                            .update("profilePictureUrl", null)
                            .addOnSuccessListener(v2 -> {
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

    /**
     * Performs a search across all active users matching the query against name or email.
     *
     * @param query search query
     * @param callback callback with matching users or error
     */
    @Override
    public void searchUsers(String query, RepositoryCallback<List<User>> callback) {
        db.collection("users")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    String lowerQuery = query.toLowerCase();

                    querySnapshot.forEach(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null && (
                                user.getName().toLowerCase().contains(lowerQuery) ||
                                        user.getEmail().toLowerCase().contains(lowerQuery)
                        )) {
                            users.add(user);
                        }
                    });
                    if (callback != null) callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Subscribes to real-time updates on a single user document.
     *
     * @param userId user id to listen to
     * @param listener listener notified with data or errors
     */
    @Override
    public void listenToUser(String userId, RepositoryListener<User> listener) {
        db.collection("users").document(userId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        if (listener != null) listener.onError(error);
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (listener != null) listener.onDataChanged(user);
                    }
                });
    }

    /**
     * Fully deactivates a user account and cleans up associated data.
     *
     * @param userId id of the user to deactivate
     * @param callback callback invoked after batch commit or on error
     */
    @Override
    public void deactivateAccount(String userId, RepositoryCallback<Void> callback) {
        if (userId == null || userId.trim().isEmpty()) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("userId is empty"));
            }
            return;
        }

        // 1) Fetch all notifications for this user
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(notifSnap -> {

                    // 2) Then fetch all entrants for this user
                    db.collection("entrants")
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(entrantSnap -> {

                                // 3) Build a batch delete for notifications, entrants, and the user doc
                                com.google.firebase.firestore.WriteBatch batch = db.batch();

                                // Track events that user was in
                                java.util.Set<String> affectedEventIds = new java.util.HashSet<>();

                                // Track how many WAITING / ENROLLED entrants removed per event
                                java.util.Map<String, Integer> waitingCountsByEvent  = new java.util.HashMap<>();
                                java.util.Map<String, Integer> enrolledCountsByEvent = new java.util.HashMap<>();

                                // Delete notifications
                                for (com.google.firebase.firestore.DocumentSnapshot doc : notifSnap.getDocuments()) {
                                    batch.delete(doc.getReference());
                                }

                                // Delete entrants and collect eventIds
                                for (com.google.firebase.firestore.DocumentSnapshot doc : entrantSnap.getDocuments()) {
                                    batch.delete(doc.getReference());

                                    String eventId = doc.getString("eventId");
                                    if (eventId != null && !eventId.isEmpty()) {
                                        affectedEventIds.add(eventId);

                                        // Look at status to adjust counters correctly
                                        String status = doc.getString("status");
                                        if ("WAITING".equals(status)) {
                                            int prev = waitingCountsByEvent.containsKey(eventId)
                                                    ? waitingCountsByEvent.get(eventId) : 0;
                                            waitingCountsByEvent.put(eventId, prev + 1);
                                        } else if ("ENROLLED".equals(status)) {
                                            int prev = enrolledCountsByEvent.containsKey(eventId)
                                                    ? enrolledCountsByEvent.get(eventId) : 0;
                                            enrolledCountsByEvent.put(eventId, prev + 1);
                                        }
                                    }
                                }

                                // Remove respective userId from each event waitingList
                                // and decrement currentWaitingCount / currentEnrolled
                                for (String eventId : affectedEventIds) {
                                    com.google.firebase.firestore.DocumentReference eventRef =
                                            db.collection("events").document(eventId);

                                    java.util.Map<String, Object> eventUpdates = new java.util.HashMap<>();

                                    // remove the userId from the event's waitingList array
                                    eventUpdates.put(
                                            "waitingList",
                                            com.google.firebase.firestore.FieldValue.arrayRemove(userId)
                                    );

                                    // Decrement waiting count if we removed WAITING entrants for this event
                                    Integer waitingRemoved = waitingCountsByEvent.get(eventId);
                                    if (waitingRemoved != null && waitingRemoved > 0) {
                                        eventUpdates.put(
                                                "currentWaitingCount",
                                                com.google.firebase.firestore.FieldValue.increment(-waitingRemoved)
                                        );
                                    }

                                    // Decrement enrolled count if we removed ENROLLED entrants for this event
                                    Integer enrolledRemoved = enrolledCountsByEvent.get(eventId);
                                    if (enrolledRemoved != null && enrolledRemoved > 0) {
                                        eventUpdates.put(
                                                "currentEnrolled",
                                                com.google.firebase.firestore.FieldValue.increment(-enrolledRemoved)
                                        );
                                    }

                                    batch.update(eventRef, eventUpdates);
                                }

                                // Finally, delete user document itself
                                com.google.firebase.firestore.DocumentReference userRef =
                                        db.collection("users").document(userId);
                                batch.delete(userRef);

                                // 4) Commit the batch
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            if (callback != null) {
                                                callback.onSuccess(null);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            if (callback != null) {
                                                callback.onFailure(e);
                                            }
                                        });

                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) {
                                    callback.onFailure(e);
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Exports the given user's data as a JSON string.
     *
     * @param userId user to export
     * @param callback callback with JSON string or error
     */
    @Override
    public void exportUserData(String userId, RepositoryCallback<String> callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        String jsonData = userToJson(user);
                        if (callback != null) callback.onSuccess(jsonData);
                    } else {
                        if (callback != null) callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Helper that formats a User into a basic JSON string.
     *
     * @param user User model to export
     * @return JSON representation of the user
     */
    private String userToJson(User user) {
        return "{\n" +
                "  \"id\": \"" + user.getId() + "\",\n" +
                "  \"name\": \"" + user.getName() + "\",\n" +
                "  \"email\": \"" + user.getEmail() + "\",\n" +
                "  \"phone\": \"" + user.getPhone() + "\",\n" +
                "  \"role\": \"" + user.getRole() + "\"\n" +
                "}";
    }
}
