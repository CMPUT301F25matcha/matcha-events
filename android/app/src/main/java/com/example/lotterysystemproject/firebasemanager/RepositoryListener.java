package com.example.lotterysystemproject.firebasemanager;

/**
 * Listener interface for real-time data updates
 * Used for Firestore snapshot listeners
 *
 * @param <T> The type of data being listened to
 */
public interface RepositoryListener<T> {

    /**
     * Called when data changes in real-time
     *
     * @param data The updated data
     */
    void onDataChanged(T data);

    /**
     * Called when an error occurs in the listener
     *
     * @param e The exception that caused the error
     */
    void onError(Exception e);
}