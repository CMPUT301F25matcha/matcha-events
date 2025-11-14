package com.example.lotterysystemproject.firebasemanager;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.models.Registration;
import com.example.lotterysystemproject.models.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Mock implementation of EventRepository for local development and testing.
 * Uses in-memory data structures and simulates network latency using Handlers.
 */
public class MockEventRepository implements EventRepository {

    // ===================== MOCK STATE =====================
    /** In-memory cache for events, used in mock mode. Key is the event ID. */
    private final Map<String, Event> mockEvents = new HashMap<>();
    /** Ordered list of event IDs, used in mock mode to maintain consistent order. */
    private final List<String> mockEventIds = new ArrayList<>();

    /** In-memory cache for users, used in mock mode. Key is the user ID. */
    private final Map<String, User> mockUsers = new HashMap<>();

    /** In-memory cache for registrations, grouped by user ID. */
    private final Map<String, List<Registration>> mockRegistrationsByUser = new HashMap<>();
    /** In-memory cache for registration listeners, grouped by user ID. */
    private final Map<String, List<RegistrationsListener>> mockRegListeners = new HashMap<>();

    /** Handler to post operations on the main thread, used to simulate async behavior. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Constructor initializes mock data.
     */
    public MockEventRepository() {
        initMockEvents();
        initMockUsers();
    }

    // ===================== ACCESSORS =====================

    @Nullable
    @Override
    public FirebaseFirestore getDatabase() {
        return null; // No Firebase in mock mode
    }

    @Nullable
    @Override
    public FirebaseStorage getStorage() {
        return null; // No Firebase in mock mode
    }

    @Override
    public CollectionReference getCollection(String name) {
        throw new IllegalStateException("Firebase disabled; no collections available in mock mode.");
    }

    // ===================== MOCK SEED DATA =====================

    /**
     * Initializes mock user data for testing.
     */
    private void initMockUsers() {
        User entrantUser = new User("usr_entrant123", "John Entrant", "john@example.com", "555-1234");
        entrantUser.setRole("entrant");
        mockUsers.put("usr_entrant123", entrantUser);

        User organizerUser = new User("usr_organizer456", "Jane Organizer", "jane@example.com", "555-5678");
        organizerUser.setRole("organizer");
        mockUsers.put("usr_organizer456", organizerUser);

        User adminUser = new User("usr_admin789", "Admin User", "admin@example.com", "555-9999");
        adminUser.setRole("admin");
        mockUsers.put("usr_admin789", adminUser);
    }

