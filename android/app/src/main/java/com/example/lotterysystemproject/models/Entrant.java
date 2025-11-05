package com.example.lotterysystemproject.models;

public class Entrant {

    public enum Status {
        WAITING,    // In waiting list
        INVITED,    // Selected in lottery, pending response
        ENROLLED,   // Accepted invitation
        CANCELLED   // Declined or cancelled by organizer
    }

    private String id;
    private String eventId;
    private String name;
    private String email;
    private String phone;
    private Status status;
    private long joinedTimestamp;  // When they joined waiting list
    private long statusTimestamp;  // When status last changed

    // Empty constructor for Firebase
    public Entrant() {}

    public Entrant(String name, String email) {
        this.name = name;
        this.email = email;
        this.status = Status.WAITING;
        this.joinedTimestamp = System.currentTimeMillis();
        this.statusTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        this.statusTimestamp = System.currentTimeMillis();
    }

    public long getJoinedTimestamp() { return joinedTimestamp; }
    public void setJoinedTimestamp(long timestamp) { this.joinedTimestamp = timestamp; }

    public long getStatusTimestamp() { return statusTimestamp; }
    public void setStatusTimestamp(long timestamp) { this.statusTimestamp = timestamp; }

    // Helper method to get time ago string
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