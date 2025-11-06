package com.example.lotterysystemproject.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.repositories.EntrantRepository;
import java.util.ArrayList;
import java.util.List;

public class EntrantViewModel extends ViewModel {
    private EntrantRepository repository;
    private LiveData<List<Entrant>> allEntrants;
    private String currentEventId;

    public EntrantViewModel() {
        repository = new EntrantRepository();
    }

    // Load entrants for a specific event
    public void loadEntrants(String eventId) {
        this.currentEventId = eventId;
        allEntrants = repository.getEntrants(eventId);
    }

    // Get all entrants
    public LiveData<List<Entrant>> getAllEntrants() {
        return allEntrants;
    }

    // Get waiting list (status = WAITING)
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

    // Get selected entrants (status = INVITED, ENROLLED, or CANCELLED)
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

    // Filter selected entrants by specific status
    public LiveData<List<Entrant>> getFilteredSelected(Entrant.Status filterStatus) {
        return Transformations.map(getSelectedEntrants(), entrants -> {
            if (filterStatus == null) {
                return entrants; // Return all selected
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

    // Draw lottery (US 02.05.02)
    public void drawLottery(int count, EntrantRepository.OnLotteryCompleteListener listener) {
        repository.drawLottery(currentEventId, count, listener);
    }

    // Cancel entrant (US 02.06.04)
    public void cancelEntrant(String entrantId, EntrantRepository.OnActionCompleteListener listener) {
        repository.cancelEntrant(entrantId, listener);
    }

    // Draw replacement (US 02.05.03)
    public void drawReplacement(EntrantRepository.OnReplacementDrawnListener listener) {
        repository.drawReplacement(currentEventId, listener);
    }
}