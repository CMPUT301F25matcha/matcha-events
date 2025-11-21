package com.example.lotterysystemproject.models;

/**
 * Model class representing a user (entrant) in the lottery system.
 * Contains personal information and tracks sign-up status.
 *
 * Part of the MVC pattern where this class represents the data model
 * for user information collected during registration.
 *
 * Related User Stories:
 * - US 01.02.01: Personal information storage (name, email, phone)
 * - US 01.07.01: Device-based identification via unique ID
 *
 * @see com.example.lotterysystemproject.views.entrant.UserInfoView
 * @see com.example.lotterysystemproject.controllers.UserInfo
 */
public class User {
    /**
     * Indicates whether the user has completed sign-up with personal information
     */
    private boolean signedUp;

    /**
     * Unique identifier for the user (device-based for testing, Firebase UID in production)
     */
    private String id;

    /**
     * User's full name
     */
    private String name;

    /**
     * User's email address
     */
    private String email;

    /**
     * User's phone number (optional)
     */
    private String phone;

    /**
     * User's role in the system (e.g., "entrant", "organizer", "admin")
     */
    private String role;

    private long createdAt;

    private boolean isActive;
    /**
     * Default no-argument constructor required for Firebase serialization.
     */
    public User() {}

    /**
     * Constructs a new User with the specified personal information.
     * The user is marked as not signed up by default.
     *
     * @param id    Unique identifier for the user
     * @param name  User's full name
     * @param email User's email address
     * @param phone User's phone number (can be empty string if not provided)
     */
    public User(String id, String name, String email, String phone, long timestamp) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = timestamp;
        this.role = "entrant";
        this.signedUp = false;
        this.isActive = true;
    }

    /**
     * Gets the unique identifier for this user.
     *
     * @return The user's unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the user's full name.
     *
     * @return The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the user's email address.
     *
     * @return The user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's phone number.
     *
     * @return The user's phone number (may be empty if not provided)
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gets the user's role in the system.
     *
     * @return The user's role (e.g., "entrant", "organizer", "admin")
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the unique identifier for this user.
     *
     * @param id The unique ID to assign to the user
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the user's full name.
     *
     * @param name The user's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The user's email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phone The user's phone number (can be empty string)
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Sets the user's role in the system.
     *
     * @param role The role to assign (e.g., "entrant", "organizer", "admin")
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Sets whether the user has completed the sign-up process.
     *
     * @param signedUp true if user provided personal information, false otherwise
     */
    public void setSignedUp(boolean signedUp) {
        this.signedUp = signedUp;
    }

    public void setCreatedAt(long timestamp) {
        this.createdAt = timestamp;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    /**
     * Checks whether the user has completed the sign-up process.
     *
     * @return true if user provided personal information, false otherwise
     */
    public boolean getSignedUp() {
        return signedUp;
    }

    public boolean isActive() {
        return isActive;
    }
}