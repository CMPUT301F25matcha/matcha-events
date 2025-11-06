package com.example.lotterysystemproject.Models;

public class NotificationItem {
    public enum InvitationResponse { NONE, ACCEPTED, DECLINED }

    private final String id;          // <-- stable key (e.g., eventId + ":" + registrationId)
    private final String title;
    private final String message;
    private final boolean isInvitation;
    private final long timestamp;
    private InvitationResponse response = InvitationResponse.NONE; // <- new

    public NotificationItem(String id, String title, String message, boolean isInvitation, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.isInvitation = isInvitation;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isInvitation() { return isInvitation; }
    public long getTimestamp() { return timestamp; }

    public InvitationResponse getResponse() { return response; }
    public void setResponse(InvitationResponse r) { this.response = r; }
}
