package com.example.lotterysystemproject.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lotterysystemproject.models.Entrant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntrantRepository {
    private MutableLiveData<List<Entrant>> entrantsLiveData;

    public EntrantRepository() {
        entrantsLiveData = new MutableLiveData<>();
    }

    // Get all entrants for a specific event
    public LiveData<List<Entrant>> getEntrants(String eventId) {
        loadMockData(eventId);
        return entrantsLiveData;
    }

    // Mock data for testing
    private void loadMockData(String eventId) {
        List<Entrant> mockEntrants = new ArrayList<>();
        long now = System.currentTimeMillis();
        long oneHour = 3600000;
        long oneDay = 86400000;

        // Create mock entrants for the Swimming Lessons event (id = "1")
        if (!eventId.equals("1")) {
            // For newly created events, start with empty list
            entrantsLiveData.setValue(mockEntrants);
            return;
        }

        // === WAITING LIST (130 people) - for Swimming Lessons only ===
        for (int i = 1; i <= 130; i++) {
            Entrant entrant = new Entrant("Person " + i, "person" + i + "@email.com");
            entrant.setId("waiting_" + i);
            entrant.setEventId(eventId);
            entrant.setStatus(Entrant.Status.WAITING);
            entrant.setJoinedTimestamp(now - (i * oneDay)); // Joined i days ago
            entrant.setStatusTimestamp(now - (i * oneDay));
            mockEntrants.add(entrant);
        }

        entrantsLiveData.setValue(mockEntrants);
    }

    // Draw lottery (US 02.05.02)
    public void drawLottery(String eventId, int count, OnLotteryCompleteListener listener) {
        List<Entrant> allEntrants = entrantsLiveData.getValue();
        if (allEntrants == null) {
            listener.onFailure("No entrants available");
            return;
        }

        // Get waiting list
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

        // Shuffle and select
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

        // Update LiveData to trigger UI refresh
        entrantsLiveData.setValue(allEntrants);
        listener.onComplete(winners);
    }

    // Cancel entrant (US 02.06.04)
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

    // Draw replacement (US 02.05.03)
    public void drawReplacement(String eventId, OnReplacementDrawnListener listener) {
        List<Entrant> allEntrants = entrantsLiveData.getValue();
        if (allEntrants == null) return;

        // Get waiting list
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

        // Select random replacement
        Collections.shuffle(waitingList);
        Entrant replacement = waitingList.get(0);
        replacement.setStatus(Entrant.Status.INVITED);

        entrantsLiveData.setValue(allEntrants);
        listener.onSuccess(replacement);
    }

    // Callback interfaces
    public interface OnLotteryCompleteListener {
        void onComplete(List<Entrant> winners);
        void onFailure(String error);
    }

    public interface OnActionCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnReplacementDrawnListener {
        void onSuccess(Entrant replacement);
        void onFailure(String error);
    }
}