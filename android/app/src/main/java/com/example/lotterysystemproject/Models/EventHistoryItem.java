package com.example.lotterysystemproject.Models;

public class EventHistoryItem {
    private final String eventName;
    private final String status;
    private final String dateTime;

    public EventHistoryItem(String eventName, String status, String dateTime) {
        this.eventName = eventName;
        this.status = status;
        this.dateTime = dateTime;
    }

    public String getEventName() { return eventName; }
    public String getStatus() { return status; }
    public String getDateTime() { return dateTime; }
}
