package com.example.lotterysystemproject.firebasemanager;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import com.example.lotterysystemproject.models.User;
import com.example.lotterysystemproject.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Mock implementation of AdminRepository for local development and testing.
 * Uses in-memory data structures and simulates network latency using Handlers.
 */
public class MockAdminRepository implements AdminRepository {

    // ===================== MOCK STATE =====================
    /** In-memory cache for admin events. Key is the event ID. */
    private final Map<String, Event> mockAdminEvents = new HashMap<>();
    /** Ordered list of event IDs for consistent ordering. */
    private final List<String> mockAdminEventIds = new ArrayList<>();
    /** In-memory cache for image URLs. */
    private final List<String> mockImageUrls = new ArrayList<>();

    /** Handler to post operations on the main thread, simulating async behavior. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Constructor initializes mock data.
     */
    public MockAdminRepository() {
        initMockAdminData();
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

    // ===================== MOCK SEED DATA =====================

    /**
     * Initializes the in-memory mock data for admin operations.
     */
    private void initMockAdminData() {
        if (!mockAdminEventIds.isEmpty()) return;

        Calendar cal = Calendar.getInstance();

        // Create mock admin events using Event's updated constructor
        cal.set(2024, Calendar.DECEMBER, 20, 19, 0);
        Event e1 = new Event(
                "event1",
                "Winter Music Festival",
                "Join us for an amazing winter music festival featuring top artists!",
                "Carti",
                "host21",
                cal.getTime(),
                "19:00",
                "Downtown Concert Hall",
                350
        );
        e1.setStatus("open");
        e1.setPosterImageUrl("https://example.com/images/event1_poster.jpg");
        mockAdminEvents.put(e1.getId(), e1);
        mockAdminEventIds.add(e1.getId());

        cal.set(2024, Calendar.DECEMBER, 22, 9, 0);
        Event e2 = new Event(
                "event2",
                "Tech Innovation Summit 2024",
                "Explore the latest in technology and innovation with industry leaders.",
                "Tech Corp",
                "host22",
                cal.getTime(),
                "09:00",
                "Convention Center",
                300
        );
        e2.setStatus("open");
        e2.setCurrentEnrolled(280);
        e2.setPosterImageUrl("https://example.com/images/event2_poster.jpg");
        mockAdminEvents.put(e2.getId(), e2);
        mockAdminEventIds.add(e2.getId());

        cal.set(2024, Calendar.DECEMBER, 18, 12, 0);
        Event e3 = new Event(
                "event3",
                "Food & Wine Festival",
                "Taste amazing dishes and wines from local and international vendors.",
                "Culinary Events",
                "host23",
                cal.getTime(),
                "12:00",
                "City Park",
                400
        );
        e3.setStatus("open");
        e3.setCurrentEnrolled(400);
        e3.setPosterImageUrl("https://example.com/images/event3_poster.jpg");
        mockAdminEvents.put(e3.getId(), e3);
        mockAdminEventIds.add(e3.getId());

        cal.set(2024, Calendar.DECEMBER, 25, 10, 0);
        Event e4 = new Event(
                "event4",
                "Modern Art Exhibition",
                "Discover contemporary art pieces from emerging and established artists.",
                "Art Gallery",
                "host24",
                cal.getTime(),
                "10:00",
                "Art Museum",
                200
        );
        e4.setStatus("open");
        e4.setCurrentEnrolled(120);
        e4.setPosterImageUrl("https://example.com/images/event4_poster.jpg");
        mockAdminEvents.put(e4.getId(), e4);
        mockAdminEventIds.add(e4.getId());

        cal.set(2024, Calendar.DECEMBER, 19, 14, 0);
        Event e5 = new Event(
                "event5",
                "Charity Basketball Tournament",
                "Watch exciting basketball games while supporting local charities.",
                "Sports Foundation",
                "host25",
                cal.getTime(),
                "14:00",
                "Sports Arena",
                1000
        );
        e5.setStatus("open");
        e5.setCurrentEnrolled(750);
        e5.setPosterImageUrl("https://example.com/images/event5_poster.jpg");
        mockAdminEvents.put(e5.getId(), e5);
        mockAdminEventIds.add(e5.getId());

        // Create mock image URLs
        mockImageUrls.add("https://example.com/images/event1_poster.jpg");
        mockImageUrls.add("https://example.com/images/event2_poster.jpg");
        mockImageUrls.add("https://example.com/images/event3_poster.jpg");
        mockImageUrls.add("https://example.com/images/event4_poster.jpg");
        mockImageUrls.add("https://example.com/images/event5_poster.jpg");
        mockImageUrls.add("https://example.com/images/promo_banner.jpg");
    }

    // ===================== EVENT OPERATIONS =====================

    @Override
    public void addEvent(Event eventAdmin, AdminCallback callback) {
        if (eventAdmin == null || eventAdmin.getId() == null || eventAdmin.getId().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError(new IllegalArgumentException("Event or EventID cannot be null")));
            }
            return;
        }

