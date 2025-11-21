package com.example.lotterysystemproject.firebasemanager;

/**
 * Callback interface for asynchronous repository operations
 *
 * @param <T> The type of data returned on success
 */
public interface RepositoryCallback<T> {

    /**
     * Called when the operation completes successfully
     *
     * @param result The result of the operation (may be null for void operations)
     */
    void onSuccess(T result);

    /**
     * Called when the operation fails
     *
     * @param e The exception that caused the failure
     */
    void onFailure(Exception e);
}

