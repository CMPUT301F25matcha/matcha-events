package com.example.lotterysystemproject.models;

/**
 * Represents a single notification entry shown to an entrant
 * within the notifications screen of the Lottery System app.
 */
public class NotificationItem {

    // Allowed notification types
    public enum NotificationType {
        WAITING,     // "You joined the waiting list"
        INVITED,     // "You were invited to the event"
        DECLINED,    // "You were not selected"
        CANCELLED    // "You were removed from the event"
    }

    // Local decision state for invitations
    public enum Decision {
        NONE,
        ACCEPTED,
        DECLINED
    }

    private final String id;           // notification ID / document ID
    private final long timestamp;      // when it was created

    private String title;              // notification title
    private String message;            // message text

    private NotificationType notificationType;
    private String organizerId;        // sender
    private String userId;             // receiver

    // New: persisted decision state
    private Decision decision = Decision.NONE;

    /**
     * Main constructor with all fields.
     */
    public NotificationItem(
            String id,
            NotificationType notificationType,
            String organizerId,
            String userId,
            String title,
            String message,
            long timestamp
    ) {
        this.id = id;
        this.notificationType = notificationType;
        this.organizerId = organizerId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    // ----- GETTERS -----

    public String getId() { return id; }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() { return title; }

    public String getMessage() { return message; }

    public long getTimestamp() { return timestamp; }

    public Decision getDecision() { return decision; }

    // ----- SETTERS -----

    public void setNotificationType(NotificationType type) {
        this.notificationType = type;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTitle(String title) { this.title = title; }

    public void setMessage(String message) { this.message = message; }

    public void setDecision(Decision decision) {
        this.decision = (decision == null) ? Decision.NONE : decision;
    }
}
