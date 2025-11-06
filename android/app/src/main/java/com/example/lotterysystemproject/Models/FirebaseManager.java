package com.example.lotterysystemproject.Models;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.Timestamp;
import com.google.firebase.messaging.FirebaseMessaging;
// Firebase imports commented out for demo with local data
// import com.google.firebase.firestore.CollectionReference;
// import com.google.firebase.firestore.DocumentSnapshot;
// import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.storage.FirebaseStorage;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class FirebaseManager {


    /** Firestore database instance. */
    // private final FirebaseFirestore db;

    /** Singleton instance of the FirebaseManager. */
    private static FirebaseManager instance;

    /** Firebase Storage instance. */
    // private final FirebaseStorage storage;

    /** Mock data storage for events */
    private final Map<String, Event> mockEvents;
    private final List<String> mockEventIds;
    private final Handler mainHandler;

    private FirebaseManager() {
        // db = FirebaseFirestore.getInstance();
        // storage = FirebaseStorage.getInstance();
        mockEvents = new HashMap<>();
        mockEventIds = new ArrayList<>();
        mainHandler = new Handler(Looper.getMainLooper());
        initializeMockData();
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
    // public FirebaseFirestore getDatabase() {
    //     return db;
    // }


    /**
     * Gets the Firebase Storage reference.
     *
     * @return the FirebaseStorage instance
     */
    // public FirebaseStorage getStorage() {
    //     return storage;
    // }

    /**
     * Retrieves the collection reference for a specific Firestore collection.
     * @param collectionName the name fo the collection
     * @return a collectionReference to the specified collection
     */
    // public CollectionReference getCollection(String collectionName) {
    //     return db.collection(collectionName);
    // }

    /**
     * Initialize mock event data for demo purposes.
     */
    private void initializeMockData() {
        Calendar cal = Calendar.getInstance();

        // Event 1: Music Concert
        cal.set(2024, Calendar.DECEMBER, 20, 19, 0);
        Event event1 = new Event("event1", "Winter Music Festival", "Music Promoters Inc", "host1", cal.getTime(), "Downtown Concert Hall");
        event1.setDescription("Join us for an amazing winter music festival featuring top artists!");
        event1.setCategory("Music");
        event1.setMaxCapacity(500);
        event1.setCurrentCapacity(350);
        event1.setImageUrl("");
        mockEvents.put("event1", event1);
        mockEventIds.add("event1");

        // Event 2: Tech Conference
        cal.set(2024, Calendar.DECEMBER, 22, 9, 0);
        Event event2 = new Event("event2", "Tech Innovation Summit 2024", "Tech Hub", "host2", cal.getTime(), "Convention Center");
        event2.setDescription("Explore the latest in technology and innovation with industry leaders.");
        event2.setCategory("Tech");
        event2.setMaxCapacity(300);
        event2.setCurrentCapacity(280);
        event2.setImageUrl("");
        mockEvents.put("event2", event2);
        mockEventIds.add("event2");

        // Event 3: Food & Wine Festival
        cal.set(2024, Calendar.DECEMBER, 18, 12, 0);
        Event event3 = new Event("event3", "Food & Wine Festival", "Culinary Events Co", "host3", cal.getTime(), "City Park");
        event3.setDescription("Taste amazing dishes and wines from local and international vendors.");
        event3.setCategory("Food");
        event3.setMaxCapacity(400);
        event3.setCurrentCapacity(400); // Full capacity
        event3.setImageUrl("");
        mockEvents.put("event3", event3);
        mockEventIds.add("event3");

        // Event 4: Art Exhibition
        cal.set(2024, Calendar.DECEMBER, 25, 10, 0);
        Event event4 = new Event("event4", "Modern Art Exhibition", "Art Gallery Downtown", "host4", cal.getTime(), "Art Museum");
        event4.setDescription("Discover contemporary art pieces from emerging and established artists.");
        event4.setCategory("Art");
        event4.setMaxCapacity(200);
        event4.setCurrentCapacity(120);
        event4.setImageUrl("");
        mockEvents.put("event4", event4);
        mockEventIds.add("event4");

        // Event 5: Sports Tournament
        cal.set(2024, Calendar.DECEMBER, 19, 14, 0);
        Event event5 = new Event("event5", "Charity Basketball Tournament", "Sports Community Org", "host5", cal.getTime(), "Sports Arena");
        event5.setDescription("Watch exciting basketball games while supporting local charities.");
        event5.setCategory("Sports");
        event5.setMaxCapacity(1000);
        event5.setCurrentCapacity(750);
        event5.setImageUrl("");
        mockEvents.put("event5", event5);
        mockEventIds.add("event5");

        // Event 6: Educational Workshop
        cal.set(2024, Calendar.DECEMBER, 21, 13, 0);
        Event event6 = new Event("event6", "Digital Marketing Workshop", "Learn Academy", "host6", cal.getTime(), "Business Center");
        event6.setDescription("Learn advanced digital marketing strategies from industry experts.");
        event6.setCategory("Education");
        event6.setMaxCapacity(150);
        event6.setCurrentCapacity(90);
        event6.setImageUrl("");
        mockEvents.put("event6", event6);
        mockEventIds.add("event6");
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
    // COMMENTED OUT FOR DEMO - Using local data instead
    // public void addUser(User user, FirebaseCallback callback) {
    //     db.collection("users").document(user.getId())
    //             .set(user)
    //             .addOnSuccessListener(aVoid -> {
    //                 if (callback != null) callback.onSuccess();
    //             })
    //             .addOnFailureListener(e -> {
    //                 if (callback != null) callback.onError(e);
    //             });
    // }

    // Mock implementation for demo
    public void addUser(User user, FirebaseCallback callback) {
        // Simulate async operation with a delay
        new Thread(() -> {
            try {
                Thread.sleep(300); // Simulate network delay
                if (callback != null) callback.onSuccess();
            } catch (InterruptedException e) {
                if (callback != null) callback.onError(e);
            }
        }).start();
    }


    /**
     * Update an existing user's field in Firestore
     * @param userId the ID of the user document to update
     * @param updates the map of fields and their new values
     * @param callback a callback to handle success or failure
     */
    // COMMENTED OUT FOR DEMO - Using local data instead
    // public void updateUser(String userId, Map<String, Object> updates, FirebaseCallback callback){
    //     db.collection("users").document(userId)
    //             .update(updates)
    //             .addOnSuccessListener(aVoid -> {
    //                 if (callback != null) callback.onSuccess();
    //             })
    //             .addOnFailureListener(e -> {
    //                 if (callback != null) callback.onError(e);
    //             });
    // }

    // Mock implementation for demo
    public void updateUser(String userId, Map<String, Object> updates, FirebaseCallback callback){
        new Thread(() -> {
            try {
                Thread.sleep(300);
                if (callback != null) callback.onSuccess();
            } catch (InterruptedException e) {
                if (callback != null) callback.onError(e);
            }
        }).start();
    }

    /**
     * Retrieve a single user document from the "users" collection.
     *
     * @param userId the ID of the user to retrieve
     * @param onSuccess a callback with the User object if it is found
     * @param onError a callback with an exception if User is not found or failure
     */
    // COMMENTED OUT FOR DEMO - Using local data instead
    // public void getUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError) {
    //     db.collection("users").document(userId)
    //             .get()
    //             .addOnSuccessListener(doc -> {
    //                 if (doc.exists()) {
    //                     //  Convert Firestore document into a User object
    //                     User user = doc.toObject(User.class);
    //
    //                     if (onSuccess != null) {
    //                         onSuccess.accept(user);
    //                     }
    //                 } else {
    //                     // Document not found on Firestore
    //                     if (onError != null) {
    //                         onError.accept(new Exception("User not found"));
    //                     }
    //                 }
    //             })
    //             .addOnFailureListener(e -> {
    //                 if (onError != null) {
    //                     onError.accept(e);
    //                 }
    //             });
    // }

    // Mock implementation for demo
    public void getUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError) {
        new Thread(() -> {
            try {
                Thread.sleep(300);
                // Return a mock user
                User mockUser = new User(userId, "Demo User", "demo@example.com", "123-456-7890");
                if (onSuccess != null) {
                    onSuccess.accept(mockUser);
                }
            } catch (InterruptedException e) {
                if (onError != null) {
                    onError.accept(e);
                }
            }
        }).start();
    }

    /**
     * Retrieve all documents from "events" collection in Firestore,
     * and converts them into a list of Event objects.
     * Only returns active events.
     * @param onSuccess a callback with the list of events if successful
     * @param onError a callback with an exception if operation fails
     */
    // COMMENTED OUT FOR DEMO - Using local mock data instead
    // public void getAllEvents(Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
    //     db.collection("events")
    //             .whereEqualTo("isActive", true)
    //             .get()
    //             .addOnSuccessListener(queryDocumentSnapshots -> {
    //                 // Create a list to store all Event objects
    //                 List<Event> eventList = new ArrayList<>();
    //
    //                 // Loop through all documents in the collection
    //                 for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
    //                     // Convert each document to an Event object
    //                     Event event = doc.toObject(Event.class);
    //                     if (event != null) {
    //                         event.setId(doc.getId()); // Set the document ID
    //                         eventList.add(event);
    //                     }
    //                 }
    //
    //                 if (onSuccess != null) {
    //                     onSuccess.accept(eventList);
    //                 }
    //             })
    //             .addOnFailureListener(e -> {
    //                 if (onError != null) {
    //                     onError.accept(e);
    //                 }
    //             });
    // }

    // Mock implementation for demo - returns local mock events
    public void getAllEvents(Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        // Simulate async operation with a delay
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate network delay
                List<Event> eventList = new ArrayList<>();
                // Return only active events
                for (String eventId : mockEventIds) {
                    Event event = mockEvents.get(eventId);
                    if (event != null && event.isActive()) {
                        eventList.add(event);
                    }
                }
                // Post to main thread for UI updates
                final List<Event> finalEventList = eventList;
                mainHandler.post(() -> {
                    if (onSuccess != null) {
                        onSuccess.accept(finalEventList);
                    }
                });
            } catch (InterruptedException e) {
                mainHandler.post(() -> {
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
            }
        }).start();
    }

    /**
     * Add a user to an event's waiting list.
     * @param eventId the ID of the event
     * @param userId the ID of the user to add to the waiting list
     * @param callback a callback to handle success or failure
     */
    // COMMENTED OUT FOR DEMO - Using local mock data instead
    // public void joinWaitingList(String eventId, String userId, FirebaseCallback callback) {
    //     if (eventId == null || eventId.isEmpty() || userId == null || userId.isEmpty()) {
    //         if (callback != null) {
    //             callback.onError(new IllegalArgumentException("Event ID and User ID cannot be null or empty"));
    //         }
    //         return;
    //     }
    //
    //     db.collection("events").document(eventId)
    //             .get()
    //             .addOnSuccessListener(doc -> {
    //                 if (doc.exists()) {
    //                     Event event = doc.toObject(Event.class);
    //                     if (event != null) {
    //                         // Check if user is already on waiting list or is a participant
    //                         if (event.isUserOnWaitingList(userId)) {
    //                             if (callback != null) {
    //                                 callback.onError(new Exception("User is already on the waiting list"));
    //                             }
    //                             return;
    //                         }
    //                         if (event.isUserParticipant(userId)) {
    //                             if (callback != null) {
    //                                 callback.onError(new Exception("User is already a participant"));
    //                             }
    //                             return;
    //                         }
    //
    //                         // Add user to waiting list
    //                         List<String> waitingList = event.getWaitingList();
    //                         if (waitingList == null) {
    //                             waitingList = new ArrayList<>();
    //                         }
    //                         waitingList.add(userId);
    //
    //                         // Update the event document
    //                         Map<String, Object> updates = new java.util.HashMap<>();
    //                         updates.put("waitingList", waitingList);
    //
    //                         db.collection("events").document(eventId)
    //                                 .update(updates)
    //                                 .addOnSuccessListener(aVoid -> {
    //                                     if (callback != null) callback.onSuccess();
    //                                 })
    //                                 .addOnFailureListener(e -> {
    //                                     if (callback != null) callback.onError(e);
    //                                 });
    //                     } else {
    //                         if (callback != null) {
    //                             callback.onError(new Exception("Failed to parse event data"));
    //                         }
    //                     }
    //                 } else {
    //                     if (callback != null) {
    //                         callback.onError(new Exception("Event not found"));
    //                     }
    //                 }
    //             })
    //             .addOnFailureListener(e -> {
    //                 if (callback != null) {
    //                     callback.onError(e);
    //                 }
    //             });
    // }

    // Mock implementation for demo - uses local mock events
    public void joinWaitingList(String eventId, String userId, FirebaseCallback callback) {
        if (eventId == null || eventId.isEmpty() || userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("Event ID and User ID cannot be null or empty"));
            }
            return;
        }

        // Simulate async operation
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate network delay

                Event event = mockEvents.get(eventId);
                if (event == null) {
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onError(new Exception("Event not found"));
                        }
                    });
                    return;
                }

                // Check if user is already on waiting list or is a participant
                if (event.isUserOnWaitingList(userId)) {
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onError(new Exception("User is already on the waiting list"));
                        }
                    });
                    return;
                }
                if (event.isUserParticipant(userId)) {
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onError(new Exception("User is already a participant"));
                        }
                    });
                    return;
                }

                // Add user to waiting list in mock data
                List<String> waitingList = event.getWaitingList();
                if (waitingList == null) {
                    waitingList = new ArrayList<>();
                    event.setWaitingList(waitingList);
                }
                waitingList.add(userId);

                // Update the mock event
                mockEvents.put(eventId, event);

                // Post success to main thread
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (InterruptedException e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        }).start();
    }

    // COMMENTED OUT FOR DEMO - Using local data instead
    // public void deleteUser(String userId, FirebaseCallback callback) {
    //     if (userId == null || userId.isEmpty()) {
    //         if (callback != null) {
    //             callback.onError(new IllegalArgumentException("User ID cannot be null or empty"));
    //         }
    //         return;
    //     }
    //     db.collection("users").document(userId)
    //             .delete()
    //             .addOnSuccessListener(aVoid -> {
    //                 if (callback != null) callback.onSuccess();
    //             })
    //             .addOnFailureListener(e -> {
    //                 if (callback != null) callback.onError(e);
    //             });
    // }

    // Mock implementation for demo
    public void deleteUser(String userId, FirebaseCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("User ID cannot be null or empty"));
            }
            return;
        }
        new Thread(() -> {
            try {
                Thread.sleep(300);
                if (callback != null) callback.onSuccess();
            } catch (InterruptedException e) {
                if (callback != null) callback.onError(e);
            }
        }).start();
    }

    public static void leaveWaitingList(Event event, String userId, FirebaseCallback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (event == null || userId == null) {
                if (callback != null) callback.onError(new IllegalArgumentException("event/userId is null"));
                return;
            }
            if (event.getWaitingList() != null && event.getWaitingList().contains(userId)) {
                event.getWaitingList().remove(userId);
                if (callback != null) callback.onSuccess();
            } else {
                if (callback != null) callback.onError(new Exception("User not in waiting list"));
            }
        }, 400);
    }

    // COMMENTED OUT FOR DEMO - Using local data instead
    // /** Anonymize all registrations for a user (but keeps event stats, just removes personal linkage). */
    // public void anonymizeRegistrationsForUser(String userId, FirebaseCallback callback) {
    //    db.collection("registrations")
    //            .whereEqualTo("userId", userId)
    //            .get()
    //            .addOnSuccessListener(q -> {
    //                WriteBatch batch = db.batch();
    //                for (QueryDocumentSnapshot d : q) {
    //                    batch.update(d.getReference(), new java.util.HashMap<String, Object>() {{
    //                        put("userId", "DELETED");          // break linkage
    //                        put("userDeleted", true);          // deletion marker
    //                        put("updatedAt", Timestamp.now());
    //                    }});
    //                }
    //               batch.commit()
    //                      .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(); })
    //                      .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
    //          })
    //            .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
    //}


    // COMMENTED OUT FOR DEMO - Using local data instead
    // /** Best-effort FCM token revoke. Call from Activity. */
    //public void revokeFcmToken(FirebaseCallback callback) {
    //    FirebaseMessaging.getInstance().deleteToken()
    //            .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(); })
    //            .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
    //}





}