    /**
     * Initializes the in-memory mock data for events.
     */
    private void initMockEvents() {
        if (!mockEventIds.isEmpty()) return;

        Calendar cal = Calendar.getInstance();

        cal.set(2024, Calendar.DECEMBER, 20, 19, 0);
        Event e1 = new Event(
                "event1",
                "Winter Music Festival",
                "Join us for an amazing winter music festival featuring top artists!",
                "Music Promoters Inc",
                "host1",
                cal.getTime(),
                "5:00PM",
                "Downtown Concert Hall",
                500
        );
        e1.setCategory("Music");
        e1.setCurrentEnrolled(350);
        e1.setActive(true);
        mockEvents.put(e1.getId(), e1);
        mockEventIds.add(e1.getId());

        cal.set(2024, Calendar.DECEMBER, 22, 9, 0);
        Event e2 = new Event(
                "event2",
                "Tech Innovation Summit 2024",
                "Explore the latest in technology and innovation with industry leaders.",
                "Tech Hub",
                "host2",
                cal.getTime(),
                "5:00PM",
                "Convention Center",
                300
        );
        e2.setCategory("Tech");
        e2.setCurrentEnrolled(280);
        e2.setActive(true);
        mockEvents.put(e2.getId(), e2);
        mockEventIds.add(e2.getId());

        cal.set(2024, Calendar.DECEMBER, 18, 12, 0);
        Event e3 = new Event(
                "event3",
                "Food & Wine Festival",
                "Taste amazing dishes and wines from local and international vendors.",
                "Culinary Events Co",
                "host3",
                cal.getTime(),
                "5:00PM",
                "City Park",
                400
        );
        e3.setCategory("Food");
        e3.setCurrentEnrolled(400);
        e3.setActive(true);
        mockEvents.put(e3.getId(), e3);
        mockEventIds.add(e3.getId());

        cal.set(2024, Calendar.DECEMBER, 25, 10, 0);
        Event e4 = new Event(
                "event4",
                "Modern Art Exhibition",
                "Discover contemporary art pieces from emerging and established artists.",
                "Art Gallery Downtown",
                "host4",
                cal.getTime(),
                "5:00PM",
                "Art Museum",
                200
        );
        e4.setCategory("Art");
        e4.setCurrentEnrolled(120);
        e4.setActive(true);
        mockEvents.put(e4.getId(), e4);
        mockEventIds.add(e4.getId());

        cal.set(2024, Calendar.DECEMBER, 19, 14, 0);
        Event e5 = new Event(
                "event5",
                "Charity Basketball Tournament",
                "Watch exciting basketball games while supporting local charities.",
                "Sports Community Org",
                "host5",
                cal.getTime(),
                "5:00PM",
                "Sports Arena",
                1000
        );
        e5.setCategory("Sports");
        e5.setCurrentEnrolled(750);
        e5.setActive(true);
        mockEvents.put(e5.getId(), e5);
        mockEventIds.add(e5.getId());

        cal.set(2024, Calendar.DECEMBER, 21, 13, 0);
        Event e6 = new Event(
                "event6",
                "Digital Marketing Workshop",
                "Learn advanced digital marketing strategies from industry experts.",
                "Learn Academy",
                "host6",
                cal.getTime(),
                "5:00PM",
                "Business Center",
                150
        );
        e6.setCategory("Education");
        e6.setCurrentEnrolled(90);
        e6.setActive(true);
        mockEvents.put(e6.getId(), e6);
        mockEventIds.add(e6.getId());
    }

    // ===================== USER OPERATIONS =====================

    @Override
    public void addUser(User user, RepositoryCallback callback) {
        mainHandler.postDelayed(() -> {
            mockUsers.put(user.getId(), user);
            if (callback != null) callback.onSuccess();
        }, 200);
    }

    @Override
    public void updateUser(String userId, Map<String, Object> updates, RepositoryCallback callback) {
        mainHandler.postDelayed(() -> {
            User user = mockUsers.get(userId);
            if (user != null) {
                // Apply updates to the user object
                for (Map.Entry<String, Object> entry : updates.entrySet()) {
                    switch (entry.getKey()) {
                        case "name":
                            user.setName((String) entry.getValue());
                            break;
                        case "email":
                            user.setEmail((String) entry.getValue());
                            break;
                        case "phone":
                            user.setPhone((String) entry.getValue());
                            break;
                        case "role":
                            user.setRole((String) entry.getValue());
                            break;
                        // Add more cases as needed for other fields
                    }
                }
                if (callback != null) callback.onSuccess();
            } else {
                if (callback != null) callback.onError(new Exception("User not found"));
            }
        }, 200);
    }

