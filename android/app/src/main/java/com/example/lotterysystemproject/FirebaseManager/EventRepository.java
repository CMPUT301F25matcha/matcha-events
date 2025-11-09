package com.example.lotterysystemproject.FirebaseManager;

import androidx.annotation.Nullable;

import com.example.lotterysystemproject.Models.Event;
import com.example.lotterysystemproject.Models.Registration;
import com.example.lotterysystemproject.Models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Repository interface for managing event and user data operations.
 * Implementations can provide either mock data or real Firebase connectivity.
 */
public interface EventRepository {

    // ===================== CALLBACK INTERFACES =====================

    /**
     * A generic callback interface for operations that do not return data.
     */
    interface RepositoryCallback {
        /** Called on successful completion of the operation. */
        void onSuccess();
        /** Called when the operation fails. */
        void onError(Exception e);
    }

    /**
     * A listener interface for receiving updates to a list of registrations.
     */
    interface RegistrationsListener {
        /**
         * Called when the list of registrations has changed.
         * @param items The updated list of Registration objects.
         */
        void onChanged(List<Registration> items);
        /** Called when an error occurs while listening for changes. */
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

    /**
     * Gets a reference to a specific Firestore collection.
     * @param name The name of the collection.
     * @return A CollectionReference to the specified collection.
     * @throws IllegalStateException if Firebase is not available.
     */
    CollectionReference getCollection(String name);

    // ===================== USER OPERATIONS =====================

    /**
     * Adds a user to the repository.
     * @param user The User object to add.
     * @param callback The callback to handle success or failure.
     */
    void addUser(User user, RepositoryCallback callback);

    /**
     * Updates user information in the repository.
     * @param userId The ID of the user to update.
     * @param updates A map of fields to update.
     * @param callback The callback to handle success or failure.
     */
    void updateUser(String userId, Map<String, Object> updates, RepositoryCallback callback);

    /**
     * Retrieves a user from the repository.
     * @param userId The ID of the user to retrieve.
     * @param onSuccess A consumer for the retrieved User object.
     * @param onError A consumer for any exception that occurs.
     */
    void getUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError);

    /**
     * Deletes a user from the repository.
     * @param userId The ID of the user to delete.
     * @param callback The callback to handle success or failure.
     */
    void deleteUser(String userId, RepositoryCallback callback);

    // ===================== EVENT OPERATIONS =====================

    /**
     * Retrieves all active events.
     * @param onSuccess A consumer for the list of Event objects.
     * @param onError A consumer for any exception that occurs.
     */
    void getAllEvents(Consumer<List<Event>> onSuccess, Consumer<Exception> onError);

    /**
     * Adds a user to an event's waiting list.
     * @param eventId The ID of the event.
     * @param userId The ID of the user to add to the waiting list.
     * @param callback The callback to handle success or failure.
     */
    void joinWaitingList(String eventId, String userId, RepositoryCallback callback);

    /**
     * Removes a user from an event's waiting list.
     * @param eventId The ID of the event.
     * @param userId The ID of the user to remove.
     * @param callback The callback to handle success or failure.
     */
    void leaveWaitingList(String eventId, String userId, RepositoryCallback callback);

    // ===================== REGISTRATION OPERATIONS =====================

    /**
     * Listens for real-time updates to a user's event registrations.
     * @param userId The ID of the user whose registrations to listen for.
     * @param listener The listener to be notified of changes.
     */
    void listenUserRegistrations(String userId, RegistrationsListener listener);

    /**
     * Stops listening for user registration updates.
     */
    void stopListeningUserRegistrations();

    /**
     * Creates or updates a registration when a user joins an event.
     * @param userId The ID of the user.
     * @param eventId The ID of the event.
     * @param eventTitleSnapshot A snapshot of the event title at the time of registration.
     * @param callback The callback to handle success or failure.
     */
    void upsertRegistrationOnJoin(String userId, String eventId, String eventTitleSnapshot, RepositoryCallback callback);
}