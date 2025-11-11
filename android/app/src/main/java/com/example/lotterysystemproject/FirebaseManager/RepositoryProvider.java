package com.example.lotterysystemproject.FirebaseManager;

/**
 * Factory class that provides the appropriate repository implementations.
 * This allows for easy switching between mock and Firebase modes.
 */
public class RepositoryProvider {

    /** Toggles between Firebase and mock mode. Set to false for testing, true for production. */
    private static final boolean USE_FIREBASE = false;

    private static EventRepository eventRepositoryInstance;
    private static AdminRepository adminRepositoryInstance;

    /**
     * Returns the singleton instance of the EventRepository.
     * The implementation returned depends on the USE_FIREBASE flag.
     *
     * @return The EventRepository instance (either Mock or Firebase implementation).
     */
    public static EventRepository getEventRepository() {
        if (eventRepositoryInstance == null) {
            synchronized (RepositoryProvider.class) {
                if (eventRepositoryInstance == null) {
                    if (USE_FIREBASE) {
                        eventRepositoryInstance = new FirebaseEventRepository();
                    } else {
                        eventRepositoryInstance = new MockEventRepository();
                    }
                }
            }
        }
        return eventRepositoryInstance;
    }

    /**
     * Returns the singleton instance of the AdminRepository.
     * The implementation returned depends on the USE_FIREBASE flag.
     *
     * @return The AdminRepository instance (either Mock or Firebase implementation).
     */
    public static AdminRepository getAdminRepository() {
        if (adminRepositoryInstance == null) {
            synchronized (RepositoryProvider.class) {
                if (adminRepositoryInstance == null) {
                    if (USE_FIREBASE) {
                        adminRepositoryInstance = new FirebaseAdminRepository();
                    } else {
                        adminRepositoryInstance = new MockAdminRepository();
                    }
                }
            }
        }
        return adminRepositoryInstance;
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use getEventRepository() instead.
     */
    @Deprecated
    public static EventRepository getInstance() {
        return getEventRepository();
    }

    /**
     * Resets the singleton instances. Useful for testing purposes.
     * WARNING: Only call this during testing or app restart.
     */
    public static void resetInstances() {
        synchronized (RepositoryProvider.class) {
            eventRepositoryInstance = null;
            adminRepositoryInstance = null;
        }
    }

    /**
     * Checks if the current implementation is using Firebase.
     *
     * @return true if using Firebase, false if using mock data.
     */
    public static boolean isUsingFirebase() {
        return USE_FIREBASE;
    }
}