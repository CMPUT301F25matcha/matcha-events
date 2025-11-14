package com.example.lotterysystemproject.views.fragments.organizer;

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
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.viewmodels.EventViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment representing the organizer's main dashboard.
 * <p>
 * Displays a list of events managed by the organizer along with event statistics
 * such as the number of active, upcoming, and past events.
 * Provides navigation options to create new events or view event details.
 * </p>
 */
public class OrganizerDashboardFragment extends Fragment {

    /** ViewModel used to manage and observe event data. */
    private EventViewModel eventViewModel;

    /** RecyclerView that displays the list of events. */
    private RecyclerView eventRecyclerView;

    /** Adapter responsible for binding event data to RecyclerView items. */
    private EventAdapter eventAdapter;

    /** Button that navigates to the event creation screen. */
    private Button createEventButton;

    /** TextViews displaying event counts for different categories. */
    private TextView activeCount, upcomingCount, pastCount;

    /**
     * Inflates the fragment layout, initializes the ViewModel and UI components,
     * and sets up event observation and navigation.
     *
     * @param inflater LayoutInflater to inflate the layout
     * @param container Parent container view
     * @param savedInstanceState Previously saved instance state, if available
     * @return The inflated view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_dashboard, container, false);

        // Initialize ViewModel
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Initialize UI components
        createEventButton = view.findViewById(R.id.create_event_button);
        activeCount = view.findViewById(R.id.active_count);
        upcomingCount = view.findViewById(R.id.upcoming_count);
        pastCount = view.findViewById(R.id.past_count);
        eventRecyclerView = view.findViewById(R.id.event_recycler_view);

        // Configure RecyclerView
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(new ArrayList<>(), this::onEventClick);
        eventRecyclerView.setAdapter(eventAdapter);

        // Observe event list changes
        eventViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            eventAdapter.updateEvents(events);
            updateStats(events);
        });

        // Navigate to event creation screen
        createEventButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_createEvent)
        );

        return view;
    }

    /**
     * Handles event selection from the RecyclerView.
     * Navigates to the Event Management screen for the selected event.
     *
     * @param event The selected {@link Event} instance
     */
    private void onEventClick(Event event) {
        Bundle args = new Bundle();
        args.putString("eventId", event.getId());

        Navigation.findNavController(requireView()).navigate(
                R.id.action_dashboard_to_eventManagement,
                args
        );
    }

    /**
     * Updates the active, upcoming, and past event counts displayed on the dashboard.
     *
     * @param events The list of all events managed by the organizer
     */
    private void updateStats(List<Event> events) {
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
        long oneDayFromNow = now + (24 * 60 * 60 * 1000); // 24 hours in milliseconds

        for (Event event : events) {
            long eventTime = event.getEventDate().getTime();

            if (eventTime < now) {
                past++;
            } else if (eventTime <= oneDayFromNow) {
                active++;
            } else {
                upcoming++;
            }
        }

        activeCount.setText(String.valueOf(active));
        upcomingCount.setText(String.valueOf(upcoming));
        pastCount.setText(String.valueOf(past));
    }
}
