package com.example.lotterysystemproject.firebasemanager;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.models.Registration;
import com.example.lotterysystemproject.models.User;
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
     */
    LiveData<List<Event>> getAllEvents();

    /**
     * Adds an event to the repository.
     * @param event The Event object to add.
     * @param onError A consumer for any exception that occurs.
     */
    void addEvent(Event event, Consumer<Exception> onError);

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

    /**
     * Retrieves events matching the provided category.
     *
     * @param category the category to filter by (exact match string)
     * @param onSuccess consumer receiving the list of matching events
     * @param onError consumer receiving any exception
     */
    void getEventsByCategory(String category, Consumer<List<Event>> onSuccess, Consumer<Exception> onError);

    /**
     * Retrieves all active events (events where isActive == true).
     *
     * @param onSuccess callback providing the list of active events
     * @param onError   callback providing an Exception, if any
     */
    void getActiveEvents(
            java.util.function.Consumer<java.util.List<com.example.lotterysystemproject.models.Event>> onSuccess,
            java.util.function.Consumer<Exception> onError
    );

    /**
     * Gets the most recent events ordered by createdAt descending.
     * @param limit maximum number of events to return
     * @param onSuccess consumer receiving the events list
     * @param onError consumer receiving any exception
     */
    void getRecentEvents(int limit, java.util.function.Consumer<java.util.List<com.example.lotterysystemproject.models.Event>> onSuccess, java.util.function.Consumer<Exception> onError);

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

    /**
     * Updates a user's role to organizer.
     * @param userId The user's ID
     * @param callback Callback to signal success or failure
     */
    void updateUserRoleToOrganizer(String userId, RepositoryCallback callback);
}