        mainHandler.postDelayed(() -> {
            mockAdminEvents.put(eventAdmin.getId(), eventAdmin);
            if (!mockAdminEventIds.contains(eventAdmin.getId())) {
                mockAdminEventIds.add(eventAdmin.getId());
            }
            if (callback != null) callback.onSuccess();
        }, 200);
    }

    @Override
    public void getAllEvents(Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        mainHandler.postDelayed(() -> {
            List<Event> eventList = new ArrayList<>();
            for (String id : mockAdminEventIds) {
                Event event = mockAdminEvents.get(id);
                if (event != null) {
                    eventList.add(event);
                }
            }
            if (onSuccess != null) onSuccess.accept(eventList);
        }, 300);
    }

    @Override
    public void listenToAllEvents(Consumer<List<Event>> onSuccess, Consumer<Exception> onError) {
        // In mock mode, just call getAllEvents once
        // In a real implementation, you might set up periodic updates
        getAllEvents(onSuccess, onError);
    }

    @Override
    public void deleteEvent(String eventId, AdminCallback callback) {
        if (eventId == null || eventId.isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError(new IllegalArgumentException("Event ID cannot be null or empty")));
            }
            return;
        }

        mainHandler.postDelayed(() -> {
            mockAdminEvents.remove(eventId);
            mockAdminEventIds.remove(eventId);
            if (callback != null) callback.onSuccess();
        }, 200);
    }

    // ===================== USER OPERATIONS =====================

    @Override
    public void deleteUser(String userId, AdminCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError(new IllegalArgumentException("User ID cannot be null or empty")));
            }
            return;
        }

        mainHandler.postDelayed(() -> {
            // In mock mode, we just simulate success
            if (callback != null) callback.onSuccess();
        }, 200);
    }

    // ===================== IMAGE OPERATIONS =====================

    @Override
    public void getAllImages(Consumer<List<String>> onSuccess, Consumer<Exception> onError) {
        mainHandler.postDelayed(() -> {
            if (onSuccess != null) onSuccess.accept(new ArrayList<>(mockImageUrls));
        }, 250);
    }

    @Override
    public void deleteImage(String imageUrl, AdminCallback callback) {
        mainHandler.postDelayed(() -> {
            boolean removed = mockImageUrls.remove(imageUrl);
            if (removed) {
                if (callback != null) callback.onSuccess();
            } else {
                if (callback != null) callback.onError(new Exception("Image not found"));
            }
        }, 200);
    }

    @Override
    public void deleteMultipleImages(List<String> imageUrls, BiConsumer<Integer, Exception> onComplete) {
        mainHandler.postDelayed(() -> {
            int deleteCount = 0;
            for (String url : imageUrls) {
                if (mockImageUrls.remove(url)) {
                    deleteCount++;
                }
            }
            if (onComplete != null) {
                onComplete.accept(deleteCount, null);
            }
        }, 300);
    }

    @Override
    public void getAllOrganizers(Consumer<List<User>> onSuccess, Consumer<Exception> onError) {

        List<User> organizers = new ArrayList<>();

        User u1 = new User("org1", "Alice Organizer", "alice@example.com", "1234567890", System.currentTimeMillis());
        u1.setRole("organizer");

        User u2 = new User("org2", "Bob Organizer", "bob@example.com", "9876543210", System.currentTimeMillis());
        u2.setRole("organizer");
        organizers.add(u1);
        organizers.add(u2);

        mainHandler.postDelayed(() -> {
            if (onSuccess != null) onSuccess.accept(organizers);
        }, 200);
    }

    public void listenToAllOrganizers(Consumer<List<User>> onSuccess, Consumer<Exception> onError) {
        // Mock implementation – do nothing
        if (onSuccess != null) onSuccess.accept(new ArrayList<>());
    }

}