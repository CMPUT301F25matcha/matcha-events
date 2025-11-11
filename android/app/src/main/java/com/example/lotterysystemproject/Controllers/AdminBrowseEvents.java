package com.example.lotterysystemproject.Controllers;

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

import com.example.lotterysystemproject.FirebaseManager.AdminRepository;
import com.example.lotterysystemproject.FirebaseManager.RepositoryProvider;
import com.example.lotterysystemproject.Models.EventAdmin;
import com.example.lotterysystemproject.databinding.AdminBrowseEventsBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminBrowseEvents extends Fragment {
    private AdminBrowseEventsBinding binding;
    private AdminEventsAdapter adapter;
    private final List<EventAdmin> eventAdminList = new ArrayList<>();
    private final List<EventAdmin> allEventAdmins = new ArrayList<>();
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


       // addSampleEventsToFirebase();
        // Fetch events
        fetchEvents();


    }

    private void fetchEvents() {
        adminRepository.listenToAllEvents(events -> {
            allEventAdmins.clear();
            allEventAdmins.addAll(events);

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
        EventAdmin e1 = new EventAdmin("Charity Marathon", new Date(), "10:00 AM", "Downtown Park", 50);
        e1.setId(adminRepository.getDatabase().collection("events").document().getId());
        e1.setDescription("A community marathon to raise funds for hospitals.");

        EventAdmin e2 = new EventAdmin("Winter Festival", new Date(), "5:00 PM", "City Hall", 100);
        e2.setId(adminRepository.getDatabase().collection("events").document().getId());
        e2.setDescription("Enjoy local food, music, and winter festivities.");

        EventAdmin e3 = new EventAdmin("Tech Conference", new Date(), "9:00 AM", "UofA Campus", 200);
        e3.setId(adminRepository.getDatabase().collection("events").document().getId());
        e3.setDescription("A day of talks, networking, and innovation.");

        adminRepository.addEvent(e1, null);
        adminRepository.addEvent(e2, null);
        adminRepository.addEvent(e3, null);
    }

    private void filterEvents(String query) {
        eventAdminList.clear();
        if (query.isEmpty()) {
            eventAdminList.addAll(allEventAdmins);
        } else {
            for (EventAdmin eventAdmin : allEventAdmins) {
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




