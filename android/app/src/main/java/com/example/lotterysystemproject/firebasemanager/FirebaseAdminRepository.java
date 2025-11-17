package com.example.lotterysystemproject.firebasemanager;

import android.util.Log;

import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.models.NotificationLog;
import com.example.lotterysystemproject.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin repository for privileged operations
 * Handles content moderation and system administration
 * All operations create audit logs
 */
public class FirebaseAdminRepository implements AdminRepository {

    private static final String TAG = "AdminRepo";
    private static final String COLLECTION_EVENTS = "events";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_ENTRANTS = "entrants";
    private static final String COLLECTION_REGISTRATIONS = "registrations";
    private static final String COLLECTION_NOTIFICATION_LOGS = "notification_logs";
    private static final String COLLECTION_ADMIN_AUDIT = "admin_audit_logs";

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    public FirebaseAdminRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    // ========================================================================
    // REMOVE EVENT (US 03.01.01)
    // ========================================================================

    @Override
    public void removeEvent(String eventId, RepositoryCallback<Void> callback) {
        Log.d(TAG, "removeEvent: eventId=" + eventId);

        // Step 1: Get event details for audit log
        db.collection(COLLECTION_EVENTS).document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        callback.onFailure(new Exception("Event not found"));
                        return;
                    }

                    Event event = eventDoc.toObject(Event.class);

