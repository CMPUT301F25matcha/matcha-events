package com.example.lotterysystemproject.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lotterysystemproject.Models.Event;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventRepository {
    private MutableLiveData<List<Event>> eventsLiveData;
    private List<Event> eventsList; // Keep a reference to the list
    private int nextId = 2; // Start from 2 since mock event has id "1"

    public EventRepository() {
        eventsLiveData = new MutableLiveData<>();
        eventsList = new ArrayList<>();
        loadMockData();
    }

    public LiveData<List<Event>> getEvents() {
        return eventsLiveData;
    }

    // Add new event (US 02.01.01)
    public void addEvent(Event event) {
        // Generate ID for new event
        event.setId(String.valueOf(nextId++));
        eventsList.add(event);
        eventsLiveData.setValue(new ArrayList<>(eventsList));
    }

    // Mock data for testing
    private void loadMockData() {
        Calendar cal = Calendar.getInstance();

        // Only ONE pre-made event: Swimming Lessons
        cal.set(2025, 0, 15); // Jan 15, 2025
        Event event1 = new Event("Swimming Lessons", cal.getTime(), "2:00 PM", "Community Pool", 20);
        event1.setId("1");
        event1.setEnrolled(0);  // No one enrolled yet
        event1.setStatus("open");
        event1.setDescription("Beginner swimming for kids ages 6-12");
        event1.setMaxWaitingList(130);
        eventsList.add(event1);

        eventsLiveData.setValue(new ArrayList<>(eventsList));
    }
}