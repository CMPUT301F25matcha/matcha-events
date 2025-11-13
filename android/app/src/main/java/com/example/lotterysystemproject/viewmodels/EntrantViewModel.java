package com.example.lotterysystemproject.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.repositories.EntrantRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel that manages entrant data for a specific event.
 * Handles operations such as loading entrants, filtering by status,
 * and performing actions like drawing a lottery or replacements.
 */
public class EntrantViewModel extends ViewModel {
    private EntrantRepository repository;
    private LiveData<List<Entrant>> allEntrants;
    private String currentEventId;

    /**
     * Initializes the EntrantViewModel and repository.
     */
    public EntrantViewModel() {
        repository = new EntrantRepository();
    }

    /**
     * Loads entrants associated with a given event.
     *
     * @param eventId The unique ID of the event.
     */
    public void loadEntrants(String eventId) {
        this.currentEventId = eventId;
        allEntrants = repository.getEntrants(eventId);
    }

    /**
     * Returns all entrants for the current event.
     *
     * @return LiveData list of all entrants.
     */
    public LiveData<List<Entrant>> getAllEntrants() {
        return allEntrants;
    }

    /**
     * Returns only entrants with {@link Entrant.Status#WAITING}.
     *
     * @return LiveData list of waiting entrants.
     */
    public LiveData<List<Entrant>> getWaitingList() {
        return Transformations.map(allEntrants, entrants -> {
            List<Entrant> waiting = new ArrayList<>();
            if (entrants != null) {
                for (Entrant e : entrants) {
                    if (e.getStatus() == Entrant.Status.WAITING) {
                        waiting.add(e);
                    }
                }
            }
            return waiting;
        });
    }

    /**
     * Returns entrants with status other than {@link Entrant.Status#WAITING}.
     *
     * @return LiveData list of selected entrants.
     */
    public LiveData<List<Entrant>> getSelectedEntrants() {
        return Transformations.map(allEntrants, entrants -> {
            List<Entrant> selected = new ArrayList<>();
            if (entrants != null) {
                for (Entrant e : entrants) {
                    if (e.getStatus() != Entrant.Status.WAITING) {
                        selected.add(e);
                    }
                }
            }
            return selected;
        });
    }

    /**
     * Filters selected entrants by a specific status.
     *
     * @param filterStatus The status to filter by (e.g. INVITED, CANCELLED, etc.)
     * @return LiveData list of filtered entrants.
     */
    public LiveData<List<Entrant>> getFilteredSelected(Entrant.Status filterStatus) {
        return Transformations.map(getSelectedEntrants(), entrants -> {
            if (filterStatus == null) {
                return entrants;
            }

            List<Entrant> filtered = new ArrayList<>();
            for (Entrant e : entrants) {
                if (e.getStatus() == filterStatus) {
                    filtered.add(e);
                }
            }
            return filtered;
        });
    }

    /**
     * Performs a lottery draw for a specified number of entrants.
     *
     * @param count     Number of entrants to select.
     * @param listener  Callback for success or failure.
     */
    public void drawLottery(int count, EntrantRepository.OnLotteryCompleteListener listener) {
        repository.drawLottery(currentEventId, count, listener);
    }

    /**
     * Cancels a specific entrant from the list.
     *
     * @param entrantId The ID of the entrant to cancel.
     * @param listener  Callback for operation result.
     */
    public void cancelEntrant(String entrantId, EntrantRepository.OnActionCompleteListener listener) {
        repository.cancelEntrant(entrantId, listener);
    }

    /**
     * Draws a replacement entrant when someone is cancelled.
     *
     * @param listener Callback for success or failure.
     */
    public void drawReplacement(EntrantRepository.OnReplacementDrawnListener listener) {
        repository.drawReplacement(currentEventId, listener);
    }
}
