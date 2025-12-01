package com.example.lotterysystemproject.firebasemanager;

/**
 * Factory class that provides the appropriate repository implementations.
 * This allows for easy switching between mock and Firebase modes.
 */
public class RepositoryProvider {

    /** Toggles between Firebase and mock mode. Set to false for testing, true for production. */
    private static final boolean USE_FIREBASE = true;

    private static EventRepository eventRepositoryInstance;
    private static AdminRepository adminRepositoryInstance;
    private static EntrantRepository entrantRepositoryInstance;
    private static UserRepository userRepositoryInstance;
    private static NotificationRepository notificationRepository;


    /**
     * Returns the singleton instance of the EventRepository.
     * @return The EventRepository instance (either Mock or Firebase implementation).
     */
    public static EventRepository getEventRepository() {
        if (eventRepositoryInstance == null) {
            synchronized (RepositoryProvider.class) {
                if (eventRepositoryInstance == null) {
                    if (USE_FIREBASE) {
                        eventRepositoryInstance = new FirebaseEventRepository();
                    }
                }
            }
        }
        return eventRepositoryInstance;
    }

    /**
     * Returns the singleton instance of the UserRepository.
     * @return The UserRepository instance (either Mock or Firebase implementation).
     */
    public static UserRepository getUserRepository() {
        if (userRepositoryInstance == null) {
            synchronized (RepositoryProvider.class) {
                if (userRepositoryInstance == null) {
                    if (USE_FIREBASE) {
                        userRepositoryInstance = new FirebaseUserRepository();
                    }
                }
            }
        }
        return userRepositoryInstance;
    }

    /**
     * Returns the singleton instance of the AdminRepository.
     * @return The AdminRepository instance (either Mock or Firebase implementation).
     */
    public static AdminRepository getAdminRepository() {
        if (adminRepositoryInstance == null) {
            synchronized (RepositoryProvider.class) {
                if (adminRepositoryInstance == null) {
                    if (USE_FIREBASE) {
                        adminRepositoryInstance = new FirebaseAdminRepository();
                    }
                }
            }
        }
        return adminRepositoryInstance;
    }

    /**
     * Returns the singleton instance of the EntrantRepository.
     * @return The EntrantRepository instance (either Mock or Firebase implementation).
     */
    public static EntrantRepository getEntrantRepository() {
        if (entrantRepositoryInstance == null) {
            synchronized (RepositoryProvider.class) {
                if (entrantRepositoryInstance == null) {
                    if (USE_FIREBASE) {
                        entrantRepositoryInstance = new FirebaseEntrantRepository();
                    }
                }
            }
        }
        return entrantRepositoryInstance;
    }

    /**
     * Returns the singleton instance of the NotificationRepository.
     * @return The NotificationRepository instance.
     */
    public static synchronized NotificationRepository getNotificationRepository() {
        if (notificationRepository == null) {
            notificationRepository = new FirebaseNotificationRepository();
        }
        return notificationRepository;
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
            entrantRepositoryInstance = null;
        }
    }

    /**
     * Checks if the current implementation is using Firebase.
     * @return true if using Firebase, false if using mock data.
     */
    public static boolean isUsingFirebase() {
        return USE_FIREBASE;
    }
}