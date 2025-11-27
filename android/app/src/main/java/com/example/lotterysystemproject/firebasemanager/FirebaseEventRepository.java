package com.example.lotterysystemproject.firebasemanager;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.models.Registration;
import com.example.lotterysystemproject.models.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Firebase implementation of EventRepository for production use.
 * Connects to a real Firebase Firestore and Storage backend.
 */
public class FirebaseEventRepository implements EventRepository {

    // ===================== FIREBASE INSTANCES =====================
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    /** Firestore listener registration for managing live updates. */
    private ListenerRegistration activeRegsListener;

    /**
     * Constructor initializes Firebase instances.
     */
    public FirebaseEventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    // ===================== ACCESSORS =====================

    @Nullable
    @Override
    public FirebaseFirestore getDatabase() {
        return db;
    }

    @Nullable
    @Override
    public FirebaseStorage getStorage() {
        return storage;
    }

    @Override
    public CollectionReference getCollection(String name) {
        return db.collection(name);
    }

    // ===================== USER OPERATIONS =====================

    @Override
    public void addUser(User user, RepositoryCallback callback) {
        android.util.Log.d("Firebase", "Saving user: " + user.getId());
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(v -> {
                    android.util.Log.d("Firebase", "User saved successfully: " + user.getId());
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("Firebase", "Failed to save user", e);
                    if (callback != null) callback.onError(e);
                });
    }

