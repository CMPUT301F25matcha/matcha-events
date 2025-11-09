package com.example.lotterysystemproject.FirebaseManager;

/**
 * Factory class that provides the appropriate EventRepository implementation.
 * This allows for easy switching between mock and Firebase modes.
 */
public class RepositoryProvider {

    /** Toggles between Firebase and mock mode. Set to false for testing, true for production. */
    private static final boolean USE_FIREBASE = false;

    private static EventRepository instance;

    /**
     * Returns the singleton instance of the EventRepository.
     * The implementation returned depends on the USE_FIREBASE flag.
     *
     * @return The EventRepository instance (either Mock or Firebase implementation).
     */
    public static EventRepository getInstance() {
        if (instance == null) {
            synchronized (RepositoryProvider.class) {
                if (instance == null) {
                    if (USE_FIREBASE) {
                        instance = new FirebaseEventRepository();
                    } else {
                        instance = new MockEventRepository();
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Resets the singleton instance. Useful for testing purposes.
     * WARNING: Only call this during testing or app restart.
     */
    public static void resetInstance() {
        synchronized (RepositoryProvider.class) {
            instance = null;
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