                    // Step 2: Soft delete event (set isActive = false)
                    db.collection(COLLECTION_EVENTS).document(eventId)
                            .update("isActive", false,
                                    "status", "cancelled")
                            .addOnSuccessListener(aVoid -> {
                                // Step 3: Delete associated entrants
                                deleteEventEntrants(eventId, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        // Step 4: Delete associated registrations
                                        deleteEventRegistrations(eventId, new RepositoryCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                // Step 5: Log admin action
                                                logAdminAction("REMOVE_EVENT", eventId,
                                                        "Removed event: " + (event != null ? event.getName() : eventId));

                                                Log.d(TAG, "Successfully removed event");
                                                callback.onSuccess(null);
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e(TAG, "Failed to delete registrations", e);
                                                callback.onFailure(e);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e(TAG, "Failed to delete entrants", e);
                                        callback.onFailure(e);
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to soft delete event", e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ========================================================================
    // REMOVE PROFILE (US 03.02.01)
    // ========================================================================

    @Override
    public void removeProfile(String userId, RepositoryCallback<Void> callback) {
        Log.d(TAG, "removeProfile: userId=" + userId);

        // Step 1: Get user details for audit
        db.collection(COLLECTION_USERS).document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        callback.onFailure(new Exception("User not found"));
                        return;
                    }

                    User user = userDoc.toObject(User.class);

                    // Step 2: Soft delete user (set isActive = false)
                    db.collection(COLLECTION_USERS).document(userId)
                            .update("isActive", false)
                            .addOnSuccessListener(aVoid -> {
                                // Step 3: Cancel all user's registrations
                                cancelUserRegistrations(userId, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        // Step 4: Log admin action
                                        logAdminAction("REMOVE_PROFILE", userId,
                                                "Removed profile: " + (user != null ? user.getName() : userId));

                                        Log.d(TAG, "Successfully removed profile");
                                        callback.onSuccess(null);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e(TAG, "Failed to cancel registrations", e);
                                        callback.onFailure(e);
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to soft delete user", e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ========================================================================
    // REMOVE IMAGE (US 03.03.01, US 03.06.01)
    // ========================================================================

    @Override
    public void removeImage(String imageUrl, RepositoryCallback<Void> callback) {
        Log.d(TAG, "removeImage: imageUrl=" + imageUrl);

        if (imageUrl == null || imageUrl.isEmpty()) {
            callback.onFailure(new Exception("Invalid image URL"));
            return;
        }

        try {
            // Parse Firebase Storage URL to get storage reference
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);

            // Delete from Firebase Storage
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove image reference from any events using it
                        removeImageFromEvents(imageUrl, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                logAdminAction("REMOVE_IMAGE", imageUrl,
                                        "Removed image from storage");

                                Log.d(TAG, "Successfully removed image");
                                callback.onSuccess(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.w(TAG, "Image deleted but failed to update events", e);
                                callback.onSuccess(null); // Still succeed
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete image from storage", e);
                        callback.onFailure(e);
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid storage URL", e);
            callback.onFailure(new Exception("Invalid Firebase Storage URL"));
        }
    }

    // ========================================================================
    // BROWSE EVENTS (US 03.04.01)
    // ========================================================================

    @Override
    public void getAllEventsAdmin(RepositoryCallback<List<Event>> callback) {
        Log.d(TAG, "getAllEventsAdmin");

        // Admin sees ALL events, including inactive ones
        db.collection(COLLECTION_EVENTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }

                    Log.d(TAG, "Found " + events.size() + " events (including inactive)");
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get all events", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // BROWSE PROFILES (US 03.05.01)
    // ========================================================================

    @Override
    public void getAllProfiles(RepositoryCallback<List<User>> callback) {
        Log.d(TAG, "getAllProfiles");

        // Admin sees ALL profiles, including inactive ones
        db.collection(COLLECTION_USERS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }

                    Log.d(TAG, "Found " + users.size() + " profiles");
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get all profiles", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // REMOVE ORGANIZER (US 03.07.01)
    // ========================================================================

    @Override
    public void removeOrganizer(String userId, String reason,
                                RepositoryCallback<Void> callback) {
        Log.d(TAG, "removeOrganizer: userId=" + userId + ", reason=" + reason);

        // Step 1: Get user details
        db.collection(COLLECTION_USERS).document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        callback.onFailure(new Exception("User not found"));
                        return;
                    }

                    User user = userDoc.toObject(User.class);
                    if (user == null || !user.isOrganizer()) {
                        callback.onFailure(new Exception("User is not an organizer"));
                        return;
                    }

                    // Step 2: Downgrade role to entrant
                    db.collection(COLLECTION_USERS).document(userId)
                            .update("role", "entrant")
                            .addOnSuccessListener(aVoid -> {
                                // Step 3: Cancel all their events
                                cancelOrganizerEvents(userId, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        // Step 4: Log admin action with reason
                                        logAdminAction("REMOVE_ORGANIZER", userId,
                                                "Removed organizer: " + user.getName() +
                                                        ". Reason: " + reason);

                                        Log.d(TAG, "Successfully removed organizer");
                                        callback.onSuccess(null);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e(TAG, "Failed to cancel organizer events", e);
                                        callback.onFailure(e);
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to downgrade organizer role", e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ========================================================================
    // NOTIFICATION AUDIT LOG (US 03.08.01)
    // ========================================================================

    @Override
    public void getNotificationAuditLog(RepositoryCallback<List<NotificationLog>> callback) {
        Log.d(TAG, "getNotificationAuditLog");

        db.collection(COLLECTION_NOTIFICATION_LOGS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(500) // Last 500 notifications
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<NotificationLog> logs = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        NotificationLog log = doc.toObject(NotificationLog.class);
                        if (log != null) {
                            logs.add(log);
                        }
                    }

                    Log.d(TAG, "Retrieved " + logs.size() + " notification logs");
                    callback.onSuccess(logs);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get notification audit log", e);
                    callback.onFailure(e);
                });
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private void deleteEventEntrants(String eventId, RepositoryCallback<Void> callback) {
        db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Deleted " + querySnapshot.size() + " entrants");
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void deleteEventRegistrations(String eventId, RepositoryCallback<Void> callback) {
        db.collection(COLLECTION_REGISTRATIONS)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Deleted " + querySnapshot.size() + " registrations");
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void cancelUserRegistrations(String userId, RepositoryCallback<Void> callback) {
        // Update all user's entrants to CANCELLED status
        db.collection(COLLECTION_ENTRANTS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    long timestamp = System.currentTimeMillis();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.update(doc.getReference(),
                                "status", "CANCELLED",
                                "statusTimestamp", timestamp);
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Cancelled " + querySnapshot.size() + " registrations");
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void removeImageFromEvents(String imageUrl, RepositoryCallback<Void> callback) {
        db.collection(COLLECTION_EVENTS)
                .whereEqualTo("posterImageUrl", imageUrl)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.update(doc.getReference(), "posterImageUrl", null);
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Removed image from " + querySnapshot.size() + " events");
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void cancelOrganizerEvents(String organizerId, RepositoryCallback<Void> callback) {
        db.collection(COLLECTION_EVENTS)
                .whereEqualTo("organizerId", organizerId)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.update(doc.getReference(),
                                "status", "cancelled",
                                "isActive", false);
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Cancelled " + querySnapshot.size() + " events");
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Log all admin actions for accountability
     */
    private void logAdminAction(String action, String targetId, String description) {
        String logId = db.collection(COLLECTION_ADMIN_AUDIT).document().getId();

        java.util.Map<String, Object> log = new java.util.HashMap<>();
        log.put("logId", logId);
        log.put("action", action);
        log.put("targetId", targetId);
        log.put("description", description);
        log.put("timestamp", System.currentTimeMillis());
        log.put("adminUserId", getCurrentAdminUserId()); // Would get from auth

        db.collection(COLLECTION_ADMIN_AUDIT).document(logId)
                .set(log)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Logged admin action: " + action))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to log admin action", e));
    }

    /**
     * Get current admin's user ID
     * In production, get from Firebase Auth or session
     */
    private String getCurrentAdminUserId() {
        // TODO: Implement proper authentication
        return "admin_user_placeholder";
    }
}
