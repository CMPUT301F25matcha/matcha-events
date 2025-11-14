package com.example.lotterysystemproject.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import java.util.List;

/**
 * ViewModel for managing event data.
 * Provides access to all events and supports creating new events.
 */
public class EventViewModel extends ViewModel {
    private EventRepository repository;
    private LiveData<List<Event>> events;

    /**
     * Initializes the EventViewModel and loads events from the repository.
     */
    public EventViewModel() {
        repository = RepositoryProvider.getEventRepository();
        events = repository.getAllEvents();
    }

    /**
     * Returns all available events.
     *
     * @return LiveData list of {@link Event} objects.
     */
    public LiveData<List<Event>> getEvents() {return events;}

    /**
     * Creates and adds a new event.
     */
    public void createEvent(Event event) {
        repository.addEvent(event, e -> {});
    }
}
