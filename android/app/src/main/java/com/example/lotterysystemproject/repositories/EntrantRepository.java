package com.example.lotterysystemproject.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lotterysystemproject.Models.Entrant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository class responsible for managing entrant data.
 * Simulates backend operations such as loading entrants,
 * drawing lotteries, canceling entrants, and drawing replacements.
 */
public class EntrantRepository {
    private MutableLiveData<List<Entrant>> entrantsLiveData;

    /**
     * Initializes the repository and prepares LiveData storage.
     */
    public EntrantRepository() {
        entrantsLiveData = new MutableLiveData<>();
    }

    /**
     * Returns a LiveData list of entrants for the given event ID.
     * Loads mock data for testing purposes.
     *
     * @param eventId ID of the event to fetch entrants for.
     * @return LiveData containing a list of entrants.
     */
    public LiveData<List<Entrant>> getEntrants(String eventId) {
        loadMockData(eventId);
        return entrantsLiveData;
    }

    /**
     * Loads mock entrant data for testing or demo purposes.
     * Currently generates 130 mock entrants for event ID "1".
     *
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
     * Performs a random lottery draw to select entrants.
     * A quarter of selected entrants are automatically enrolled,
     * and the rest are invited.
     *
     * @param eventId  ID of the event for which to draw entrants.
     * @param count    Number of entrants to select.
     * @param listener Callback to report completion or errors.
     */
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
     * Cancels an entrant’s participation by ID.
     *
     * @param entrantId ID of the entrant to cancel.
     * @param listener  Callback to signal success or failure.
     */
    public void cancelEntrant(String entrantId, OnActionCompleteListener listener) {
        List<Entrant> allEntrants = entrantsLiveData.getValue();
        if (allEntrants == null) return;

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
     * Draws a replacement entrant from the waiting list.
     *
     * @param eventId  ID of the event for which to draw replacement.
     * @param listener Callback to report the result or errors.
     */
    public void drawReplacement(String eventId, OnReplacementDrawnListener listener) {
        List<Entrant> allEntrants = entrantsLiveData.getValue();
        if (allEntrants == null) return;

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
     * Callback for reporting lottery completion results.
     */
    public interface OnLotteryCompleteListener {
        void onComplete(List<Entrant> winners);
        void onFailure(String error);
    }

    /**
     * Callback for generic success or failure actions.
     */
    public interface OnActionCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Callback for when a replacement entrant is successfully drawn.
     */
    public interface OnReplacementDrawnListener {
        void onSuccess(Entrant replacement);
        void onFailure(String error);
    }
}
