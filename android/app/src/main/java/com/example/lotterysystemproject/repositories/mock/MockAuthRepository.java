package com.example.lotterysystemproject.repositories.mock;

import android.util.Log;

import com.example.lotterysystemproject.models.User;
import com.example.lotterysystemproject.repositories.AuthRepository;
import com.example.lotterysystemproject.repositories.RepositoryCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of AuthRepository for testing
 * Simulates device-based authentication without Firebase Auth
 */
public class MockAuthRepository implements AuthRepository {

    private static final String TAG = "MockAuthRepo";

    // Storage
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private String currentDeviceId = null;
    private User currentUser = null;

    private boolean simulateDelay = false;
    private long delayMs = 100;
    private boolean simulateFailure = false;
    private boolean simulateNewDevice = false;

    public MockAuthRepository() {
    }

    // ========================================================================
    // CONFIGURATION (for testing)
    // ========================================================================

    public void setSimulateDelay(boolean simulate, long delayMs) {
        this.simulateDelay = simulate;
        this.delayMs = delayMs;
    }

    public void setSimulateFailure(boolean simulate) {
        this.simulateFailure = simulate;
    }

    /**
     * Simulate authenticating on a new device (creates new user)
     */
    public void setSimulateNewDevice(boolean simulate) {
        this.simulateNewDevice = simulate;
    }

    /**
     * Set the device ID that will be returned by authentication
     */
    public void setMockDeviceId(String deviceId) {
        this.currentDeviceId = deviceId;
    }

    public void clear() {
        users.clear();
        currentUser = null;
        currentDeviceId = null;
        simulateNewDevice = false;
    }

    // ========================================================================
    // AUTHENTICATE DEVICE (US 01.07.01)
    // ========================================================================

    @Override
    public void authenticateDevice(RepositoryCallback<User> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated authentication failure"));
                    return;
                }

                // Generate device ID if not set
                if (currentDeviceId == null) {
                    currentDeviceId = "usr_" + generateRandomId();
                }

                User user = users.get(currentDeviceId);

                // Check if user exists OR if simulating new device
                if (user == null || simulateNewDevice) {
                    // Create new user
                    user = createNewUser(currentDeviceId);
                    users.put(currentDeviceId, user);
                    Log.d(TAG, "Created new user for device: " + currentDeviceId);
                } else {
                    Log.d(TAG, "Loaded existing user: " + currentDeviceId);
                }

                currentUser = user;
                callback.onSuccess(user);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // SET USER ROLE (Admin only)
    // ========================================================================

    @Override
    public void setUserRole(String userId, String role,
                            RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated role update failure"));
                    return;
                }

                User user = users.get(userId);

                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                // Validate role
                if (!isValidRole(role)) {
                    callback.onFailure(new Exception("Invalid role: " + role));
                    return;
                }

                // Update role
                user.setRole(role);
                users.put(userId, user);

                // Update current user if it's the same
                if (currentUser != null && currentUser.getUserId().equals(userId)) {
                    currentUser = user;
                }

                Log.d(TAG, "Updated role for user " + userId + " to " + role);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private User createNewUser(String userId) {
        User user = new User();
        user.setUserId(userId);
        user.setRole("entrant"); // Default role
        user.setActive(true);
        user.setNotificationEnabled(true);
        user.setCreatedAt(System.currentTimeMillis());

        // Name, email, phone are null until user fills profile

        return user;
    }

    private boolean isValidRole(String role) {
        return "entrant".equals(role) ||
                "organizer".equals(role) ||
                "admin".equals(role);
    }

    private String generateRandomId() {
        // Generate a random alphanumeric ID
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    private void executeAsync(Runnable task) {
        if (simulateDelay) {
            new Thread(() -> {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                task.run();
            }).start();
        } else {
            task.run();
        }
    }

    // ========================================================================
    // TEST HELPER METHODS
    // ========================================================================

    /**
     * Get the currently authenticated user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Get current device ID
     */
    public String getCurrentDeviceId() {
        return currentDeviceId;
    }

    /**
     * Check if a user exists for a device ID
     */
    public boolean userExists(String userId) {
        return users.containsKey(userId);
    }

    /**
     * Get user by ID (for testing)
     */
    public User getUser(String userId) {
        return users.get(userId);
    }

    /**
     * Manually add a user (for testing)
     */
    public void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    /**
     * Get total user count
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Simulate logging out (clear current user)
     */
    public void logout() {
        currentUser = null;
        currentDeviceId = null;
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Check if current user has specific role
     */
    public boolean currentUserHasRole(String role) {
        return currentUser != null && role.equals(currentUser.getRole());
    }

    /**
     * Promote current user to organizer (for testing)
     */
    public void promoteCurrentUserToOrganizer() {
        if (currentUser != null) {
            currentUser.setRole("organizer");
            users.put(currentUser.getUserId(), currentUser);
        }
    }

    /**
     * Promote current user to admin (for testing)
     */
    public void promoteCurrentUserToAdmin() {
        if (currentUser != null) {
            currentUser.setRole("admin");
            users.put(currentUser.getUserId(), currentUser);
        }
    }

    /**
     * Get users by role (for testing)
     */
    public int getUserCountByRole(String role) {
        return (int) users.values().stream()
                .filter(user -> role.equals(user.getRole()))
                .count();
    }

    /**
     * Simulate app restart (preserves users, clears current session)
     */
    public void simulateAppRestart() {
        currentUser = null;
        // Keep users map intact
        // Keep currentDeviceId for re-authentication
    }
}
