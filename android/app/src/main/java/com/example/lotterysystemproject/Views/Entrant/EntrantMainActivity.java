package com.example.lotterysystemproject.Views.Entrant;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterysystemproject.Helpers.EventListHelper;
import com.example.lotterysystemproject.databinding.EventViewsBinding;
import android.view.View;

public class EntrantMainActivity extends AppCompatActivity {
    private EventViewsBinding binding;
    private EventListHelper eventListHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = EventViewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup search view
        SearchView searchView = binding.eventSearchBar;
        if (searchView != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ComponentName componentName = new ComponentName(this, SearchableActivity.class);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
            searchView.setIconifiedByDefault(false);
        }

        // Launch details on featured_events_card click
        binding.featuredEventsCard.setOnClickListener(v -> {
            Object tag = binding.featuredEventsCard.getTag();
            // If tag is set as Event, extract event id
            String eventId = (tag instanceof String) ? (String) tag : null;
            launchEventDetails(eventId);
        });

        // Launch details on recent_events_container children
        for (int i = 0; i < binding.recentEventsContainer.getChildCount(); i++) {
            View child = binding.recentEventsContainer.getChildAt(i);
            child.setOnClickListener(v -> {
                Object tag = v.getTag();
                String eventId = (tag instanceof String) ? (String) tag : null;
                launchEventDetails(eventId);
            });
        }

        // Setup events list
        eventListHelper = new EventListHelper(this, binding.eventsListContainer, this::launchEventDetails);
        eventListHelper.loadEvents();
    }

    // Utility method to start details activity
    private void launchEventDetails(String eventId) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        if (eventId != null) intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events list when returning to this activity
        if (eventListHelper != null) {
            eventListHelper.loadEvents();
        }
    }
}
