


package com.example.lotterysystemproject.Models;

import java.util.Date;
import java.util.List;

/**
 * Model class representing an Event in the system.
 * Events are stored in Firestore and can be joined by entrants.
 */
public class Event {
    private String id;
    private String name;
    private String hostName;
    private String hostId;
    private Date date;
    private String location;
    private String description;
    private String imageUrl;
    private String category;
    private int maxCapacity;
    private int currentCapacity;
    private List<String> waitingList; // List of user IDs on waiting list
    private List<String> participants; // List of user IDs who are participants
    private boolean isActive;
    private Date createdAt;

    public Event(String id, String name, String hostName, String hostId, Date date, String location) {
        this.id = id;
        this.name = name;
        this.hostName = hostName;
        this.hostId = hostId;
        this.date = date;
        this.location = location;
        this.isActive = true;
        this.waitingList = new java.util.ArrayList<>();
        this.participants = new java.util.ArrayList<>();
        this.createdAt = new Date();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getHostName() { return hostName; }
    public String getHostId() { return hostId; }
    public Date getDate() { return date; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getCurrentCapacity() { return currentCapacity; }
    public List<String> getWaitingList() { return waitingList; }
    public List<String> getParticipants() { return participants; }
    public boolean isActive() { return isActive; }
    public Date getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    public void setHostId(String hostId) { this.hostId = hostId; }
    public void setDate(Date date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public void setCurrentCapacity(int currentCapacity) { this.currentCapacity = currentCapacity; }
    public void setWaitingList(List<String> waitingList) { this.waitingList = waitingList; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public void setActive(boolean active) { isActive = active; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    /**
     * Checks if the event has available spots (not at capacity)
     */
    public boolean hasAvailableSpots() {
        return currentCapacity < maxCapacity;
    }

    /**
     * Checks if a user is on the waiting list
     */
    public boolean isUserOnWaitingList(String userId) {
        return waitingList != null && waitingList.contains(userId);
    }

    /**
     * Checks if a user is a participant
     */
    public boolean isUserParticipant(String userId) {
        return participants != null && participants.contains(userId);
    }
}
