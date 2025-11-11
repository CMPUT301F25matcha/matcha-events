package com.example.lotterysystemproject.FirebaseManager;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.lotterysystemproject.Models.EventAdmin;
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
    private final Map<String, EventAdmin> mockAdminEvents = new HashMap<>();
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

        // Create mock admin events using EventAdmin's actual constructor and setters
        cal.set(2024, Calendar.DECEMBER, 20, 19, 0);
        EventAdmin e1 = new EventAdmin("Winter Music Festival", cal.getTime(), "19:00", "Downtown Concert Hall", 500);
        e1.setId("event1");
        e1.setDescription("Join us for an amazing winter music festival featuring top artists!");
        e1.setStatus("open");
        e1.setEnrolled(350);
        e1.setPosterUrl("https://example.com/images/event1_poster.jpg");
        mockAdminEvents.put(e1.getId(), e1);
        mockAdminEventIds.add(e1.getId());

        cal.set(2024, Calendar.DECEMBER, 22, 9, 0);
        EventAdmin e2 = new EventAdmin("Tech Innovation Summit 2024", cal.getTime(), "09:00", "Convention Center", 300);
        e2.setId("event2");
        e2.setDescription("Explore the latest in technology and innovation with industry leaders.");
        e2.setStatus("open");
        e2.setEnrolled(280);
        e2.setPosterUrl("https://example.com/images/event2_poster.jpg");
        mockAdminEvents.put(e2.getId(), e2);
        mockAdminEventIds.add(e2.getId());

        cal.set(2024, Calendar.DECEMBER, 18, 12, 0);
        EventAdmin e3 = new EventAdmin("Food & Wine Festival", cal.getTime(), "12:00", "City Park", 400);
        e3.setId("event3");
        e3.setDescription("Taste amazing dishes and wines from local and international vendors.");
        e3.setStatus("open");
        e3.setEnrolled(400);
        e3.setPosterUrl("https://example.com/images/event3_poster.jpg");
        mockAdminEvents.put(e3.getId(), e3);
        mockAdminEventIds.add(e3.getId());

        cal.set(2024, Calendar.DECEMBER, 25, 10, 0);
        EventAdmin e4 = new EventAdmin("Modern Art Exhibition", cal.getTime(), "10:00", "Art Museum", 200);
        e4.setId("event4");
        e4.setDescription("Discover contemporary art pieces from emerging and established artists.");
        e4.setStatus("open");
        e4.setEnrolled(120);
        e4.setPosterUrl("https://example.com/images/event4_poster.jpg");
        mockAdminEvents.put(e4.getId(), e4);
        mockAdminEventIds.add(e4.getId());

        cal.set(2024, Calendar.DECEMBER, 19, 14, 0);
        EventAdmin e5 = new EventAdmin("Charity Basketball Tournament", cal.getTime(), "14:00", "Sports Arena", 1000);
        e5.setId("event5");
        e5.setDescription("Watch exciting basketball games while supporting local charities.");
        e5.setStatus("open");
        e5.setEnrolled(750);
        e5.setPosterUrl("https://example.com/images/event5_poster.jpg");
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
    public void addEvent(EventAdmin eventAdmin, AdminCallback callback) {
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
    public void getAllEvents(Consumer<List<EventAdmin>> onSuccess, Consumer<Exception> onError) {
        mainHandler.postDelayed(() -> {
            List<EventAdmin> eventList = new ArrayList<>();
            for (String id : mockAdminEventIds) {
                EventAdmin event = mockAdminEvents.get(id);
                if (event != null) {
                    eventList.add(event);
                }
            }
            if (onSuccess != null) onSuccess.accept(eventList);
        }, 300);
    }

    @Override
    public void listenToAllEvents(Consumer<List<EventAdmin>> onSuccess, Consumer<Exception> onError) {
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
}