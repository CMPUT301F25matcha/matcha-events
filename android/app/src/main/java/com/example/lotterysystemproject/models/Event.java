package com.example.lotterysystemproject.Models;

import java.util.Date;

public class Event {
    private String id;
    private String name;
    private String description;
    private Date eventDate;
    private String time;
    private String location;
    private int capacity;
    private int enrolled;
    private String status; // "open", "closed", "completed"


    private double price;
    private Date registrationStart;
    private Date registrationEnd;
    private String qrCodePromo;
    private String qrCodeCheckin;
    private String posterUrl;
    private int maxWaitingList;
    // Empty constructor for Firebase
    public Event() {}

    // Constructor with required fields
    public Event(String name, Date eventDate, String time, String location, int capacity) {
        this.name = name;
        this.eventDate = eventDate;
        this.time = time;
        this.location = location;
        this.capacity = capacity;
        this.enrolled = 0;
        this.status = "open";
        this.price = 0.0;  // Default: free event
    }

    // ========== SCREEN 1 GETTERS AND SETTERS ==========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getEnrolled() { return enrolled; }
    public void setEnrolled(int enrolled) { this.enrolled = enrolled; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    // ========== SCREEN 2 GETTERS AND SETTERS ==========

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Date getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(Date registrationStart) {
        this.registrationStart = registrationStart;
    }

    public Date getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(Date registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public int getMaxWaitingList() { return maxWaitingList; }
    public void setMaxWaitingList(int maxWaitingList) { this.maxWaitingList = maxWaitingList; }
    public String getQrCodePromo() { return qrCodePromo; }
    public void setQrCodePromo(String qrCodePromo) {
        this.qrCodePromo = qrCodePromo;
    }

    public String getQrCodeCheckin() { return qrCodeCheckin; }
    public void setQrCodeCheckin(String qrCodeCheckin) {
        this.qrCodeCheckin = qrCodeCheckin;
    }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}