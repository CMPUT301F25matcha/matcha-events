package com.example.matchamonday.repositories;

import com.example.matchamonday.models.User;

public interface AuthRepository {

    // Get or create a user tied to device ID
    void authenticateDevice(RepositoryCallback<User> callback);

    // Promote a user to organizer/admin (admin only)
    void setUserRole(String userId, String role,
                     RepositoryCallback<Void> callback);
}

