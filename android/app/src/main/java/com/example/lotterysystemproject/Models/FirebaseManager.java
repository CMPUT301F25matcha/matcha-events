package com.example.lotterysystemproject.Models;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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



    /**
     * Retrieve all documents from "events" collection in Firestore,
     * and converts them into a list of Event objects.
     * @param onSuccess a callback with the list of events if successful
     * @param onError a callback with an exception if operation fails
     */
    public void getAllEvents(Consumer<List<EventAdmin>> onSuccess, Consumer<Exception> onError) {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Create a list to store all Event objects
                    List<EventAdmin> eventAdminList = new ArrayList<>();

                    // Loop through all documents in the collection
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        // Convert each document to an Event object
                        EventAdmin eventAdmin = doc.toObject(EventAdmin.class);
                        eventAdminList.add(eventAdmin);
                    }

                    if (onSuccess != null) {
                        onSuccess.accept(eventAdminList);
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

    public void addEvent(EventAdmin eventAdmin, FirebaseCallback callback) {
        if (eventAdmin == null || eventAdmin.getId() == null || eventAdmin.getId().isEmpty()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("Event or EventID cannot be null"));
            }
            return;
        }
        db.collection("events").document(eventAdmin.getId())
                .set(eventAdmin)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    public void deleteEvent(String eventId, FirebaseCallback callback) {
        if (eventId == null || eventId.isEmpty()) {
            if(callback != null) {
                callback.onError(new IllegalArgumentException("Event ID cannot be null or empty"));
            }
            return;
        }
        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if
                    (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }


    public void listenToAllEvents(Consumer<List<EventAdmin>> onSuccess, Consumer<Exception> onError) {
        db.collection("events").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                if (onError != null) onError.accept(e);
                return;
            }

            List<EventAdmin> eventAdminList = new ArrayList<>();
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot doc: queryDocumentSnapshots) {
                    EventAdmin eventAdmin = doc.toObject(EventAdmin.class);
                    if (eventAdmin != null) {

                        eventAdminList.add(eventAdmin);

                    }

                }
            }

            if (onSuccess != null) onSuccess.accept(eventAdminList);

        });
    }

    public void getAllImages(Consumer<List<String>> onSuccess, Consumer<Exception> onError) {
        storage.getReference().child("images")
                .listAll()
                .addOnSuccessListener(listResult -> {
                    List<String> urls = new ArrayList<>();
                    List<StorageReference> items = listResult.getItems();

                    if (items.isEmpty()) {
                        if (onSuccess != null) onSuccess.accept(urls);
                        return;
                    }

                    for (StorageReference item: items) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            urls.add(uri.toString());
                            if (urls.size() == items.size() && onSuccess != null) {
                                onSuccess.accept(urls);
                            }
                        }).addOnFailureListener(e -> {
                            if (onError != null) onError.accept(e);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    public void deleteImage(String imageUrl, FirebaseCallback callback) {
        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError(e);
                    });
        } catch (Exception e) {
            if (callback != null) callback.onError(e);
        }
    }


    public void deleteMultipleImages(List<String> imageUrls, BiConsumer<Integer, Exception> onComplete) {

        int[] remaining = {imageUrls.size()};
        int[] deleteCount = {0};

        for (String url: imageUrls) {
            deleteImage(url, new FirebaseCallback() {
                @Override
                public void onSuccess() {
                    deleteCount[0]++;
                    if (--remaining[0] == 0 && onComplete != null) {
                        onComplete.accept(deleteCount[0], null);
                    }
                }
                @Override
                public void onError(Exception e) {
                    if (--remaining[0] == 0 && onComplete != null) {
                        onComplete.accept(deleteCount[0], e);
                    }
                }
            });
        }
    }
}