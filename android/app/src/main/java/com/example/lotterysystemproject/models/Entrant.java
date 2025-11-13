package com.example.lotterysystemproject.models;

/**
 * Represents an entrant in an event lottery.
 * This class stores information about a user who has entered a lottery for an event,
 * including their personal details and their status in the lottery.
 */
public class Entrant {

    /**
     * Enum representing the status of an entrant in the lottery.
     */
    public enum Status {
        /** The entrant is on the waiting list. */
        WAITING,    // In waiting list
        /** The entrant has been selected in the lottery and is pending a response. */
        INVITED,    // Selected in lottery, pending response
        /** The entrant has accepted the invitation and is enrolled in the event. */
        ENROLLED,   // Accepted invitation
        /** The entrant has declined the invitation or has been cancelled by the organizer. */
        CANCELLED   // Declined or cancelled by organizer
    }

    /** The unique identifier for the entrant. */
    private String id;
    /** The unique identifier of the event the entrant is registered for. */
    private String eventId;
    /** The name of the entrant. */
    private String name;
    /** The email address of the entrant. */
    private String email;
    /** The phone number of the entrant. */
    private String phone;
    /** The current status of the entrant in the lottery. */
    private Status status;
    /** The timestamp when the entrant joined the waiting list. */
    private long joinedTimestamp;  // When they joined waiting list
    /** The timestamp when the entrant's status was last updated. */
    private long statusTimestamp;  // When status last changed

    /**
     * Empty constructor required for Firebase.
     */
    public Entrant() {}

    /**
     * Constructs a new Entrant with the specified name and email.
     * The entrant is initialized with a WAITING status and the current timestamps.
     *
     * @param name The name of the entrant.
     * @param email The email address of the entrant.
     */
    public Entrant(String name, String email) {
        this.name = name;
        this.email = email;
        this.status = Status.WAITING;
        this.joinedTimestamp = System.currentTimeMillis();
        this.statusTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters

    /**
     * Gets the unique identifier of the entrant.
     * @return The entrant's ID.
     */
    public String getId() { return id; }
    /**
     * Sets the unique identifier of the entrant.
     * @param id The entrant's ID.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Gets the unique identifier of the event.
     * @return The event ID.
     */
    public String getEventId() { return eventId; }
    /**
     * Sets the unique identifier of the event.
     * @param eventId The event ID.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Gets the name of the entrant.
     * @return The entrant's name.
     */
    public String getName() { return name; }
    /**
     * Sets the name of the entrant.
     * @param name The entrant's name.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets the email address of the entrant.
     * @return The entrant's email address.
     */
    public String getEmail() { return email; }
    /**
     * Sets the email address of the entrant.
     * @param email The entrant's email address.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the phone number of the entrant.
     * @return The entrant's phone number.
     */
    public String getPhone() { return phone; }
    /**
     * Sets the phone number of the entrant.
     * @param phone The entrant's phone number.
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Gets the current status of the entrant.
     * @return The entrant's status.
     */
    public Status getStatus() { return status; }
    /**
     * Sets the status of the entrant and updates the status timestamp.
     * @param status The new status.
     */
    public void setStatus(Status status) {
        this.status = status;
        this.statusTimestamp = System.currentTimeMillis();
    }

    /**
     * Gets the timestamp when the entrant joined the waiting list.
     * @return The joined timestamp in milliseconds.
     */
    public long getJoinedTimestamp() { return joinedTimestamp; }
    /**
     * Sets the timestamp when the entrant joined the waiting list.
     * @param timestamp The joined timestamp in milliseconds.
     */
    public void setJoinedTimestamp(long timestamp) { this.joinedTimestamp = timestamp; }

    /**
     * Gets the timestamp when the entrant's status was last updated.
     * @return The status timestamp in milliseconds.
     */
    public long getStatusTimestamp() { return statusTimestamp; }
    /**
     * Sets the timestamp when the entrant's status was last updated.
     * @param timestamp The status timestamp in milliseconds.
     */
    public void setStatusTimestamp(long timestamp) { this.statusTimestamp = timestamp; }

    /**
     * Helper method to get a human-readable string representing the time elapsed
     * since the status was last updated.
     * @return A string like "5 days ago", "2 hours ago", or "Just now".
     */
    public String getTimeAgo() {
        long diff = System.currentTimeMillis() - statusTimestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + " days ago";
        if (hours > 0) return hours + " hours ago";
        if (minutes > 0) return minutes + " minutes ago";
        return "Just now";
    }
}




