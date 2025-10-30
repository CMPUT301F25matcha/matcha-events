package com.example.lotterysystemproject;


public class User {
    private String id;
    private String name;
    private String email;
    private String role; // "entrant", "organizer", "admin"
    private String profileImageUrl;

    // Empty constructor needed for Firestore
    public User() {}

    public User(String id, String name, String email, String role, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getProfileImageUrl() { return profileImageUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