    @Override
    public void updateUser(String userId, Map<String, Object> updates, RepositoryCallback callback) {
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    @Override
    public void getUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User u = doc.toObject(User.class);
                        if (onSuccess != null) onSuccess.accept(u);
                    } else {
                        if (onError != null) onError.accept(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    @Override
    public void deleteUser(String userId, RepositoryCallback callback) {
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    @Override
    public void updateUserRoleToOrganizer(String userId, RepositoryCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("role", "organizer");
        updates.put("updatedAt", new Date());

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    // ===================== EVENT OPERATIONS =====================

    @Override
    public LiveData<List<Event>> getAllEvents() {
        MutableLiveData<List<Event>> liveData = new MutableLiveData<>();

        db.collection("events")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(q -> {
                    List<Event> out = new ArrayList<>();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        Event e = d.toObject(Event.class);
                        if (e != null) {
                            e.setId(d.getId());
                            out.add(e);
                        }
                    }
                    liveData.setValue(out);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(null); // or handle error differently
                    // Optional: log the error
                    Log.e("Repository", "Error fetching events", e);
                });

        return liveData;
    }
    @Override
    public void addEvent(Event event, Consumer<Exception> onError) {
        db.collection("events")
                .add(event)
                .addOnFailureListener(e -> {
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
    }

    @Override
    public void joinWaitingList(String eventId, String userId, RepositoryCallback callback) {
        if (eventId == null || eventId.isEmpty() || userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError(new IllegalArgumentException("Event ID and User ID required"));
            return;
        }

        // Step 1: Get the event document
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        if (callback != null) callback.onError(new Exception("Event not found"));
                        return;
                    }

                    Event event = eventDoc.toObject(Event.class);
                    if (event == null) {
                        if (callback != null) callback.onError(new Exception("Failed to parse event"));
                        return;
                    }
                    List<String> waitingList = event.getWaitingList();
                    // Step 2: Validate event state and capacity
                    if (!event.isActive()) {
                        if (callback != null) callback.onError(new Exception("Event is not active"));
                        return;
                    }

                    // Check if registration period is open (if dates are set)
                    Date now = new Date();
                    if (event.getRegistrationStart() != null && now.before(event.getRegistrationStart())) {
                        if (callback != null) callback.onError(new Exception("Registration has not started yet"));
                        return;
                    }

                    if (event.getRegistrationEnd() != null && now.after(event.getRegistrationEnd())) {
                        if (callback != null) callback.onError(new Exception("Registration period has ended"));
                        return;
                    }

                    // Check if waiting list has capacity limit (US 02.03.01)
                    if (waitingList == null) {
                        waitingList = new ArrayList<>();
                    }

                    if (event.getMaxWaitingListSize() != 0 &&
                            event.getMaxWaitingListSize() > 0 &&
                            waitingList.size() >= event.getMaxWaitingListSize()) {
                        if (callback != null) callback.onError(new Exception("Waiting list is full"));
                        return;
                    }

                    // Check if user is already on waiting list
                    if (waitingList.contains(userId)) {
                        if (callback != null) callback.onError(new Exception("Already on waiting list"));
                        return;
                    }

                    // Step 3: Get user information for the entrant record
                    List<String> finalWaitingList = waitingList;
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(userDoc -> {
                                if (!userDoc.exists()) {
                                    if (callback != null) callback.onError(new Exception("User not found"));
                                    return;
                                }

                                User user = userDoc.toObject(User.class);
                                if (user == null) {
                                    if (callback != null) callback.onError(new Exception("Failed to parse user"));
                                    return;
                                }

                                // Step 4: Create Entrant record
                                String entrantId = userId + "_" + eventId;
                                long currentTime = System.currentTimeMillis();

                                Map<String, Object> entrantData = new HashMap<>();
                                entrantData.put("id", entrantId);
                                entrantData.put("userId", userId);
                                entrantData.put("eventId", eventId);
                                entrantData.put("name", user.getName() != null ? user.getName() : "");
                                entrantData.put("email", user.getEmail() != null ? user.getEmail() : "");
                                entrantData.put("phone", user.getPhone() != null ? user.getPhone() : "");
                                entrantData.put("status", "WAITING");
                                entrantData.put("joinedTimestamp", currentTime);
                                entrantData.put("statusTimestamp", currentTime);
                                entrantData.put("declineReason", null);
                                entrantData.put("geolocationVerified", false);

                                // Step 5: Create Registration record
                                String registrationId = userId + "_" + eventId;

                                Map<String, Object> registrationData = new HashMap<>();
                                registrationData.put("userId", userId);
                                registrationData.put("eventId", eventId);
                                registrationData.put("status", "JOINED");
                                registrationData.put("eventTitleSnapshot", event.getName());
                                registrationData.put("eventLocationSnapshot", event.getLocation());
                                registrationData.put("registeredAt", new Timestamp(new Date(currentTime)));
                                registrationData.put("updatedAt", new Timestamp(new Date(currentTime)));
                                registrationData.put("selectedAt", null);
                                registrationData.put("enrolledAt", null);

                                // Step 6: Batch write all updates
                                WriteBatch batch = db.batch();

                                // Add entrant document
                                DocumentReference entrantRef = db.collection("entrants").document(entrantId);
                                batch.set(entrantRef, entrantData);

                                // Add registration document
                                DocumentReference registrationRef = db.collection("registrations").document(registrationId);
                                batch.set(registrationRef, registrationData);

                                // Update event's waiting list and count
                                finalWaitingList.add(userId);
                                DocumentReference eventRef = db.collection("events").document(eventId);
                                batch.update(eventRef, "waitingList", finalWaitingList);
                                batch.update(eventRef, "currentWaitingCount", finalWaitingList.size());

                                // Commit the batch
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("FirebaseEventRepository",
                                                    "Successfully joined waiting list: " + userId + " -> " + eventId);
                                            if (callback != null) callback.onSuccess();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FirebaseEventRepository",
                                                    "Failed to join waiting list", e);
                                            if (callback != null) callback.onError(e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    @Override
    public void leaveWaitingList(String eventId, String userId, RepositoryCallback callback) {
        if (eventId == null || eventId.isEmpty() || userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError(new IllegalArgumentException("Event ID and User ID required"));
            return;
        }

        // Step 1: Get the event document
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        if (callback != null) callback.onError(new Exception("Event not found"));
                        return;
                    }

                    Event event = eventDoc.toObject(Event.class);
                    if (event == null) {
                        if (callback != null) callback.onError(new Exception("Failed to parse event"));
                        return;
                    }

                    List<String> waitingList = event.getWaitingList();
                    if (waitingList == null || !waitingList.contains(userId)) {
                        if (callback != null) callback.onError(new Exception("User not on waiting list"));
                        return;
                    }

                    // Step 2: Prepare batch updates
                    WriteBatch batch = db.batch();

                    // Update entrant status to CANCELLED
                    String entrantId = userId + "_" + eventId;
                    DocumentReference entrantRef = db.collection("entrants").document(entrantId);
                    Map<String, Object> entrantUpdates = new HashMap<>();
                    entrantUpdates.put("status", "CANCELLED");
                    entrantUpdates.put("statusTimestamp", System.currentTimeMillis());
                    batch.update(entrantRef, entrantUpdates);

                    // Update registration status to CANCELLED
                    String registrationId = userId + "_" + eventId;
                    DocumentReference registrationRef = db.collection("registrations").document(registrationId);
                    Map<String, Object> registrationUpdates = new HashMap<>();
                    registrationUpdates.put("status", "CANCELLED");
                    registrationUpdates.put("updatedAt", new Timestamp(new Date()));
                    batch.update(registrationRef, registrationUpdates);

                    // Remove user from event's waiting list
                    waitingList.remove(userId);
                    DocumentReference eventRef = db.collection("events").document(eventId);
                    batch.update(eventRef, "waitingList", waitingList);
                    batch.update(eventRef, "currentWaitingCount", waitingList.size());

                    // Step 3: Commit the batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("FirebaseEventRepository",
                                        "Successfully left waiting list: " + userId + " -> " + eventId);
                                if (callback != null) callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseEventRepository", "Failed to leave waiting list", e);
                                if (callback != null) callback.onError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    @Override
    public void getEventsByCategory(String category, Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        db.collection("events")
                .whereEqualTo("active", true)
                .whereArrayContains("categories", category)
                .get()
                .addOnSuccessListener(q -> {
                    List<Event> out = new ArrayList<>();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        Event e = d.toObject(Event.class);
                        if (e != null) {
                            e.setId(d.getId());
                            out.add(e);
                        }
                    }
                    if (onSuccess != null) onSuccess.accept(out);
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    @Override
    public void getActiveEvents(
            Consumer<List<Event>> onSuccess,
            Consumer<Exception> onError
    ) {
        db.collection("events")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(query -> {
                    List<Event> events = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setId(doc.getId());
                            events.add(event);
                        }
                    }

                    if (onSuccess != null) onSuccess.accept(events);
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    @Override
    public void getRecentEvents(int limit, Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        db.collection("events")
                .whereEqualTo("active", true)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(q -> {
                    List<Event> out = new ArrayList<>();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        Event e = d.toObject(Event.class);
                        if (e != null) {
                            e.setId(d.getId());
                            out.add(e);
                        }
                    }
                    if (onSuccess != null) onSuccess.accept(out);
                })
                .addOnFailureListener(err -> {
                    if (onError != null) onError.accept(err);
                });
    }

    // ===================== REGISTRATION OPERATIONS =====================

    @Override
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

    @Override
    public void stopListeningUserRegistrations() {
        if (activeRegsListener != null) {
            activeRegsListener.remove();
            activeRegsListener = null;
        }
    }

    @Override
    public void upsertRegistrationOnJoin(String userId, String eventId, String eventTitleSnapshot, RepositoryCallback callback) {
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
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }
}