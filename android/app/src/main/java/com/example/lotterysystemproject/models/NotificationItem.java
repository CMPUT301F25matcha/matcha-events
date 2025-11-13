package com.example.lotterysystemproject.models;

/**
 * Represents a single notification entry shown to an entrant within
 * notifications screen of Lottery System app.
 */
public class NotificationItem {
    public enum InvitationResponse { NONE, ACCEPTED, DECLINED }

    private final String id;     // stable key (eventId + ":" + registrationId)
    private final String title;
    private final String message;
    private final boolean isInvitation;
    private final long timestamp;
    private InvitationResponse response = InvitationResponse.NONE;


    /**
     * Constructs a new NotificationItem
     * @param id           Identifier for this notification
     * @param title        Title/header of the notification.
     * @param message      Detailed message text.
     * @param isInvitation True if notification is an invitation;
     * @param timestamp    Time notification was created
     */
    public NotificationItem(String id, String title, String message, boolean isInvitation, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.isInvitation = isInvitation;
        this.timestamp = timestamp;
    }

    /** @return Unique stable identifier for this notification. */
    public String getId() { return id; }

    /** @return Title or short description of this notification. */
    public String getTitle() { return title; }

    /** @return Full message text of the notification. */
    public String getMessage() { return message; }

    /** @return true if this notification is an invitation. */
    public boolean isInvitation() { return isInvitation; }

    /** @return Timestamp (in milliseconds) when this notification was generated. */
    public long getTimestamp() { return timestamp; }

    /** @return User’s response to this invitation. */
    public InvitationResponse getResponse() { return response; }

    /**
     * Sets response state for this invitation.
     * @param r New InvitationResponse value to assign.
     */
    public void setResponse(InvitationResponse r) { this.response = r; }
}
