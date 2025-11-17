package com.example.lotterysystemproject.firebasemanager;

import android.net.Uri;
import android.util.Log;

import com.example.lotterysystemproject.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Mock implementation of UserRepository for testing
 * Stores users in memory with profile picture tracking
 */
public class MockUserRepository implements UserRepository {

    private static final String TAG = "MockUserRepo";

    // Storage
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> profilePictures = new ConcurrentHashMap<>(); // userId -> imageUrl
    private final Map<String, RepositoryListener<User>> userListeners = new ConcurrentHashMap<>();

    private boolean simulateDelay = false;
    private long delayMs = 100;
    private boolean simulateFailure = false;

    public MockUserRepository() {
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

    public void clear() {
        users.clear();
        profilePictures.clear();
        userListeners.clear();
    }

    // ========================================================================
    // CREATE OR UPDATE USER
    // ========================================================================

    @Override
    public void createOrUpdateUser(User user, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated user operation failure"));
                    return;
                }

                if (user == null || user.getId() == null) {
                    callback.onFailure(new Exception("Invalid user data"));
                    return;
                }

                // Set timestamps if new user
                if (!users.containsKey(user.getId())) {
                    user.setCreatedAt(System.currentTimeMillis());
                }

                Log.d(TAG, "Created/updated user: " + user.getId());

                // Store user
                users.put(user.getId(), user);

                // Notify listeners
                notifyListener(user.getId(), user);

                Log.d(TAG, "Created/updated user: " + user.getId());
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // GET USER BY ID
    // ========================================================================

    @Override
    public void getUserById(String userId, RepositoryCallback<User> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated user operation failure"));
                    return;
                }

                User user = users.get(userId);

                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                Log.d(TAG, "Retrieved user: " + userId);
                callback.onSuccess(user);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // DELETE USER
    // ========================================================================

    @Override
    public void deleteUser(String userId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated user operation failure"));
                    return;
                }

                User user = users.get(userId);

                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                // Hard delete
                users.remove(userId);
                profilePictures.remove(userId);

                Log.d(TAG, "Deleted user: " + userId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // GET ALL USERS (Admin only)
    // ========================================================================

    @Override
    public void getAllUsers(RepositoryCallback<List<User>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated user operation failure"));
                    return;
                }

                List<User> allUsers = new ArrayList<>(users.values());
                allUsers.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

                Log.d(TAG, "Retrieved " + allUsers.size() + " users");
                callback.onSuccess(allUsers);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // NOTIFICATION PREFERENCES
    // ========================================================================

    @Override
    public void updateNotificationPreferences(String userId, boolean enabled,
                                              RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated user operation failure"));
                    return;
                }

                User user = users.get(userId);

                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                users.put(userId, user);

                notifyListener(userId, user);

                Log.d(TAG, "Updated notification preferences for user: " + userId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // PROFILE PICTURE
    // ========================================================================

    @Override
    public void uploadProfilePicture(String userId, Uri imageUri,
                                     RepositoryCallback<String> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated upload failure"));
                    return;
                }

                User user = users.get(userId);

                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                // Simulate upload by generating URL
                String imageUrl = "mock://profile_pictures/" + userId + ".jpg";

                profilePictures.put(userId, imageUrl);
                users.put(userId, user);

                notifyListener(userId, user);

                Log.d(TAG, "Uploaded profile picture for user: " + userId);
                callback.onSuccess(imageUrl);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void deleteProfilePicture(String userId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated delete failure"));
                    return;
                }

                User user = users.get(userId);

                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                profilePictures.remove(userId);
                users.put(userId, user);

                notifyListener(userId, user);

                Log.d(TAG, "Deleted profile picture for user: " + userId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // SEARCH USERS (Admin)
    // ========================================================================

    @Override
    public void searchUsers(String query, RepositoryCallback<List<User>> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated search failure"));
                    return;
                }

                String lowerQuery = query.toLowerCase();

                List<User> results = users.values().stream()
                        .filter(user ->
                                (user.getName() != null && user.getName().toLowerCase().contains(lowerQuery)) ||
                                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)) ||
                                        user.getId().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());

                Log.d(TAG, "Search for '" + query + "' found " + results.size() + " users");
                callback.onSuccess(results);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // REAL-TIME LISTENER
    // ========================================================================

    @Override
    public void listenToUser(String userId, RepositoryListener<User> listener) {
        userListeners.put(userId, listener);

        // Immediately send current data
        User user = users.get(userId);
        if (user != null) {
            listener.onDataChanged(user);
        } else {
            listener.onError(new Exception("User not found"));
        }
    }

    public void removeListener(String userId) {
        userListeners.remove(userId);
    }

    public void removeAllListeners() {
        userListeners.clear();
    }

    // ========================================================================
    // ACCOUNT MANAGEMENT
    // ========================================================================

    @Override
    public void deactivateAccount(String userId, RepositoryCallback<Void> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated deactivation failure"));
                    return;
                }

                User user = users.get(userId);

                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                // Soft delete
                user.setActive(false);
                users.put(userId, user);

                notifyListener(userId, user);

                Log.d(TAG, "Deactivated account: " + userId);
                callback.onSuccess(null);

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void exportUserData(String userId, RepositoryCallback<String> callback) {
        executeAsync(() -> {
            try {
                if (simulateFailure) {
                    callback.onFailure(new Exception("Simulated export failure"));
                    return;
                }

                User user = users.get(userId);

                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                // Generate JSON export
                StringBuilder json = new StringBuilder();
                json.append("{\n");
                json.append("  \"userId\": \"").append(user.getId()).append("\",\n");
                json.append("  \"name\": \"").append(user.getName()).append("\",\n");
                json.append("  \"email\": \"").append(user.getEmail()).append("\",\n");
                json.append("  \"phone\": \"").append(user.getPhone()).append("\",\n");
                json.append("  \"role\": \"").append(user.getRole()).append("\",\n");
                json.append("  \"createdAt\": ").append(user.getCreatedAt()).append(",\n");
                json.append("}");

                Log.d(TAG, "Exported data for user: " + userId);
                callback.onSuccess(json.toString());

            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void notifyListener(String userId, User user) {
        RepositoryListener<User> listener = userListeners.get(userId);
        if (listener != null) {
            listener.onDataChanged(user);
        }
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

    public int getUserCount() {
        return users.size();
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    public boolean userExists(String userId) {
        return users.containsKey(userId);
    }

    public int getActiveUserCount() {
        return (int) users.values().stream()
                .filter(User::isActive)
                .count();
    }

    public int getInactiveUserCount() {
        return (int) users.values().stream()
                .filter(user -> !user.isActive())
                .count();
    }

    public List<User> getUsersByRole(String role) {
        return users.values().stream()
                .filter(user -> role.equals(user.getRole()))
                .collect(Collectors.toList());
    }

    public boolean hasProfilePicture(String userId) {
        return profilePictures.containsKey(userId);
    }

    public String getProfilePictureUrl(String userId) {
        return profilePictures.get(userId);
    }

    public List<User> getUsersCreatedAfter(long timestamp) {
        return users.values().stream()
                .filter(user -> user.getCreatedAt() > timestamp)
                .collect(Collectors.toList());
    }
}
