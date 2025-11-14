package com.example.lotterysystemproject.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.lotterysystemproject.firebasemanager.EntrantRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.Entrant;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel that manages entrant data for a specific event.
 * Handles operations such as loading entrants, filtering by status,
 * and performing actions like drawing a lottery or replacements.
 */
public class EntrantViewModel extends ViewModel {
    private EntrantRepository repository;
    private LiveData<List<Entrant>> entrantsLiveData;
    private String currentEventId;

    /**
     * Initializes the EntrantViewModel with repository from provider.
     */
    public EntrantViewModel() {
        repository = RepositoryProvider.getEntrantRepository();
    }

    /**
     * Loads entrants associated with a given event.
     *
     * @param eventId The unique ID of the event.
     */
    public void loadEntrants(String eventId) {
        this.currentEventId = eventId;
        entrantsLiveData = repository.getEntrants(eventId);
    }

    /**
     * Returns LiveData of entrants.
     * @return LiveData containing list of entrants.
     */
    public LiveData<List<Entrant>> getEntrants() {
        return entrantsLiveData;
    }

    /**
     * Performs lottery draw for specified number of entrants.
     * @param count Number of entrants to select.
     * @param listener Callback for success or failure.
     */
    public void drawLottery(int count, EntrantRepository.OnLotteryCompleteListener listener) {
        repository.drawLottery(currentEventId, count, listener);
    }

    /**
     * Cancels a specific entrant from the list.
     * @param entrantId The ID of the entrant to cancel.
     * @param listener Callback for operation result.
     */
    public void cancelEntrant(String entrantId, EntrantRepository.OnActionCompleteListener listener) {
        repository.cancelEntrant(entrantId, listener);
    }

    /**
     * Returns only entrants with {@link Entrant.Status#WAITING}.
     *
     * @return LiveData list of waiting entrants.
     */
    public LiveData<List<Entrant>> getWaitingList() {
        return Transformations.map(entrantsLiveData, entrants -> {
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
     * Draws replacement entrant when someone is cancelled.
     * @param listener Callback for success or failure.
     */
    public void drawReplacement(EntrantRepository.OnReplacementDrawnListener listener) {
        repository.drawReplacement(currentEventId, listener);
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
     * Returns entrants with status other than {@link Entrant.Status#WAITING}.
     *
     * @return LiveData list of selected entrants.
     */
    public LiveData<List<Entrant>> getSelectedEntrants() {
        return Transformations.map(entrantsLiveData, entrants -> {
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

}
