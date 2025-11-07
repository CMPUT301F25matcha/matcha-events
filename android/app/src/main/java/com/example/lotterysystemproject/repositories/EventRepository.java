package com.example.lotterysystemproject.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lotterysystemproject.Models.EventAdmin;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Repository responsible for managing event data.
 * Provides mock data and supports adding new events.
 */
public class EventRepository {
    private MutableLiveData<List<EventAdmin>> eventsLiveData;
    private List<EventAdmin> eventsList;
    private int nextId = 2; // Next available ID (mock data uses "1")

    /**
     * Initializes the repository and loads mock data.
     */
    public EventRepository() {
        eventsLiveData = new MutableLiveData<>();
        eventsList = new ArrayList<>();
        loadMockData();
    }

    /**
     * Returns a LiveData object containing the list of all events.
     *
     * @return LiveData of EventAdmin list.
     */
    public LiveData<List<EventAdmin>> getEvents() {
        return eventsLiveData;
    }

    /**
     * Adds a new event to the repository.
     * Assigns a unique ID automatically.
     *
     * @param event Event to add.
     */
    public void addEvent(EventAdmin event) {
        event.setId(String.valueOf(nextId++));
        eventsList.add(event);
        eventsLiveData.setValue(new ArrayList<>(eventsList));
    }

    /**
     * Loads sample mock data into the repository.
     * Currently creates a single "Swimming Lessons" event.
     */
    private void loadMockData() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JANUARY, 15);

        EventAdmin event1 = new EventAdmin(
                "Swimming Lessons",
                cal.getTime(),
                "2:00 PM",
                "Community Pool",
                20
        );

        event1.setId("1");
        event1.setEnrolled(0);
        event1.setStatus("open");
        event1.setDescription("Beginner swimming for kids ages 6–12");
        event1.setMaxWaitingList(130);

        eventsList.add(event1);
        eventsLiveData.setValue(new ArrayList<>(eventsList));
    }
}
