package com.example.matchamonday.models;

import lombok.Data;
import lombok.NonNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data  // Automatically generates getters, setters, equals, hashCode, toString methods
@AllArgsConstructor  // Generates a constructor for all fields
@NoArgsConstructor   // Generates a default constructor
public class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private String role; // "entrant", "organizer", "admin"
    private boolean geolocationRequired;
    private boolean isNotificationEnabled;
    private boolean isActive;

    public void setRole(@NonNull String role) {
        List<String> validRoles = List.of("entrant", "organizer", "admin");
        if (validRoles.contains(role)) {
            this.role = role;
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    public boolean isEntrant() {
        return "entrant".equals(this.role);
    }

    public boolean isOrganizer() {
        return "organizer".equals(this.role);
    }

    public boolean isAdmin() {
        return "admin".equals(this.role);
    }

    public boolean isNotificationsEnabled() {
        return isNotificationEnabled;
    }
}
