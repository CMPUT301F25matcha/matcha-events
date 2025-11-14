package com.example.lotterysystemproject.firebasemanager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lotterysystemproject.models.Entrant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of EntrantRepository for testing and development.
 * Simulates backend operations with in-memory data.
 */
public class MockEntrantRepository implements EntrantRepository {
    private MutableLiveData<List<Entrant>> entrantsLiveData;

    /**
     * Initializes the repository with LiveData storage.
     */
    public MockEntrantRepository() {
        entrantsLiveData = new MutableLiveData<>();
    }

    /**
     * Returns LiveData list of entrants for the given event ID.
     * Loads mock data for testing purposes.
     * @param eventId ID of the event to fetch entrants for.
     * @return LiveData containing list of entrants.
     */
    @Override
    public LiveData<List<Entrant>> getEntrants(String eventId) {
        loadMockData(eventId);
        return entrantsLiveData;
    }

    /**
     * Loads mock entrant data. Generates 130 mock entrants for event ID "1".
     * @param eventId ID of the event being loaded.
     */
    private void loadMockData(String eventId) {
        List<Entrant> mockEntrants = new ArrayList<>();
        long now = System.currentTimeMillis();
        long oneDay = 86_400_000L;

        if (!eventId.equals("1")) {
            entrantsLiveData.setValue(mockEntrants);
            return;
        }

        for (int i = 1; i <= 130; i++) {
            Entrant entrant = new Entrant("Person " + i, "person" + i + "@email.com");
            entrant.setId("waiting_" + i);
            entrant.setEventId(eventId);
            entrant.setStatus(Entrant.Status.WAITING);
            entrant.setJoinedTimestamp(now - (i * oneDay));
            entrant.setStatusTimestamp(now - (i * oneDay));
            mockEntrants.add(entrant);
        }

        entrantsLiveData.setValue(mockEntrants);
    }

    /**
     * Performs random lottery draw. A quarter of selected entrants are automatically enrolled.
     * @param eventId ID of the event for lottery draw.
     * @param count Number of entrants to select.
     * @param listener Callback to report completion or errors.
     */
    @Override
    public void drawLottery(String eventId, int count, OnLotteryCompleteListener listener) {
        List<Entrant> allEntrants = entrantsLiveData.getValue();
        if (allEntrants == null) {
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

        entrantsLiveData.setValue(allEntrants);
        listener.onComplete(winners);
    }

    /**
     * Cancels an entrant's participation by ID.
     * @param entrantId ID of the entrant to cancel.
     * @param listener Callback to signal success or failure.
     */
    @Override
    public void cancelEntrant(String entrantId, OnActionCompleteListener listener) {
        List<Entrant> allEntrants = entrantsLiveData.getValue();
        if (allEntrants == null) {
            listener.onFailure("No entrants available");
            return;
        }

        for (Entrant e : allEntrants) {
            if (e.getId().equals(entrantId)) {
                e.setStatus(Entrant.Status.CANCELLED);
                break;
            }
        }

        entrantsLiveData.setValue(allEntrants);
        listener.onSuccess();
    }

    /**
     * Draws replacement entrant from waiting list.
     * @param eventId ID of the event for replacement draw.
     * @param listener Callback to report result or errors.
     */
    @Override
    public void drawReplacement(String eventId, OnReplacementDrawnListener listener) {
        List<Entrant> allEntrants = entrantsLiveData.getValue();
        if (allEntrants == null) {
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

        entrantsLiveData.setValue(allEntrants);
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
            // Access the mock users through a helper method we'll add
            mockRepo.getUserInfo(deviceId, listener);
        } else {
            listener.onFailure("Not using MockEventRepository");
        }
    }
}