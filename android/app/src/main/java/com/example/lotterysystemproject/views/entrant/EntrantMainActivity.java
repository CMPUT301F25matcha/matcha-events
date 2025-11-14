package com.example.lotterysystemproject.views.entrant;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterysystemproject.helpers.EventListHelper;
import com.example.lotterysystemproject.utils.NavWiring;
import com.example.lotterysystemproject.databinding.EventViewsBinding;
import android.view.View;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.lotterysystemproject.R;

/**
 * Serves as the main home screen for entrants in the application.
 * Displays featured and available community events that users can browse, search for,
 * and select to view details or register.
 */
public class EntrantMainActivity extends AppCompatActivity {
    private EventViewsBinding binding;
    private EventListHelper eventListHelper;


    /**
     * Initializes the view binding, sets up the search bar, featured events, dynamic event list,
     * and bottom navigation bar.
     * @param savedInstanceState Saved instance state if the activity is being reinitialized
     *
     */
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
            Log.d("EntrantMainActivity", "Featured card clicked");
            Object tag = binding.featuredEventsCard.getTag();
            Log.d("EntrantMainActivity", "Featured card tag: " + tag);
            String eventId = (tag instanceof String) ? (String) tag : null;
            launchEventDetails(eventId);
        });

        // Setup events list with callback to set up listeners
        eventListHelper = new EventListHelper(this, binding.eventsListContainer, this, this::setupEventCardListeners);
        eventListHelper.loadEvents();

        NavWiring.wire(
                this,
                EntrantMainActivity.class,   // home
                null,                        // explore (not yet)
                null,                        // QR (not yet)
                NotificationsActivity.class, // notifications
                ProfileHostActivity.class // profile
        );

        // Select the HOME tab for THIS screen (null-safe)
        LinearLayout home  = findViewById(R.id.nav_home);
        LinearLayout expl  = findViewById(R.id.nav_explore);
        LinearLayout qr    = findViewById(R.id.nav_qr_scanner);
        LinearLayout notif = findViewById(R.id.nav_notifications);
        LinearLayout prof  = findViewById(R.id.nav_profile);

        if (home != null && expl != null && qr != null && notif != null && prof != null) {
            com.example.lotterysystemproject.utils.BottomNavigationHelper.setSelectedItem(
                    com.example.lotterysystemproject.utils.BottomNavigationHelper.NavItem.HOME,
                    home, expl, qr, notif, prof
            );
        }


    }

    /**
     * Sets up click listeners for event cards in the events list.
     * Called after EventListHelper finishes populating the container.
     */
    private void setupEventCardListeners() {
        Log.d("EntrantMainActivity", "Setting up event card listeners");
        for (int i = 0; i < binding.eventsListContainer.getChildCount(); i++) {
            View card = binding.eventsListContainer.getChildAt(i);
            int finalI = i;
            card.setOnClickListener(v -> {
                Log.d("EntrantMainActivity", "Event card clicked at index " + finalI);
                Object tag = v.getTag();
                Log.d("EntrantMainActivity", "Card tag: " + tag);
                String eventId = (tag instanceof String) ? (String) tag : null;
                launchEventDetails(eventId);
            });
        }
    }


    /**
     * Utility method to start details activity.
     * Launches the event details activity for a selected event
     * @param eventId unique identifier of the selected event
     */
    private void launchEventDetails(String eventId) {
        Log.d("EntrantMainActivity", "launchEventDetails called with ID: " + eventId);
        if (eventId == null || eventId.isEmpty()) {
            Log.w("EntrantMainActivity", "Event ID is null or empty, cannot launch details");
            return;
        }
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    /**
     * Refreshes the event list when the activity resumes.
     * Ensures event data stays up-to-date whenever entrant returns to the home screen
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events list when returning to this activity
        if (eventListHelper != null) {
            eventListHelper.loadEvents();
        }
    }
}