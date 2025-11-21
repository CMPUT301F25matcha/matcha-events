package com.example.lotterysystemproject.firebasemanager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lotterysystemproject.models.Entrant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of EntrantRepository for testing and development.
 * Simulates backend operations with in-memory data.
 */
public class MockEntrantRepository implements EntrantRepository {
    // Store entrants by event ID for better management
    private final Map<String, MutableLiveData<List<Entrant>>> entrantsByEvent = new HashMap<>();
    private final Map<String, List<Entrant>> entrantsData = new HashMap<>();

    /**
     * Initializes the repository with LiveData storage.
     */
    public MockEntrantRepository() {
        // Pre-initialize mock data for event "1"
        initializeMockData();
    }

    /**
     * Pre-initializes mock data so it's available immediately for lottery operations.
     */
    private void initializeMockData() {
        List<Entrant> mockEntrants = new ArrayList<>();
        long now = System.currentTimeMillis();
        long oneDay = 86_400_000L;

        for (int i = 1; i <= 130; i++) {
            Entrant entrant = new Entrant("Person " + i, "person" + i + "@email.com");
            entrant.setId("waiting_" + i);
            entrant.setEventId("1");
            entrant.setStatus(Entrant.Status.WAITING);
            entrant.setJoinedTimestamp(now - (i * oneDay));
            entrant.setStatusTimestamp(now - (i * oneDay));
            mockEntrants.add(entrant);
        }

        // Store the data
        entrantsData.put("1", mockEntrants);
    }

    /**
     * Returns LiveData list of entrants for the given event ID.
     * Loads mock data for testing purposes.
     * @param eventId ID of the event to fetch entrants for.
     * @return LiveData containing list of entrants.
     */
    @Override
    public LiveData<List<Entrant>> getEntrants(String eventId) {
        // Get or create LiveData for this event
        if (!entrantsByEvent.containsKey(eventId)) {
            MutableLiveData<List<Entrant>> liveData = new MutableLiveData<>();
            entrantsByEvent.put(eventId, liveData);

            // Get the data for this event (or empty list if none exists)
            List<Entrant> data = entrantsData.getOrDefault(eventId, new ArrayList<>());
            liveData.setValue(data);
        }

        return entrantsByEvent.get(eventId);
    }

    /**
     * Gets the current entrant list for an event directly (not LiveData).
     * Used internally by lottery operations.
     */
    private List<Entrant> getEntrantsList(String eventId) {
        return entrantsData.getOrDefault(eventId, new ArrayList<>());
    }

    /**
     * Updates the entrant list and notifies observers.
     */
    private void updateEntrants(String eventId, List<Entrant> entrants) {
        entrantsData.put(eventId, entrants);

        // Update LiveData if it exists
        MutableLiveData<List<Entrant>> liveData = entrantsByEvent.get(eventId);
        if (liveData != null) {
            liveData.setValue(entrants);
        }
    }

    /**
     * Performs random lottery draw. A quarter of selected entrants are automatically enrolled.
     * @param eventId ID of the event for lottery draw.
     * @param count Number of entrants to select.
     * @param listener Callback to report completion or errors.
     */
    @Override
    public void drawLottery(String eventId, int count, OnLotteryCompleteListener listener) {
        List<Entrant> allEntrants = getEntrantsList(eventId);

        if (allEntrants.isEmpty()) {
            listener.onFailure("No entrants available");
            return;
        }

        List<Entrant> waitingList = new ArrayList<>();
        for (Entrant e : allEntrants) {
            if (e.getStatus() == Entrant.Status.WAITING) {
                waitingList.add(e);
            }
        }

        if (waitingList.isEmpty()) {
            listener.onFailure("Waiting list is empty");
            return;
        }

        Collections.shuffle(waitingList);
        int selected = Math.min(count, waitingList.size());
        List<Entrant> winners = new ArrayList<>();

        for (int i = 0; i < selected; i++) {
            Entrant winner = waitingList.get(i);
            if (i < selected / 4) {
                winner.setStatus(Entrant.Status.ENROLLED);
            } else {
                winner.setStatus(Entrant.Status.INVITED);
            }
            winners.add(winner);
        }

        updateEntrants(eventId, allEntrants);
        listener.onComplete(winners);
    }

    /**
     * Cancels an entrant's participation by ID.
     * @param entrantId ID of the entrant to cancel.
     * @param listener Callback to signal success or failure.
     */
    @Override
    public void cancelEntrant(String entrantId, OnActionCompleteListener listener) {
        // Find the entrant across all events
        boolean found = false;

        for (Map.Entry<String, List<Entrant>> entry : entrantsData.entrySet()) {
            String eventId = entry.getKey();
            List<Entrant> entrants = entry.getValue();

            for (Entrant e : entrants) {
                if (e.getId().equals(entrantId)) {
                    e.setStatus(Entrant.Status.CANCELLED);
                    updateEntrants(eventId, entrants);
                    found = true;
                    break;
                }
            }

            if (found) break;
        }

        if (found) {
            listener.onSuccess();
        } else {
            listener.onFailure("Entrant not found");
        }
    }

    /**
     * Draws replacement entrant from waiting list.
     * @param eventId ID of the event for replacement draw.
     * @param listener Callback to report result or errors.
     */
    @Override
    public void drawReplacement(String eventId, OnReplacementDrawnListener listener) {
        List<Entrant> allEntrants = getEntrantsList(eventId);

        if (allEntrants.isEmpty()) {
            listener.onFailure("No entrants available");
            return;
        }

        List<Entrant> waitingList = new ArrayList<>();
        for (Entrant e : allEntrants) {
            if (e.getStatus() == Entrant.Status.WAITING) {
                waitingList.add(e);
            }
        }

        if (waitingList.isEmpty()) {
            listener.onFailure("No entrants in waiting list");
            return;
        }

        Collections.shuffle(waitingList);
        Entrant replacement = waitingList.get(0);
        replacement.setStatus(Entrant.Status.INVITED);

        updateEntrants(eventId, allEntrants);
        listener.onSuccess(replacement);
    }

    /**
     * Gets the current user info from the EventRepository's shared mock users.
     * This ensures consistency between what's saved during sign-up and what's looked up later.
     * @param deviceId The device ID to look up.
     * @param listener Callback to report success or failure.
     */
    @Override
    public void getCurrentUserInfo(String deviceId, OnUserInfoListener listener) {
        // Get the MockEventRepository instance to access its mock users
        EventRepository eventRepo = RepositoryProvider.getEventRepository();

        // Only works if eventRepo is a MockEventRepository
        if (eventRepo instanceof MockEventRepository) {
            MockEventRepository mockRepo = (MockEventRepository) eventRepo;
            // Access the mock users through a helper method
            mockRepo.getUserInfo(deviceId, listener);
        } else {
            listener.onFailure("Not using MockEventRepository");
        }
    }
}