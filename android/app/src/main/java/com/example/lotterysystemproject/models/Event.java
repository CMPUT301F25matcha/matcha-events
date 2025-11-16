package com.example.lotterysystemproject.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class representing an Event in the lottery system.
 * Supports event creation, registration periods, waiting lists, and participant management.
 * Implements requirements from user stories for Entrants, Organizers, and Admins.
 */
public class Event {
    private String id;
    private String name;
    private String description;
    private String hostName;
    private String hostId;
    private String location;
    private List<String> categories;

    // Event Timing
    private Date eventDate;
    private String eventTime;

    // Registration Period
    private Date registrationStart;
    private Date registrationEnd;

    // Capacity and Participants
    private int maxCapacity;
    private int currentEnrolled; // Number of participants who accepted
    private List<String> participants; // Entrants who accepted invitations
    private List<String> waitingList; // Entrants interested in the event
    private List<String> selectedEntrants; // Entrants chosen but awaiting response
    private List<String> declinedEntrants; // Entrants who declined invitations
    private int maxWaitingListSize; // Optional limit on waiting list size

    // Event Details
    private String posterImageUrl;

    // QR Codes
    private String promotionalQrCode; // Scanned to view event details and join waiting list
    private String checkInQrCode; // Used for check-in verification

    // Geolocation
    private boolean geolocationRequired;

    // Event Status
    private String status; // "open" (accepting registrations), "closed", "completed", "cancelled"
    private boolean isActive;

    // Metadata
    private Date createdAt;
    private Date updatedAt;

    /**
     * Empty constructor for Firebase deserialization.
     */
    public Event() {
        this.participants = new ArrayList<>();
        this.waitingList = new ArrayList<>();
        this.selectedEntrants = new ArrayList<>();
        this.declinedEntrants = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
        this.status = "open";
        this.currentEnrolled = 0;
        this.geolocationRequired = false;
    }

    /**
     * Constructs a new Event object with basic information.
     */
    public Event(String id, String name, String description, String hostName, String hostId,
                 Date eventDate, String eventTime, String location, int maxCapacity) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.hostName = hostName;
        this.hostId = hostId;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.maxCapacity = maxCapacity;
    }

    // ============ GETTERS & SETTERS ============

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getHostId() { return hostId; }
    public void setHostId(String hostId) { this.hostId = hostId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) {this.categories = categories;}

    public void addCategory(String category) {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        categories.add(category);
    }

    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }

    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public Date getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(Date registrationStart) { this.registrationStart = registrationStart; }

    public Date getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(Date registrationEnd) { this.registrationEnd = registrationEnd; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public int getCurrentEnrolled() { return currentEnrolled; }
    public void setCurrentEnrolled(int currentEnrolled) { this.currentEnrolled = currentEnrolled; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public List<String> getWaitingList() { return waitingList; }
    public void setWaitingList(List<String> waitingList) { this.waitingList = waitingList; }

    public List<String> getSelectedEntrants() { return selectedEntrants; }
    public void setSelectedEntrants(List<String> selectedEntrants) { this.selectedEntrants = selectedEntrants; }

    public List<String> getDeclinedEntrants() { return declinedEntrants; }
    public void setDeclinedEntrants(List<String> declinedEntrants) { this.declinedEntrants = declinedEntrants; }

    public int getMaxWaitingListSize() { return maxWaitingListSize; }
    public void setMaxWaitingListSize(int maxWaitingListSize) { this.maxWaitingListSize = maxWaitingListSize; }

    public String getPosterImageUrl() { return posterImageUrl; }
    public void setPosterImageUrl(String posterImageUrl) { this.posterImageUrl = posterImageUrl; }

    public String getPromotionalQrCode() { return promotionalQrCode; }
    public void setPromotionalQrCode(String promotionalQrCode) { this.promotionalQrCode = promotionalQrCode; }

    public String getCheckInQrCode() { return checkInQrCode; }
    public void setCheckInQrCode(String checkInQrCode) { this.checkInQrCode = checkInQrCode; }

    public boolean isGeolocationRequired() { return geolocationRequired; }
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // ============ HELPER METHODS ============

    /**
     * Checks if the event is currently accepting registrations.
     * @return true if current time is within registration period
     */
    public boolean isRegistrationOpen() {
        if (registrationStart == null || registrationEnd == null) {
            return false;
        }
        Date now = new Date();
        return now.after(registrationStart) && now.before(registrationEnd) && "open".equals(status);

    }

    /**
     * Checks if the event has available spots for enrollment.
     * @return true if current enrolled count is less than max capacity
     */
    public boolean hasAvailableSpots() {
        return currentEnrolled < maxCapacity;
    }

    /**
     * Checks if the waiting list has reached its maximum size (if set).
     * @return true if max waiting list size is set and reached
     */
    public boolean isWaitingListFull() {
        return maxWaitingListSize > 0 && waitingList.size() >= maxWaitingListSize;
    }

    /**
     * Checks if a specific user is on the waiting list.
     * @param userId The user's unique identifier
     * @return true if user is on waiting list
     */
    public boolean isUserOnWaitingList(String userId) {
        return waitingList != null && waitingList.contains(userId);
    }

    /**
     * Checks if a specific user is a confirmed participant.
     * @param userId The user's unique identifier
     * @return true if user is a participant
     */
    public boolean isUserParticipant(String userId) {
        return participants != null && participants.contains(userId);
    }

    /**
     * Checks if a specific user has been selected in the lottery draw.
     * @param userId The user's unique identifier
     * @return true if user is in selected entrants list
     */
    public boolean isUserSelected(String userId) {
        return selectedEntrants != null && selectedEntrants.contains(userId);
    }

    /**
     * Adds a user to the waiting list.
     * @param userId The user's unique identifier
     * @return true if successfully added, false if already on list
     */
    public boolean addToWaitingList(String userId) {
        if (waitingList == null) waitingList = new ArrayList<>();
        if (waitingList.contains(userId)) return false;
        if (isWaitingListFull()) return false;
        return waitingList.add(userId);
    }

    /**
     * Removes a user from the waiting list.
     * @param userId The user's unique identifier
     * @return true if successfully removed
     */
    public boolean removeFromWaitingList(String userId) {
        if (waitingList == null) return false;
        return waitingList.remove(userId);
    }

    /**
     * Adds a user to participants (accepted an invitation).
     * @param userId The user's unique identifier
     * @return true if successfully added
     */
    public boolean addParticipant(String userId) {
        if (participants == null) participants = new ArrayList<>();
        if (participants.contains(userId)) return false;
        boolean added = participants.add(userId);
        if (added) currentEnrolled++;
        return added;
    }

    /**
     * Removes a user from participants (e.g., if they cancel).
     * @param userId The user's unique identifier
     * @return true if successfully removed
     */
    public boolean removeParticipant(String userId) {
        if (participants == null) return false;
        boolean removed = participants.remove(userId);
        if (removed) currentEnrolled--;
        return removed;
    }

    /**
     * Marks a user as declined for future reference in the history.
     * @param userId The user's unique identifier
     */
    public void markAsDeclined(String userId) {
        if (declinedEntrants == null) declinedEntrants = new ArrayList<>();
        if (!declinedEntrants.contains(userId)) {
            declinedEntrants.add(userId);
        }
    }
}