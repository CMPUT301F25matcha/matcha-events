package com.example.lotterysystemproject.Views.fragments.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.adapters.EventAdapter;
import com.example.lotterysystemproject.Models.EventAdmin;
import com.example.lotterysystemproject.viewmodels.EventViewModel;
import java.util.ArrayList;

public class OrganizerDashboardFragment extends Fragment {

    private EventViewModel eventViewModel;
    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    private Button createEventButton;
    private TextView activeCount, upcomingCount, pastCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_dashboard, container, false);

        // Initialize ViewModel
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Initialize UI
        createEventButton = view.findViewById(R.id.create_event_button);
        activeCount = view.findViewById(R.id.active_count);
        upcomingCount = view.findViewById(R.id.upcoming_count);
        pastCount = view.findViewById(R.id.past_count);
        eventRecyclerView = view.findViewById(R.id.event_recycler_view);

        // Setup RecyclerView
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(new ArrayList<>(), this::onEventClick);
        eventRecyclerView.setAdapter(eventAdapter);

        // Observe events
        eventViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            eventAdapter.updateEvents(events);
            updateStats(events);
        });

        // Create event button
        createEventButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_dashboard_to_createEvent);
        });

        return view;
    }

    private void onEventClick(EventAdmin event) {
        Bundle args = new Bundle();
        args.putString("eventId", event.getId());

        // Use getView() to get the fragment's root view
        Navigation.findNavController(requireView()).navigate(
                R.id.action_dashboard_to_eventManagement,
                args
        );
    }

    private void updateStats(java.util.List<EventAdmin> events) {
        if (events == null || events.isEmpty()) {
            activeCount.setText("0");
            upcomingCount.setText("0");
            pastCount.setText("0");
            return;
        }

        int active = 0;
        int upcoming = 0;
        int past = 0;

        long now = System.currentTimeMillis();
        long oneDayFromNow = now + (24 * 60 * 60 * 1000); // 1 day in milliseconds

        for (EventAdmin event : events) {
            long eventTime = event.getEventDate().getTime();

            if (eventTime < now) {
                // Event has already happened
                past++;
            } else if (eventTime <= oneDayFromNow) {
                // Event is within next 24 hours
                active++;
            } else {
                // Event is more than 24 hours away
                upcoming++;
            }
        }

        activeCount.setText(String.valueOf(active));
        upcomingCount.setText(String.valueOf(upcoming));
        pastCount.setText(String.valueOf(past));
    }
}