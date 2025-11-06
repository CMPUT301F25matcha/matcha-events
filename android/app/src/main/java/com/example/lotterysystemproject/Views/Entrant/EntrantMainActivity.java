package com.example.lotterysystemproject.Views.Entrant;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterysystemproject.Helpers.EventListHelper;
import com.example.lotterysystemproject.Utils.NavWiring;
import com.example.lotterysystemproject.databinding.EventViewsBinding;
import android.view.View;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.lotterysystemproject.R;

public class EntrantMainActivity extends AppCompatActivity {
    private EventViewsBinding binding;
    private EventListHelper eventListHelper;
    private LinearLayout navHome, navExplore, navQR, navNotifications, navProfile;


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
        eventListHelper = new EventListHelper(this, binding.eventsListContainer, this::setupEventCardListeners);
        eventListHelper.loadEvents();

        NavWiring.wire(
                this,
                EntrantMainActivity.class,   // home
                null,                        // explore (not yet)
                null,                        // QR (not yet)
                NotificationsActivity.class,       // notifications
                ProfileHostActivity.class // profile
        );

        // Select the HOME tab for THIS screen (null-safe)
        LinearLayout home  = findViewById(R.id.nav_home);
        LinearLayout expl  = findViewById(R.id.nav_explore);
        LinearLayout qr    = findViewById(R.id.nav_qr_scanner);
        LinearLayout notif = findViewById(R.id.nav_notifications);
        LinearLayout prof  = findViewById(R.id.nav_profile);

        if (home != null && expl != null && qr != null && notif != null && prof != null) {
            com.example.lotterysystemproject.Utils.BottomNavigationHelper.setSelectedItem(
                    com.example.lotterysystemproject.Utils.BottomNavigationHelper.NavItem.HOME,
                    home, expl, qr, notif, prof
            );
        }

        /*
        // Initialize the layouts for each nav item
        navHome = findViewById(R.id.nav_home);
        navExplore = findViewById(R.id.nav_explore);
        navQR = findViewById(R.id.nav_qr_scanner);
        navNotifications = findViewById(R.id.nav_notifications);
        navProfile = findViewById(R.id.nav_profile);

        // PROFILE BUTTON HANDLER
        navProfile.setOnClickListener(v -> {
            BottomNavigationHelper.setSelectedItem(
                    BottomNavigationHelper.NavItem.PROFILE,
                    navHome, navExplore, navQR, navNotifications, navProfile
            );

            // Open your profile activity
            Intent intent = new Intent(this, EntrantProfileView.class);
            startActivity(intent);
        });


        // HOME BUTTON HANDLER
        navHome.setOnClickListener(v -> {
            BottomNavigationHelper.setSelectedItem(
                    BottomNavigationHelper.NavItem.HOME,
                    navHome, navExplore, navQR, navNotifications, navProfile
            );

            // Open your home activity
            Intent intent = new Intent(this, EntrantMainActivity.class);
            startActivity(intent);
        });



        // 1) Wire the bar actions for THIS screen
        NavWiring.wire(
                this,
                EntrantMainActivity.class,
                null,
                null,
                null,
                EntrantProfileActivity.class
        );

        // 2) Make HOME look selected in THIS activity
        com.example.lotterysystemproject.Utils.BottomNavigationHelper.setSelectedItem(
                com.example.lotterysystemproject.Utils.BottomNavigationHelper.NavItem.HOME,
                findViewById(R.id.nav_home),
                findViewById(R.id.nav_explore),
                findViewById(R.id.nav_qr_scanner),
                findViewById(R.id.nav_notifications),
                findViewById(R.id.nav_profile)
        );

         */


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

    // Utility method to start details activity
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events list when returning to this activity
        if (eventListHelper != null) {
            eventListHelper.loadEvents();
        }
    }
}