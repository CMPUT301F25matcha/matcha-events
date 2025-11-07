package com.example.lotterysystemproject.Models;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Central data manager for handling event and user data. This class is designed to operate in two modes:
 * <ul>
 *     <li><b>Mock Mode:</b> (Default) Uses an in-memory collection of mock data for local development and testing. This mode does not require a Firebase connection and simulates network latency using Handlers.</li>
 *     <li><b>Firebase Mode:</b> Connects to a real Firebase Firestore and Storage backend for production use.</li>
 * </ul>
 * The mode is controlled by the static final boolean {@code USE_FIREBASE}. This class is implemented as a singleton.
 */
public class EventFirebase {
    /** Toggles between Firebase and mock mode. Set to false for testing, true for production. */
    private static final boolean USE_FIREBASE = false;

    private static EventFirebase instance;

    /**
     * Returns the singleton instance of the EventFirebase class.
     * @return The single instance of EventFirebase.
     */
    public static EventFirebase getInstance() {
        if (instance == null) {
            synchronized (EventFirebase.class) {
                if (instance == null) instance = new EventFirebase();
            }
        }
        return instance;
    }

    // ===================== FIREBASE (optional) =====================
    /** Nullable instance of FirebaseFirestore, used only when in Firebase mode. */
    @Nullable private final FirebaseFirestore db;
    /** Nullable instance of FirebaseStorage, used only when in Firebase mode. */
    @Nullable private final FirebaseStorage storage;

    // ===================== MOCK STATE =====================
    /** In-memory cache for events, used in mock mode. Key is the event ID. */
    private final Map<String, Event> mockEvents = new HashMap<>();
    /** Ordered list of event IDs, used in mock mode to maintain consistent order. */
    private final List<String> mockEventIds = new ArrayList<>();

    /** In-memory cache for registrations, grouped by user ID. Used in mock mode. */
    private final Map<String, List<Registration>> mockRegistrationsByUser = new HashMap<>();
    /** In-memory cache for registration listeners, grouped by user ID. Used in mock mode. */
    private final Map<String, List<RegistrationsListener>> mockRegListeners = new HashMap<>();

    /** Handler to post operations on the main thread, used to simulate async behavior in mock mode. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    /** Firestore listener registration, used only in Firebase mode to manage live updates. */
    private ListenerRegistration activeRegsListener;

    /**
     * Private constructor to initialize the singleton instance.
     * Sets up either the Firebase connections or initializes the mock data based on the USE_FIREBASE flag.
     */
    private EventFirebase() {
        if (USE_FIREBASE) {
            db = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
        } else {
            db = null;
            storage = null;
        }
        initMockEvents();
    }

    // ===================== ACCESSORS =====================

    /**
     * Gets the FirebaseFirestore instance.
     * @return The Firestore instance, or null if in mock mode.
     */
    @Nullable public FirebaseFirestore getDatabase() { return db; }

    /**
     * Gets the FirebaseStorage instance.
     * @return The FirebaseStorage instance, or null if in mock mode.
     */
    @Nullable public FirebaseStorage getStorage() { return storage; }

    /**
     * Gets a reference to a specific Firestore collection.
     * @param name The name of the collection.
     * @return A {@link CollectionReference} to the specified collection.
     * @throws IllegalStateException if called while in mock mode.
     */
    public CollectionReference getCollection(String name) {
        if (!USE_FIREBASE || db == null) {
            throw new IllegalStateException("Firebase disabled; no collections available in mock mode.");
        }
        return db.collection(name);
    }

    // ===================== CALLBACK TYPES =====================

    /**
     * A generic callback interface for Firebase operations that do not return data.
     */
    public interface FirebaseCallback {
        /** Called on successful completion of the operation. */
        void onSuccess();
        /** Called when the operation fails. */
        void onError(Exception e);
    }

    /**
     * A listener interface for receiving updates to a list of registrations.
     */
    public interface RegistrationsListener {
        /**
         * Called when the list of registrations has changed.
         * @param items The updated list of {@link Registration} objects.
         */
        void onChanged(List<Registration> items);
        /** Called when an error occurs while listening for changes. */
        void onError(Exception e);
    }

    // ===================== MOCK SEED DATA =====================

