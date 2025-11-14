package com.example.matchamonday.models;

/**
 * Combined status object containing both Entrant and Registration data
 * Used by EntrantRegistrationRepository to provide full status to UI
 */
public class EntrantRegistrationStatus {

    private Entrant entrant;
    private Registration registration;

    public EntrantRegistrationStatus() {
    }

    public EntrantRegistrationStatus(Entrant entrant, Registration registration) {
        this.entrant = entrant;
        this.registration = registration;
    }

    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================

    public Entrant getEntrant() {
        return entrant;
    }

    public void setEntrant(Entrant entrant) {
        this.entrant = entrant;
    }

    public Registration getRegistration() {
        return registration;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Check if user has any registration for this event
     */
    public boolean hasRegistration() {
        return entrant != null && registration != null;
    }

    /**
     * Check if user is currently on waiting list
     */
    public boolean isWaiting() {
        return entrant != null && entrant.getStatus() == Entrant.Status.WAITING;
    }

    /**
     * Check if user has been invited/selected
     */
    public boolean isInvited() {
        return entrant != null && entrant.getStatus() == Entrant.Status.INVITED;
    }

    /**
     * Check if user is enrolled
     */
    public boolean isEnrolled() {
        return entrant != null && entrant.getStatus() == Entrant.Status.ENROLLED;
    }

    /**
     * Check if user has cancelled or been cancelled
     */
    public boolean isCancelled() {
        return entrant != null && entrant.getStatus() == Entrant.Status.CANCELLED;
    }

    /**
     * Get user-friendly status text for display
     */
    public String getDisplayStatus() {
        if (entrant == null) {
            return "Not Registered";
        }

        switch (entrant.getStatus()) {
            case WAITING:
                return "On Waiting List";
            case INVITED:
                return "Selected - Please Respond";
            case ENROLLED:
                return "Enrolled";
            case CANCELLED:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    /**
     * Get detailed status message
     */
    public String getDetailedMessage() {
        if (entrant == null) {
            return "You have not joined this event.";
        }

        switch (entrant.getStatus()) {
            case WAITING:
                return "You're on the waiting list. You'll be notified if selected.";
            case INVITED:
                return "Congratulations! You've been selected. Please accept or decline your invitation.";
            case ENROLLED:
                return "You're enrolled in this event!";
            case CANCELLED:
                String reason = entrant.getDeclineReason();
                return "Your registration was cancelled" +
                        (reason != null ? ": " + reason : ".");
            default:
                return "Status unknown.";
        }
    }

    /**
     * Check if user can take action (accept/decline)
     */
    public boolean canTakeAction() {
        return isInvited();
    }

    /**
     * Check if user can leave waiting list
     */
    public boolean canLeaveWaitingList() {
        return isWaiting();
    }

    @Override
    public String toString() {
        return "EntrantRegistrationStatus{" +
                "entrant=" + (entrant != null ? entrant.getStatus() : "null") +
                ", registration=" + (registration != null ? registration.getStatus() : "null") +
                '}';
    }
}