    @Override
    public void getUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError) {
        mainHandler.postDelayed(() -> {
            User user = mockUsers.get(userId);
            if (user != null) {
                if (onSuccess != null) onSuccess.accept(user);
            } else {
                if (onError != null) onError.accept(new Exception("User not found"));
            }
        }, 150);
    }

    @Override
    public void deleteUser(String userId, RepositoryCallback callback) {
        mainHandler.postDelayed(() -> {
            if (mockUsers.remove(userId) != null) {
                if (callback != null) callback.onSuccess();
            } else {
                if (callback != null) callback.onError(new Exception("User not found"));
            }
        }, 150);
    }

    @Override
    public void updateUserRoleToOrganizer(String userId, RepositoryCallback callback) {
        mainHandler.postDelayed(() -> {
            User user = mockUsers.get(userId);
            if (user != null) {
                user.setRole("organizer");
                if (callback != null) callback.onSuccess();
            } else {
                if (callback != null) callback.onError(new Exception("User not found"));
            }
        }, 200);
    }

    // ===================== EVENT OPERATIONS =====================

    @Override
    public LiveData<List<Event>> getAllEvents() {
        MutableLiveData<List<Event>> liveData = new MutableLiveData<>();

        mainHandler.postDelayed(() -> {
            List<Event> out = new ArrayList<>();
            for (String id : mockEventIds) {
                Event e = mockEvents.get(id);
                if (e != null && e.isActive()) out.add(e);
            }
            liveData.setValue(out);
        }, 300);

        return liveData;
    }

    @Override
    public void addEvent(Event event, Consumer<Exception> onError) {
        mainHandler.postDelayed(() -> {
            if (event.getId() == null || event.getId().isEmpty()) {
                // Generate a new ID if one isn't provided
                event.setId("event" + (mockEvents.size() + 1));
            }
            if (mockEvents.containsKey(event.getId())) {
                if (onError != null) {
                    onError.accept(new Exception("Event with this ID already exists."));
                }
                return;
            }
            mockEvents.put(event.getId(), event);
            mockEventIds.add(event.getId());
            // No error, so we don't call onError.
        }, 200);
    }

    @Override
    public void joinWaitingList(String eventId, String userId, RepositoryCallback callback) {
        if (eventId == null || eventId.isEmpty() || userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError(new IllegalArgumentException("Event ID and User ID required"));
            return;
        }

        mainHandler.postDelayed(() -> {
            Event e = mockEvents.get(eventId);
            if (e == null) {
                if (callback != null) callback.onError(new Exception("Event not found"));
                return;
            }
            List<String> wl = e.getWaitingList();
            if (wl == null) {
                wl = new ArrayList<>();
                e.setWaitingList(wl);
            }
            if (wl.contains(userId)) {
                if (callback != null) callback.onError(new Exception("Already on waiting list"));
                return;
            }
            wl.add(userId);
            if (callback != null) callback.onSuccess();
        }, 250);
    }

    @Override
    public void leaveWaitingList(String eventId, String userId, RepositoryCallback callback) {
        mainHandler.postDelayed(() -> {
            Event event = mockEvents.get(eventId);
            if (event == null || userId == null) {
                if (callback != null) callback.onError(new IllegalArgumentException("event/userId null"));
                return;
            }
            List<String> wl = event.getWaitingList();
            if (wl != null && wl.remove(userId)) {
                if (callback != null) callback.onSuccess();
            } else {
                if (callback != null) callback.onError(new Exception("User not on waiting list"));
            }
        }, 200);
    }

    // ===================== REGISTRATION OPERATIONS =====================

    @Override
    public void listenUserRegistrations(String userId, RegistrationsListener listener) {
        mockRegListeners.computeIfAbsent(userId, k -> new ArrayList<>()).add(listener);
        List<Registration> cur = mockRegistrationsByUser.getOrDefault(userId, new ArrayList<>());
        mainHandler.post(() -> {
            if (listener != null) {
                assert cur != null;
                listener.onChanged(new ArrayList<>(cur));
            }
        });
    }

    @Override
    public void stopListeningUserRegistrations() {
        // In mock mode, listeners are not actively removed in this implementation.
    }

    @Override
    public void upsertRegistrationOnJoin(String userId, String eventId, String eventTitleSnapshot, RepositoryCallback callback) {
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

        if (callback != null) mainHandler.post(callback::onSuccess);
    }

    // ===================== HELPER METHODS FOR MOCK ENTRANT REPO =====================

    /**
     * Helper method for MockEntrantRepository to access user info.
     * This allows both repositories to share the same user data.
     * @param deviceId The device ID to look up
     * @param listener Callback with user info or error
     */
    public void getUserInfo(String deviceId, EntrantRepository.OnUserInfoListener listener) {
        User user = mockUsers.get(deviceId);

        if (user != null) {
            listener.onSuccess(user.getId(), user.getName(), user.getRole());
        } else {
            listener.onFailure("User not found: " + deviceId);
        }
    }
}