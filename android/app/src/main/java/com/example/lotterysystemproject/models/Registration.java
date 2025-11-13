package com.example.lotterysystemproject.models;

import com.google.firebase.Timestamp;

/**
 * Represents an entrant’s registration record for a specific event.
 * Each Registration links a user to an event and tracks the registration state over time
 */
public class Registration {
    private String userId;
    private String eventId;
    private String status;               // JOINED | SELECTED | DECLINED | NOT_SELECTED
    private String eventTitleSnapshot;   // optional: preserve history if event title changes
    private Timestamp eventStartAt;      // optional: for display/sorting
    private Timestamp registeredAt;      // optional
    private Timestamp updatedAt;         // for ordering (recommended)

    /** Constructor required for Firebase. */
    public Registration() {}

    // Getters
    public String getUserId() { return userId; }
    public String getEventId() { return eventId; }
    public String getStatus() { return status; }
    public String getEventTitleSnapshot() { return eventTitleSnapshot; }
    public Timestamp getEventStartAt() { return eventStartAt; }
    public Timestamp getRegisteredAt() { return registeredAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }


    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setStatus(String status) { this.status = status; }
    public void setEventTitleSnapshot(String s) { this.eventTitleSnapshot = s; }
    public void setEventStartAt(Timestamp t) { this.eventStartAt = t; }
    public void setRegisteredAt(Timestamp t) { this.registeredAt = t; }
    public void setUpdatedAt(Timestamp t) { this.updatedAt = t; }
}