    /**
     * Initializes the in-memory mock data for events.
     * This method populates the {@code mockEvents} map with a predefined set of {@link Event} objects
     * for testing and development purposes. It only runs if the mock data has not been previously initialized.
     */
    private void initMockEvents() {
        if (!mockEventIds.isEmpty()) return;

        Calendar cal = Calendar.getInstance();

        cal.set(2024, Calendar.DECEMBER, 20, 19, 0);
        Event e1 = new Event("event1", "Winter Music Festival", "Music Promoters Inc", "host1", cal.getTime(), "Downtown Concert Hall");
        e1.setDescription("Join us for an amazing winter music festival featuring top artists!");
        e1.setCategory("Music");
        e1.setMaxCapacity(500);
        e1.setCurrentCapacity(350);
        e1.setImageUrl("");
        e1.setActive(true);
        mockEvents.put(e1.getId(), e1); mockEventIds.add(e1.getId());

        cal.set(2024, Calendar.DECEMBER, 22, 9, 0);
        Event e2 = new Event("event2", "Tech Innovation Summit 2024", "Tech Hub", "host2", cal.getTime(), "Convention Center");
        e2.setDescription("Explore the latest in technology and innovation with industry leaders.");
        e2.setCategory("Tech");
        e2.setMaxCapacity(300);
        e2.setCurrentCapacity(280);
        e2.setImageUrl("");
        e2.setActive(true);
        mockEvents.put(e2.getId(), e2); mockEventIds.add(e2.getId());

        cal.set(2024, Calendar.DECEMBER, 18, 12, 0);
        Event e3 = new Event("event3", "Food & Wine Festival", "Culinary Events Co", "host3", cal.getTime(), "City Park");
        e3.setDescription("Taste amazing dishes and wines from local and international vendors.");
        e3.setCategory("Food");
        e3.setMaxCapacity(400);
        e3.setCurrentCapacity(400);
        e3.setImageUrl("");
        e3.setActive(true);
        mockEvents.put(e3.getId(), e3); mockEventIds.add(e3.getId());

        cal.set(2024, Calendar.DECEMBER, 25, 10, 0);
        Event e4 = new Event("event4", "Modern Art Exhibition", "Art Gallery Downtown", "host4", cal.getTime(), "Art Museum");
        e4.setDescription("Discover contemporary art pieces from emerging and established artists.");
        e4.setCategory("Art");
        e4.setMaxCapacity(200);
        e4.setCurrentCapacity(120);
        e4.setImageUrl("");
        e4.setActive(true);
        mockEvents.put(e4.getId(), e4); mockEventIds.add(e4.getId());

        cal.set(2024, Calendar.DECEMBER, 19, 14, 0);
        Event e5 = new Event("event5", "Charity Basketball Tournament", "Sports Community Org", "host5", cal.getTime(), "Sports Arena");
        e5.setDescription("Watch exciting basketball games while supporting local charities.");
        e5.setCategory("Sports");
        e5.setMaxCapacity(1000);
        e5.setCurrentCapacity(750);
        e5.setImageUrl("");
        e5.setActive(true);
        mockEvents.put(e5.getId(), e5); mockEventIds.add(e5.getId());

        cal.set(2024, Calendar.DECEMBER, 21, 13, 0);
        Event e6 = new Event("event6", "Digital Marketing Workshop", "Learn Academy", "host6", cal.getTime(), "Business Center");
        e6.setDescription("Learn advanced digital marketing strategies from industry experts.");
        e6.setCategory("Education");
        e6.setMaxCapacity(150);
        e6.setCurrentCapacity(90);
        e6.setImageUrl("");
        e6.setActive(true);
        mockEvents.put(e6.getId(), e6); mockEventIds.add(e6.getId());
    }

    // ===================== USERS =====================

    /**
     * Adds a user to the database. In mock mode, this simulates a successful operation after a short delay.
     * @param user The {@link User} object to add.
     * @param cb The callback to handle success or failure.
     */
    public void addUser(User user, FirebaseCallback cb) {
        if (USE_FIREBASE && db != null) {
            db.collection("users").document(user.getId())
                    .set(user)
                    .addOnSuccessListener(v -> { if (cb != null) cb.onSuccess(); })
                    .addOnFailureListener(e -> { if (cb != null) cb.onError(e); });
        } else {
            mainHandler.postDelayed(() -> { if (cb != null) cb.onSuccess(); }, 200);
        }
    }

