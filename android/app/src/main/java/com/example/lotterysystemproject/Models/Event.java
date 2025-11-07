package com.example.lotterysystemproject.Models;

import java.util.Date;
import java.util.List;

/**
 * Model class representing an Event for the Entrant's in the system.
 * This class holds all the information related to an event, such as its name, date, location,
 * and participants. It is used to store and retrieve event data from Firestore.
 */
public class Event {
    /** The unique identifier of the event. */
    private String id;
    /** The name of the event. */
    private String name;
    /** The name of the host of the event. */
    private String hostName;
    /** The unique identifier of the event's host. */
    private String hostId;
    /** The date and time of the event. */
    private Date date;
    /** The location where the event will take place. */
    private String location;
    /** A detailed description of the event. */
    private String description;
    /** The URL for the event's promotional image. */
    private String imageUrl;
    /** The category of the event (e.g., "Music", "Sports"). */
    private String category;
    /** The maximum number of participants allowed. */
    private int maxCapacity;
    /** The current number of enrolled participants. */
    private int currentCapacity;
    /** A list of user IDs on the waiting list for the event. */
    private List<String> waitingList; // List of user IDs on waiting list
    /** A list of user IDs who are registered participants for the event. */
    private List<String> participants; // List of user IDs who are participants
    /** The status of the event, true if active, false otherwise. */
    private boolean isActive;
    /** The timestamp when the event was created. */
    private Date createdAt;

    /**
     * Constructs a new Event object.
     *
     * @param id The unique identifier for the event.
     * @param name The name of the event.
     * @param hostName The name of the event's host.
     * @param hostId The unique identifier of the event's host.
     * @param date The date and time of the event.
     * @param location The location of the event.
     */
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

    /**
     * Gets the unique identifier of the event.
     * @return The event ID.
     */
    public String getId() { return id; }

    /**
     * Gets the name of the event.
     * @return The event name.
     */
    public String getName() { return name; }

    /**
     * Gets the name of the event's host.
     * @return The host's name.
     */
    public String getHostName() { return hostName; }

    /**
     * Gets the unique identifier of the event's host.
     * @return The host's ID.
     */
    public String getHostId() { return hostId; }

    /**
     * Gets the date of the event.
     * @return The event date.
     */
    public Date getDate() { return date; }

    /**
     * Gets the location of the event.
     * @return The event location.
     */
    public String getLocation() { return location; }

    /**
     * Gets the description of the event.
     * @return The event description.
     */
    public String getDescription() { return description; }

    /**
     * Gets the image URL for the event.
     * @return The image URL.
     */
    public String getImageUrl() { return imageUrl; }

    /**
     * Gets the category of the event.
     * @return The event category.
     */
    public String getCategory() { return category; }

    /**
     * Gets the maximum capacity of the event.
     * @return The maximum capacity.
     */
    public int getMaxCapacity() { return maxCapacity; }

    /**
     * Gets the current number of participants.
     * @return The current capacity.
     */
    public int getCurrentCapacity() { return currentCapacity; }

    /**
     * Gets the list of users on the waiting list.
     * @return A list of user IDs.
     */
    public List<String> getWaitingList() { return waitingList; }

    /**
     * Gets the list of participants.
     * @return A list of user IDs.
     */
    public List<String> getParticipants() { return participants; }

    /**
     * Checks if the event is active.
     * @return true if the event is active, false otherwise.
     */
    public boolean isActive() { return isActive; }

    /**
     * Gets the creation date of the event.
     * @return The creation date.
     */
    public Date getCreatedAt() { return createdAt; }

    // Setters

    /**
     * Sets the unique identifier of the event.
     * @param id The event ID.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Sets the name of the event.
     * @param name The event name.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Sets the name of the event's host.
     * @param hostName The host's name.
     */
    public void setHostName(String hostName) { this.hostName = hostName; }

    /**
     * Sets the unique identifier of the event's host.
     * @param hostId The host's ID.
     */
    public void setHostId(String hostId) { this.hostId = hostId; }

    /**
     * Sets the date of the event.
     * @param date The event date.
     */
    public void setDate(Date date) { this.date = date; }

    /**
     * Sets the location of the event.
     * @param location The event location.
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * Sets the description of the event.
     * @param description The event description.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Sets the image URL for the event.
     * @param imageUrl The image URL.
     */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Sets the category of the event.
     * @param category The event category.
     */
    public void setCategory(String category) { this.category = category; }

    /**
     * Sets the maximum capacity of the event.
     * @param maxCapacity The maximum capacity.
     */
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    /**
     * Sets the current number of participants.
     * @param currentCapacity The current capacity.
     */
    public void setCurrentCapacity(int currentCapacity) { this.currentCapacity = currentCapacity; }

    /**
     * Sets the list of users on the waiting list.
     * @param waitingList A list of user IDs.
     */
    public void setWaitingList(List<String> waitingList) { this.waitingList = waitingList; }

    /**
     * Sets the list of participants.
     * @param participants A list of user IDs.
     */
    public void setParticipants(List<String> participants) { this.participants = participants; }

    /**
     * Sets the active status of the event.
     * @param active true to set the event as active, false otherwise.
     */
    public void setActive(boolean active) { isActive = active; }

    /**
     * Sets the creation date of the event.
     * @param createdAt The creation date.
     */
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    /**
     * Checks if the event has available spots (i.e., not at full capacity).
     * @return true if there are available spots, false otherwise.
     */
    public boolean hasAvailableSpots() {
        return currentCapacity < maxCapacity;
    }

    /**
     * Checks if a specific user is on the event's waiting list.
     * @param userId The unique identifier of the user to check.
     * @return true if the user is on the waiting list, false otherwise.
     */
    public boolean isUserOnWaitingList(String userId) {
        return waitingList != null && waitingList.contains(userId);
    }

    /**
     * Checks if a specific user is a participant in the event.
     * @param userId The unique identifier of the user to check.
     * @return true if the user is a participant, false otherwise.
     */
    public boolean isUserParticipant(String userId) {
        return participants != null && participants.contains(userId);
    }
}
