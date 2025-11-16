package com.example.lotterysystemproject.controllers;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterysystemproject.firebasemanager.AdminRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.databinding.AdminBrowseEventsBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminBrowseEvents extends Fragment {
    private AdminBrowseEventsBinding binding;
    private AdminEventsAdapter adapter;
    private final List<Event> eventAdminList = new ArrayList<>();
    private final List<Event> allEvents = new ArrayList<>();
    private AdminRepository adminRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AdminBrowseEventsBinding.inflate(inflater, container, false);
        adminRepository = RepositoryProvider.getAdminRepository();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize  Adapter
        adapter = new AdminEventsAdapter(getContext(), eventAdminList);

        binding.recyclerEvents.setAdapter(adapter);

        // Search bar


        // Handle back arrow
        binding.backArrow.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminBrowseEvents.this).navigateUp()
        );

        // Search Events
        TextInputEditText searchEventInput = binding.searchInput;
        searchEventInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filterEvents(s.toString());

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });


        //addSampleEventsToFirebase();
        // Fetch events
        fetchEvents();


    }

    private void fetchEvents() {
        adminRepository.listenToAllEvents(events -> {
            allEvents.clear();
            allEvents.addAll(events);

            eventAdminList.clear();
            eventAdminList.addAll(events);
            adapter.notifyDataSetChanged();

            // Add sample events only if Firestore is empty
            if (events.isEmpty()) {
                addSampleEventsToFirebase();
            }

        }, e -> e.printStackTrace());
    }


    private void addSampleEventsToFirebase() {
        // Use existing instance
        // Create a few example events
        String eventId1 = adminRepository.getDatabase().collection("events").document().getId();
        Event e1 = new Event(
                eventId1,
                "Charity Marathon",
                "A community marathon to raise funds for hospitals.",
                "Run Club 21",
                "host13",
                new Date(),
                "10:00 AM",
                "Downtown Park",
                100
        );

        String eventId2 = adminRepository.getDatabase().collection("events").document().getId();
        Event e2 = new Event(
                eventId2,
                "Winter Festival",
                "Enjoy local food, music, and winter festivities.",
                "Ice Skies",
                "host14",
                new Date(),
                "5:00 PM",
                "City Hall",
                50
        );

        String eventId3 = adminRepository.getDatabase().collection("events").document().getId();
        Event e3 = new Event(
                eventId3,
                "Tech Conference",
                "A day of talks, networking, and innovation.",
                "Tech Hub",
                "host15",
                new Date(),
                "9:00 AM",
                "Tech Park",
                200
        );

        adminRepository.addEvent(e1, null);
        adminRepository.addEvent(e2, null);
        adminRepository.addEvent(e3, null);
    }

    private void filterEvents(String query) {
        eventAdminList.clear();
        if (query.isEmpty()) {
            eventAdminList.addAll(allEvents);
        } else {
            for (Event eventAdmin : allEvents) {
                if (eventAdmin.getName().toLowerCase().contains(query.toLowerCase()) ||
                eventAdmin.getLocation().toLowerCase().contains(query.toLowerCase())) {
                    eventAdminList.add(eventAdmin);
                }
            }
        }

        adapter.notifyDataSetChanged();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadEvents() {
        adminRepository.getAllEvents(
                events -> {
                    // Update UI with events
                },
                error -> {
                    // Handle error
                }
        );
    }
}




