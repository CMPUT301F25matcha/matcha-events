package com.example.lotterysystemproject.Models;

/**
 * Represents a single entry in an entrant’s event registration history.
 * Each EventHistoryItem stores info about a specific event that the user has registered for.
 */
public class EventHistoryItem {
    private final String eventName;
    private final String status;
    private final String dateTime;

    /**
     * Constructs an EventHistoryItem instance.
     * @param eventName name of the event.
     * @param status    entrant’s registration status for event.
     * @param dateTime  date and time of invitation/registration.
     */
    public EventHistoryItem(String eventName, String status, String dateTime) {
        this.eventName = eventName;
        this.status = status;
        this.dateTime = dateTime;
    }

    /**
     * Returns the name of the event.
     * @return the event name.
     */
    public String getEventName() { return eventName; }

    /**
     * Returns current registration status of entrant for this event.
     * @return registration status.
     */
    public String getStatus() { return status; }

    /**
     * Returns date and time when entrant was invited or registered.
     * @return a string representing the invitation timestamp.
     */
    public String getDateTime() { return dateTime; }
}
