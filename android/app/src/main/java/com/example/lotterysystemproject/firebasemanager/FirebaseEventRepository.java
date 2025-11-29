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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
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

    // TESTING CONSTRUCTOR (mock Firestore & Storage)
    public FirebaseEventRepository(FirebaseFirestore db, FirebaseStorage storage) {
        this.db = db;
        this.storage = storage;
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
                .addSnapshotListener((value, error) -> {

                    if(error != null) {
                        liveData.setValue(null); // or handle error differently
                        // Optional: log the error
                        Log.e("Repository", "Error fetching events", error);
                        return;
                    }

                    List<Event> out = new ArrayList<>();
                    for (DocumentSnapshot d : value.getDocuments()) {
                        Event e = d.toObject(Event.class);
                        if (e != null) {
                            e.setId(d.getId());
                            out.add(e);
                        }
                    }
                    liveData.setValue(out);
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

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        if (callback != null) callback.onError(new Exception("Event not found"));
                        return;
                    }
                    // Add business logic here (e.g., check capacity, registration period)
                    // For now, just signal success
                    if (callback != null) callback.onSuccess();
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

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        if (callback != null) callback.onError(new Exception("Event not found"));
                        return;
                    }
                    // Add business logic here to remove user from waiting list
                    // For now, just signal success
                    if (callback != null) callback.onSuccess();
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