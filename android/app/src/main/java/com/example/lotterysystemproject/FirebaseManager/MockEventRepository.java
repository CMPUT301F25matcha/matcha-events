package com.example.lotterysystemproject.FirebaseManager;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.lotterysystemproject.Models.Event;
import com.example.lotterysystemproject.Models.Registration;
import com.example.lotterysystemproject.Models.User;
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
     * Initializes the in-memory mock data for events.
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
        mockEvents.put(e1.getId(), e1);
        mockEventIds.add(e1.getId());

        cal.set(2024, Calendar.DECEMBER, 22, 9, 0);
        Event e2 = new Event("event2", "Tech Innovation Summit 2024", "Tech Hub", "host2", cal.getTime(), "Convention Center");
        e2.setDescription("Explore the latest in technology and innovation with industry leaders.");
        e2.setCategory("Tech");
        e2.setMaxCapacity(300);
        e2.setCurrentCapacity(280);
        e2.setImageUrl("");
        e2.setActive(true);
        mockEvents.put(e2.getId(), e2);
        mockEventIds.add(e2.getId());

        cal.set(2024, Calendar.DECEMBER, 18, 12, 0);
        Event e3 = new Event("event3", "Food & Wine Festival", "Culinary Events Co", "host3", cal.getTime(), "City Park");
        e3.setDescription("Taste amazing dishes and wines from local and international vendors.");
        e3.setCategory("Food");
        e3.setMaxCapacity(400);
        e3.setCurrentCapacity(400);
        e3.setImageUrl("");
        e3.setActive(true);
        mockEvents.put(e3.getId(), e3);
        mockEventIds.add(e3.getId());

        cal.set(2024, Calendar.DECEMBER, 25, 10, 0);
        Event e4 = new Event("event4", "Modern Art Exhibition", "Art Gallery Downtown", "host4", cal.getTime(), "Art Museum");
        e4.setDescription("Discover contemporary art pieces from emerging and established artists.");
        e4.setCategory("Art");
        e4.setMaxCapacity(200);
        e4.setCurrentCapacity(120);
        e4.setImageUrl("");
        e4.setActive(true);
        mockEvents.put(e4.getId(), e4);
        mockEventIds.add(e4.getId());

        cal.set(2024, Calendar.DECEMBER, 19, 14, 0);
        Event e5 = new Event("event5", "Charity Basketball Tournament", "Sports Community Org", "host5", cal.getTime(), "Sports Arena");
        e5.setDescription("Watch exciting basketball games while supporting local charities.");
        e5.setCategory("Sports");
        e5.setMaxCapacity(1000);
        e5.setCurrentCapacity(750);
        e5.setImageUrl("");
        e5.setActive(true);
        mockEvents.put(e5.getId(), e5);
        mockEventIds.add(e5.getId());

        cal.set(2024, Calendar.DECEMBER, 21, 13, 0);
        Event e6 = new Event("event6", "Digital Marketing Workshop", "Learn Academy", "host6", cal.getTime(), "Business Center");
        e6.setDescription("Learn advanced digital marketing strategies from industry experts.");
        e6.setCategory("Education");
        e6.setMaxCapacity(150);
        e6.setCurrentCapacity(90);
        e6.setImageUrl("");
        e6.setActive(true);
        mockEvents.put(e6.getId(), e6);
        mockEventIds.add(e6.getId());
    }

    // ===================== USER OPERATIONS =====================

    @Override
    public void addUser(User user, RepositoryCallback callback) {
        mainHandler.postDelayed(() -> {
            if (callback != null) callback.onSuccess();
        }, 200);
    }

    @Override
    public void updateUser(String userId, Map<String, Object> updates, RepositoryCallback callback) {
        mainHandler.postDelayed(() -> {
            if (callback != null) callback.onSuccess();
        }, 200);
    }

    @Override
    public void getUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError) {
        mainHandler.postDelayed(() -> {
            if (onSuccess != null) {
                onSuccess.accept(new User(userId, "Demo User", "demo@example.com", "123-456-7890"));
            }
        }, 150);
    }

    @Override
    public void deleteUser(String userId, RepositoryCallback callback) {
        mainHandler.postDelayed(() -> {
            if (callback != null) callback.onSuccess();
        }, 150);
    }

    // ===================== EVENT OPERATIONS =====================

    @Override
    public void getAllEvents(Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        mainHandler.postDelayed(() -> {
            List<Event> out = new ArrayList<>();
            for (String id : mockEventIds) {
                Event e = mockEvents.get(id);
                if (e != null && e.isActive()) out.add(e);
            }
            if (onSuccess != null) onSuccess.accept(out);
        }, 300);
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
            if (listener != null) listener.onChanged(new ArrayList<>(cur));
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
}