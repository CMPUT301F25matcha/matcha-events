package com.example.lotterysystemproject.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.lotterysystemproject.Models.EventAdmin;
import com.example.lotterysystemproject.repositories.EventRepository;
import java.util.List;

public class EventViewModel extends ViewModel {
    private EventRepository repository;
    private LiveData<List<EventAdmin>> events;

    public EventViewModel() {
        repository = new EventRepository();
        events = repository.getEvents();
    }

    public LiveData<List<EventAdmin>> getEvents() {
        return events;
    }

    // Add new event (US 02.01.01)
    public void createEvent(EventAdmin event) {
        repository.addEvent(event);
    }
}