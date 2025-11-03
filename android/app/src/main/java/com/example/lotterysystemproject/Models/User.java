package com.example.lotterysystemproject.Models;

public class User {
    private boolean signedUp;
    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;


    public User() {}

    public User(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        // a bool field for checking if the user signed up or not
        this.signedUp = false;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }

    public void setSignedUp(boolean signedUp) { this.signedUp = signedUp; }
    public boolean getSignedUp() { return signedUp; }
}
