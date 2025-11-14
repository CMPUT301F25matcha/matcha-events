package com.example.lotterysystemproject.models;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String eventId;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private Date registrationStart;
    private Date registrationEnd;
    private int maxEntrants;
    private int maxWaitingList;
    private String location;
    private String organizerId;
    private String eventPosterUrl;
    private String category;
    private boolean geolocationRequired;

    private String lotteryStatus;       // "Not Drawn", "In Progress", "Completed"
    private int lotteryDrawCount;       // How many to select
    private long lotteryDrawnAt;        // When lottery was run

    // ADDED: Track event state
    private long createdAt;             // When event was created
    private boolean isActive;           // Soft delete flag

    private int currentEnrollmentCount; // For display (denormalized for performance)
    private int currentWaitingCount;    // Number on waiting list (denormalized)

    // ADDED: QR codes for scanning (from user stories)
    private String promotionalQrCode;   // Scanned to join waiting list
    private String checkInQrCode;       // For event check-in

    public Event(String name, String description, String organizerId, String category, int maxEntrants, int maxWaitingList, Date startDate, Date endDate) {
        this.name = name;
        this.description = description;
        this.organizerId = organizerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.maxEntrants = maxEntrants;
        this.maxWaitingList = maxWaitingList;
        this.currentEnrollmentCount = 0;
        this.currentWaitingCount = 0;
        this.lotteryStatus = "Not Drawn";
    }

    public boolean isRegistrationOpen() {
        Date now = new Date();
        return now.after(registrationStart) && now.before(registrationEnd) && isActive;
    }

    public boolean isLotteryDrawn() {
        return "Completed".equals(lotteryStatus);
    }

    public boolean hasAvailableSpots() {
        return currentEnrollmentCount < maxEntrants;
    }
}