    /**
     * Updates user information in the database. In mock mode, this simulates a successful operation after a short delay.
     * @param userId The ID of the user to update.
     * @param updates A map of fields to update.
     * @param cb The callback to handle success or failure.
     */
    public void updateUser(String userId, Map<String, Object> updates, FirebaseCallback cb) {
        if (USE_FIREBASE && db != null) {
            db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(v -> { if (cb != null) cb.onSuccess(); })
                    .addOnFailureListener(e -> { if (cb != null) cb.onError(e); });
        } else {
            mainHandler.postDelayed(() -> { if (cb != null) cb.onSuccess(); }, 200);
        }
    }

    /**
     * Retrieves a user from the database. In mock mode, this returns a predefined demo user after a short delay.
     * @param userId The ID of the user to retrieve.
     * @param onSuccess A consumer for the retrieved {@link User} object.
     * @param onError A consumer for any exception that occurs.
     */
    public void getUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError) {
        if (USE_FIREBASE && db != null) {
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
                    .addOnFailureListener(e -> { if (onError != null) onError.accept(e); });
        } else {
            mainHandler.postDelayed(() -> {
                if (onSuccess != null) {
                    onSuccess.accept(new User(userId, "Demo User", "demo@example.com", "123-456-7890"));
                }
            }, 150);
        }
    }

    /**
     * Deletes a user from the database. In mock mode, this simulates a successful operation after a short delay.
     * @param userId The ID of the user to delete.
     * @param cb The callback to handle success or failure.
     */
    public void deleteUser(String userId, FirebaseCallback cb) {
        if (USE_FIREBASE && db != null) {
            db.collection("users").document(userId)
                    .delete()
                    .addOnSuccessListener(v -> { if (cb != null) cb.onSuccess(); })
                    .addOnFailureListener(e -> { if (cb != null) cb.onError(e); });
        } else {
            mainHandler.postDelayed(() -> { if (cb != null) cb.onSuccess(); }, 150);
        }
    }

    // ===================== EVENTS =====================

    /**
     * Retrieves all active events. In mock mode, this returns the list of pre-seeded mock events after a short delay.
     * @param onSuccess A consumer for the list of {@link Event} objects.
     * @param onError A consumer for any exception that occurs.
     */
    public void getAllEvents(Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        if (USE_FIREBASE && db != null) {
            db.collection("events")
                    .whereEqualTo("isActive", true)
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
                    .addOnFailureListener(e -> { if (onError != null) onError.accept(e); });
        } else {
            mainHandler.postDelayed(() -> {
                List<Event> out = new ArrayList<>();
                for (String id : mockEventIds) {
                    Event e = mockEvents.get(id);
                    if (e != null && e.isActive()) out.add(e);
                }
                if (onSuccess != null) onSuccess.accept(out);
            }, 300);
        }
    }

    /**
     * Adds a user to an event's waiting list. In mock mode, this adds the user ID to the in-memory waiting list.
     * @param eventId The ID of the event.
     * @param userId The ID of the user to add to the waiting list.
     * @param cb The callback to handle success or failure.
     */
    public void joinWaitingList(String eventId, String userId, FirebaseCallback cb) {
        if (eventId == null || eventId.isEmpty() || userId == null || userId.isEmpty()) {
            if (cb != null) cb.onError(new IllegalArgumentException("Event ID and User ID required"));
            return;
        }

        if (USE_FIREBASE && db != null) {
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) { if (cb != null) cb.onError(new Exception("Event not found")); return; }
                        // Business rules can be added here in a real implementation
                        if (cb != null) cb.onSuccess();
                    })
                    .addOnFailureListener(e -> { if (cb != null) cb.onError(e); });
        } else {
            mainHandler.postDelayed(() -> {
                Event e = mockEvents.get(eventId);
                if (e == null) { if (cb != null) cb.onError(new Exception("Event not found")); return; }
                List<String> wl = e.getWaitingList();
                if (wl == null) { wl = new ArrayList<>(); e.setWaitingList(wl); }
                if (wl.contains(userId)) { if (cb != null) cb.onError(new Exception("Already on waiting list")); return; }
                wl.add(userId);
                if (cb != null) cb.onSuccess();
            }, 250);
        }
    }

    /**
     * Removes a user from an event's waiting list. This method only operates in mock mode.
     * @param event The event from which to remove the user.
     * @param userId The ID of the user to remove.
     * @param cb The callback to handle success or failure.
     */
    public static void leaveWaitingList(Event event, String userId, FirebaseCallback cb) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (event == null || userId == null) { if (cb != null) cb.onError(new IllegalArgumentException("event/userId null")); return; }
            List<String> wl = event.getWaitingList();
            if (wl != null && wl.remove(userId)) {
                if (cb != null) cb.onSuccess();
            } else {
                if (cb != null) cb.onError(new Exception("User not on waiting list"));
            }
        }, 200);
    }

    // ===================== REGISTRATIONS / HISTORY =====================

    /**
     * Listens for real-time updates to a user's event registrations.
     * In mock mode, it adds the listener to a list and immediately returns the current mock data.
     * @param userId The ID of the user whose registrations to listen for.
     * @param listener The listener to be notified of changes.
     */
    public void listenUserRegistrations(String userId, RegistrationsListener listener) {
        if (USE_FIREBASE && db != null) {
            if (activeRegsListener != null) { activeRegsListener.remove(); activeRegsListener = null; }
            activeRegsListener = db.collection("registrations")
                    .whereEqualTo("userId", userId)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .addSnapshotListener((snap, err) -> {
                        if (err != null) { if (listener != null) listener.onError(err); return; }
                        List<Registration> out = new ArrayList<>();
                        if (snap != null) {
                            for (DocumentSnapshot d : snap.getDocuments()) {
                                Registration r = d.toObject(Registration.class);
                                if (r != null) out.add(r);
                            }
                        }
                        if (listener != null) listener.onChanged(out);
                    });
        } else {
            mockRegListeners.computeIfAbsent(userId, k -> new ArrayList<>()).add(listener);
            List<Registration> cur = mockRegistrationsByUser.getOrDefault(userId, new ArrayList<>());
            mainHandler.post(() -> { if (listener != null) listener.onChanged(new ArrayList<>(cur)); });
        }
    }

    /**
     * Stops listening for user registration updates. In Firebase mode, this removes the Firestore listener.
     * In mock mode, this functionality is not implemented.
     */
    public void stopListeningUserRegistrations() {
        if (USE_FIREBASE && activeRegsListener != null) {
            activeRegsListener.remove();
            activeRegsListener = null;
        }
        // In mock mode, listeners are not actively removed in this implementation.
    }

    /**
     * Creates or updates a registration when a user joins an event.
     * In mock mode, this creates a new {@link Registration} object and adds it to the in-memory list.
     * @param userId The ID of the user.
     * @param eventId The ID of the event.
     * @param eventTitleSnapshot A snapshot of the event title at the time of registration.
     * @param cb The callback to handle success or failure.
     */
    public void upsertRegistrationOnJoin(String userId, String eventId, String eventTitleSnapshot, FirebaseCallback cb) {
        String docId = userId + "_" + eventId;

        if (USE_FIREBASE && db != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("eventId", eventId);
            data.put("eventTitleSnapshot", eventTitleSnapshot);
            data.put("status", "JOINED");
            data.put("registeredAt", Timestamp.now());
            data.put("updatedAt", Timestamp.now());

            db.collection("registrations").document(docId)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(v -> { if (cb != null) cb.onSuccess(); })
                    .addOnFailureListener(e -> { if (cb != null) cb.onError(e); });
        } else {
            Registration r = new Registration();
            r.setUserId(userId);
            r.setEventId(eventId);
            r.setEventTitleSnapshot(eventTitleSnapshot);
            r.setStatus("JOINED");
            r.setRegisteredAt(new Timestamp(Calendar.getInstance().getTime()));
            r.setUpdatedAt(new Timestamp(Calendar.getInstance().getTime()));

            List<Registration> userRegs = mockRegistrationsByUser.computeIfAbsent(userId, k -> new ArrayList<>());
            userRegs.add(r);
            
            // Notify listeners about the change
            List<RegistrationsListener> listeners = mockRegListeners.get(userId);
            if (listeners != null) {
                for (RegistrationsListener l : listeners) {
                    mainHandler.post(() -> l.onChanged(new ArrayList<>(userRegs)));
                }
            }
            
            if (cb != null) mainHandler.post(cb::onSuccess);
        }
    }
}
