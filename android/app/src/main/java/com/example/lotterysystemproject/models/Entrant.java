package com.example.lotterysystemproject.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entrant: Lottery management (Organizer-focused)
 * Tracks a user's detailed participation in an event's lottery process.
 * Used by organizers to manage waiting lists, draw winners, and track changes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Entrant {

    public enum Status {
        WAITING,    // On waiting list, not yet selected
        INVITED,    // Selected in lottery, awaiting user response
        ENROLLED,   // Accepted invitation, confirmed participation
        CANCELLED   // Declined invitation or cancelled by organizer
    }

    private String entrantId;
    private String userId;
    private String eventId;
    private Status status;
    private long joinedTimestamp;
    private long statusTimestamp;
    private String declineReason;
    private boolean geolocationVerified;

    public Entrant(String userId, String eventId) {
        this.userId = userId;
        this.eventId = eventId;
        this.status = Status.WAITING;
        this.joinedTimestamp = System.currentTimeMillis();
        this.statusTimestamp = System.currentTimeMillis();
        this.geolocationVerified = false;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.statusTimestamp = System.currentTimeMillis();
    }

    public boolean isSelected() {
        return status == Status.INVITED || status == Status.ENROLLED;
    }
}
