package com.example.lotterysystemproject.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.lotterysystemproject.models.EventAdmin;
import com.example.lotterysystemproject.repositories.EventRepository;
import java.util.List;

/**
 * ViewModel for managing event data.
 * Provides access to all events and supports creating new events.
 */
public class EventViewModel extends ViewModel {
    private EventRepository repository;
    private LiveData<List<EventAdmin>> events;

    /**
     * Initializes the EventViewModel and loads events from the repository.
     */
    public EventViewModel() {
        repository = new EventRepository();
        events = repository.getEvents();
    }

    /**
     * Returns all available events.
     *
     * @return LiveData list of {@link EventAdmin} objects.
     */
    public LiveData<List<EventAdmin>> getEvents() {
        return events;
    }

    /**
     * Creates and adds a new event.
     *
     * @param event The event to add.
     */
    public void createEvent(EventAdmin event) {
        repository.addEvent(event);
    }
}
