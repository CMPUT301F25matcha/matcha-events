package com.example.lotterysystemproject.Controllers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterysystemproject.Models.Event;
import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.databinding.AdminBrowseEventsBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseEvents extends Fragment {

    private AdminBrowseEventsBinding binding;

    private EventsAdapter adapter;

    private final List<Event> eventList = new ArrayList<>();

    private FirebaseManager firebaseManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AdminBrowseEventsBinding.inflate(inflater, container, false);
        firebaseManager = FirebaseManager.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize  Adapter
        adapter = new EventsAdapter(getContext(), eventList);

        binding.recyclerEvents.setAdapter(adapter);

        // Search bar


        // Handle back arrow
        binding.backArrow.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminBrowseEvents.this).navigateUp()
        );

        // Fetch events
        firebaseManager.getAllEvents(events -> {
            eventList.clear();
            eventList.addAll(events);
            adapter.notifyDataSetChanged();
        }, e -> {
            e.printStackTrace();
        });
    }




}




