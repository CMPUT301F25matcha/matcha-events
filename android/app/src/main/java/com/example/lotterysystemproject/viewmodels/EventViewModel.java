package com.example.lotterysystemproject.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.repositories.EventRepository;
import java.util.List;

public class EventViewModel extends ViewModel {
    private EventRepository repository;
    private LiveData<List<Event>> events;

    public EventViewModel() {
        repository = new EventRepository();
        events = repository.getEvents();
    }

    public LiveData<List<Event>> getEvents() {
        return events;
    }

    // Add new event (US 02.01.01)
    public void createEvent(Event event) {
        repository.addEvent(event);
    }
}