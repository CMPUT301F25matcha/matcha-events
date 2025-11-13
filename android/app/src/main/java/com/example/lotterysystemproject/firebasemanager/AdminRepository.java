package com.example.lotterysystemproject.firebasemanager;

import androidx.annotation.Nullable;

import com.example.lotterysystemproject.models.EventAdmin;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Repository interface for admin-specific operations.
 * Handles event management, image management, and user management for administrators.
 */
public interface AdminRepository {

    // ===================== CALLBACK INTERFACES =====================

    /**
     * A generic callback interface for operations that do not return data.
     */
    interface AdminCallback {
        /** Called on successful completion of the operation. */
        void onSuccess();
        /** Called when the operation fails. */
        void onError(Exception e);
    }

    // ===================== ACCESSORS =====================

    /**
     * Gets the FirebaseFirestore instance if available.
     * @return The Firestore instance, or null if not using Firebase.
     */
    @Nullable
    FirebaseFirestore getDatabase();

    /**
     * Gets the FirebaseStorage instance if available.
     * @return The FirebaseStorage instance, or null if not using Firebase.
     */
    @Nullable
    FirebaseStorage getStorage();

    // ===================== EVENT OPERATIONS =====================

    /**
     * Adds a new event to the repository.
     * @param eventAdmin The EventAdmin object to add.
     * @param callback The callback to handle success or failure.
     */
    void addEvent(EventAdmin eventAdmin, AdminCallback callback);

    /**
     * Retrieves all events (including inactive ones) for admin browsing.
     * @param onSuccess A consumer for the list of EventAdmin objects.
     * @param onError A consumer for any exception that occurs.
     */
    void getAllEvents(Consumer<List<EventAdmin>> onSuccess, Consumer<Exception> onError);

    /**
     * Listens for real-time updates to all events for admin browsing.
     * @param onSuccess A consumer for the list of EventAdmin objects when updated.
     * @param onError A consumer for any exception that occurs.
     */
    void listenToAllEvents(Consumer<List<EventAdmin>> onSuccess, Consumer<Exception> onError);

    /**
     * Deletes an event from the repository.
     * @param eventId The ID of the event to delete.
     * @param callback The callback to handle success or failure.
     */
    void deleteEvent(String eventId, AdminCallback callback);

    // ===================== USER OPERATIONS =====================

    /**
     * Deletes a user from the repository.
     * @param userId The ID of the user to delete.
     * @param callback The callback to handle success or failure.
     */
    void deleteUser(String userId, AdminCallback callback);

    // ===================== IMAGE OPERATIONS =====================

    /**
     * Retrieves all image URLs from storage for admin browsing.
     * @param onSuccess A consumer for the list of image URLs.
     * @param onError A consumer for any exception that occurs.
     */
    void getAllImages(Consumer<List<String>> onSuccess, Consumer<Exception> onError);

    /**
     * Deletes a single image from storage.
     * @param imageUrl The URL of the image to delete.
     * @param callback The callback to handle success or failure.
     */
    void deleteImage(String imageUrl, AdminCallback callback);

    /**
     * Deletes multiple images from storage.
     * @param imageUrls The list of image URLs to delete.
     * @param onComplete A bi-consumer that receives the count of successfully deleted images and any error.
     */
    void deleteMultipleImages(List<String> imageUrls, BiConsumer<Integer, Exception> onComplete);
}