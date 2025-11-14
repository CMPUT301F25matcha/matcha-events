package com.example.matchamonday.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registration: User journey tracking (User-focused)
 * Maintains a historical record of a user's participation in an event.
 * Shows users their event history and where they stand in the process.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Registration {

    public enum Status {
        JOINED,         // User joined the waiting list
        SELECTED,       // User was selected in lottery
        ENROLLED,       // User accepted and is participating
        DECLINED,       // User declined the invitation
        NOT_SELECTED,   // User was not selected
        CANCELLED       // User or organizer cancelled participation
    }

    private String registrationId;
    private String userId;
    private String eventId;
    private Status status;
    private String eventTitleSnapshot;
    private String eventLocationSnapshot;
    private long registeredAt;
    private long updatedAt;
    private long selectedAt;
    private long enrolledAt;

    public Registration(String userId, String eventId, String eventTitle, String eventLocation) {
        this.userId = userId;
        this.eventId = eventId;
        this.status = Status.JOINED;
        this.eventTitleSnapshot = eventTitle;
        this.eventLocationSnapshot = eventLocation;
        this.registeredAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();

        // ADDED: Auto-populate timestamps for key milestones
        switch (status) {
            case SELECTED:
                this.selectedAt = System.currentTimeMillis();
                break;
            case ENROLLED:
                this.enrolledAt = System.currentTimeMillis();
                break;
        }
    }

    // ADDED: Helper methods
    public boolean isActive() {
        return status == Status.ENROLLED || status == Status.SELECTED;
    }

    public boolean isHistory() {
        return status == Status.DECLINED || status == Status.NOT_SELECTED || status == Status.CANCELLED;
    }
}