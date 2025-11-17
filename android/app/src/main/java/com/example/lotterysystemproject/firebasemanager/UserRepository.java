package com.example.lotterysystemproject.firebasemanager;

import android.net.Uri;

import com.example.lotterysystemproject.models.User;

import java.util.List;

public interface UserRepository {

    // Create or update profile
    void createOrUpdateUser(User user, RepositoryCallback<Void> callback);

    // Get user by ID (device ID for entrants)
    void getUserById(String userId, RepositoryCallback<User> callback);

    // Delete profile
    void deleteUser(String userId, RepositoryCallback<Void> callback);

    // Browse all profiles (admin only)
    void getAllUsers(RepositoryCallback<List<User>> callback);

    // Update notification preferences
    void updateNotificationPreferences(String userId, boolean enabled,
                                       RepositoryCallback<Void> callback);

    // ADDED: User profile picture upload/delete
    void uploadProfilePicture(String userId, Uri imageUri,
                              RepositoryCallback<String> callback);

    void deleteProfilePicture(String userId,
                              RepositoryCallback<Void> callback);

    // Search users (admin)
    void searchUsers(String query,
                     RepositoryCallback<List<User>> callback);

    // Real-time listener for user changes (admin)
    void listenToUser(String userId,
                      RepositoryListener<User> listener);

    // Deactivate account (soft delete)
    void deactivateAccount(String userId,
                           RepositoryCallback<Void> callback);

    // Export user data (GDPR compliance)
    void exportUserData(String userId,
                        RepositoryCallback<String> callback);
}

