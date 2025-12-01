package com.example.lotterysystemproject.views.entrant;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lotterysystemproject.adapters.RecentEventsAdapter;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.helpers.EventListHelper;
import com.example.lotterysystemproject.utils.FeaturedEventsManager;
import com.example.lotterysystemproject.utils.NavWiring;
import com.example.lotterysystemproject.databinding.EventViewsBinding;

import android.view.View;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.lotterysystemproject.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.utils.RecentEventsManager;

/**
 * Serves as the main home screen for entrants in the application.
 * Displays featured and available community events that users can browse, search for,
 * and select to view details or register.
 *
 * Also provides category shortcuts (Music, Sports, Art, etc.)
 * that open a filtered list of events belonging to that category.
 */
public class EntrantMainActivity extends AppCompatActivity {

    private static final String TAG = "EntrantMainActivity";
    private EventViewsBinding binding;
    private EventListHelper eventListHelper;
    private EventRepository eventRepository;
    private FeaturedEventsManager featuredEventsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EventViewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init repository (ensure RepositoryProvider is implemented)
        eventRepository = RepositoryProvider.getEventRepository();

        // initialize helpers/managers
        featuredEventsManager = new FeaturedEventsManager(this);

        setupSearchBar();
        setupCategoryListeners();
        setupFeaturedCard(); // assumes you have featured logic as earlier

        // Setup events list (big list) as before
        eventListHelper = new EventListHelper(this, binding.eventsListContainer, this, this::setupEventCardListeners);
        eventListHelper.loadEvents();

        setupBottomNavigation();

        // highlight bottom nav (existing code)
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

        // Load featured events
        loadFeaturedEvents(); // uses FeaturedEventsManager if present
    }

    // -------------------------------------------------------------------------
    // Search Bar
    // -------------------------------------------------------------------------

    /**
     * Configures Android's SearchManager to delegate search queries to SearchableActivity.
     */
    private void setupSearchBar() {
        SearchView searchView = binding.eventSearchBar;
        if (searchView != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ComponentName componentName = new ComponentName(this, SearchableActivity.class);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
            searchView.setIconifiedByDefault(false);
        }
    }

    // -------------------------------------------------------------------------
    // Category Listeners
    // -------------------------------------------------------------------------

    /**
     * Sets click listeners for each category card in the UI.
     * When tapped, launches CategoryEventListActivity showing events from that category.
     * Top 5 categories: Sports & Recreation, Arts & Culture, Education & Learning,
     * Health & Wellness, Technology
     */
    private void setupCategoryListeners() {
        bindCategoryToLauncher(R.id.category_sports,          "Sports & Recreation");
        bindCategoryToLauncher(R.id.category_art,             "Arts & Culture");
        bindCategoryToLauncher(R.id.category_education,       "Education & Learning");
        bindCategoryToLauncher(R.id.category_health,          "Health & Wellness");
        bindCategoryToLauncher(R.id.category_tech,            "Technology");
    }

    /**
     * Attaches a click listener to a single category card.
     */
    private void bindCategoryToLauncher(int cardId, String category) {
        CardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> launchCategoryScreen(category));
        }
    }

    /**
     * Launches a dedicated screen that displays events for a given category.
     */
    private void launchCategoryScreen(String category) {
        Intent intent = new Intent(this, CategoryEventListActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    // -------------------------------------------------------------------------
    // Featured Event Card
    // -------------------------------------------------------------------------

    /**
     * Sets click listener on the featured card so tapping it opens event details.
     */
    private void setupFeaturedCard() {
        binding.featuredEventsCard.setOnClickListener(v -> {
            Object tag = binding.featuredEventsCard.getTag();
            String eventId = (tag instanceof String) ? (String) tag : null;
            if (eventId != null) launchEventDetails(eventId);
        });
    }

    /**
     * Loads 5 featured events with proper validation against Firebase.
     * Always fetches fresh data to ensure deleted events are removed.
     */
    private void loadFeaturedEvents() {
        Log.d(TAG, "Loading featured events");

        // Always fetch from Firebase to ensure we have valid, non-deleted events
        eventRepository.getActiveEvents(
                events -> {
                    if (events == null || events.isEmpty()) {
                        Log.w(TAG, "No active events found");
                        runOnUiThread(() -> binding.featuredEventsCard.setVisibility(View.GONE));
                        return;
                    }

                    // Shuffle and pick 5
                    Collections.shuffle(events);
                    List<Event> selected = events.size() > 5 ? events.subList(0, 5) : events;

                    Log.d(TAG, "Selected " + selected.size() + " featured events");

                    // Save for quick subsequent loads (optional - consider removing if causing issues)
                    featuredEventsManager.saveFeaturedEvents(selected);

                    // Bind to UI
                    runOnUiThread(() -> {
                        bindFeaturedEvents(selected);
                        binding.featuredEventsCard.setVisibility(View.VISIBLE);
                    });
                },
                error -> {
                    Log.e(TAG, "Failed to load featured events", error);
                    runOnUiThread(() -> {
                        binding.featuredEventsCard.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to load featured events", Toast.LENGTH_SHORT).show();
                    });
                }
        );
    }

    /**
     * Sets up the dot indicators for the ViewPager2.
     * Creates a dot for each featured event page.
     */
    private void setupDotsIndicator(int count) {
        binding.pageIndicator.removeAllViews();

        if (count <= 1) {
            // Don't show dots if only 1 or 0 pages
            binding.pageIndicator.setVisibility(View.GONE);
            return;
        }

        binding.pageIndicator.setVisibility(View.VISIBLE);

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            dot.setBackgroundResource(R.drawable.dot_selector);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20, 20);
            lp.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(lp);
            dot.setSelected(i == 0);

            binding.pageIndicator.addView(dot);
        }

        // Unregister any existing callbacks to prevent duplicates
        binding.featuredEventsViewpager.unregisterOnPageChangeCallback(pageChangeCallback);

        // Register the callback
        binding.featuredEventsViewpager.registerOnPageChangeCallback(pageChangeCallback);
    }

    // Store the callback as a field to allow proper unregistering
    private final ViewPager2.OnPageChangeCallback pageChangeCallback =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    highlightDot(position);
                }
            };

    /**
     * Highlights the dot corresponding to the current page.
     */
    private void highlightDot(int index) {
        for (int i = 0; i < binding.pageIndicator.getChildCount(); i++) {
            View dot = binding.pageIndicator.getChildAt(i);
            dot.setSelected(i == index);
        }
    }

    /**
     * Binds the list of featured events to the ViewPager2.
     */
    private void bindFeaturedEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            Log.w(TAG, "No events to bind");
            binding.featuredEventsCard.setVisibility(View.GONE);
            return;
        }

        Log.d(TAG, "Binding " + events.size() + " events to ViewPager");

        FeaturedEventsAdapter adapter = new FeaturedEventsAdapter(events, e -> {
            launchEventDetails(e.getId());
        });

        binding.featuredEventsViewpager.setAdapter(adapter);

        // Set up dots indicator AFTER adapter is set
        binding.featuredEventsViewpager.post(() -> setupDotsIndicator(events.size()));
    }

    // -------------------------------------------------------------------------
    // Dynamic List Card Click Listeners
    // -------------------------------------------------------------------------

    /**
     * Sets up click listeners for each event card after EventListHelper has
     * dynamically populated them in the container.
     */
    private void setupEventCardListeners() {
        Log.d("EntrantMainActivity", "Setting up event card listeners");
        for (int i = 0; i < binding.eventsListContainer.getChildCount(); i++) {
            View card = binding.eventsListContainer.getChildAt(i);
            int index = i;

            card.setOnClickListener(v -> {
                Log.d("EntrantMainActivity", "Event card clicked at index " + index);
                Object tag = v.getTag();
                String eventId = (tag instanceof String) ? (String) tag : null;
                launchEventDetails(eventId);
            });
        }
    }

    // -------------------------------------------------------------------------
    // Event Detail Navigation
    // -------------------------------------------------------------------------

    /**
     * Launches the EventDetailsActivity for a specific event ID.
     *
     * @param eventId event's unique ID
     */
    private void launchEventDetails(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            Log.w("EntrantMainActivity", "Event ID is null/empty; cannot launch details");
            return;
        }
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    // -------------------------------------------------------------------------
    // Bottom Navigation
    // -------------------------------------------------------------------------

    /**
     * Configures bottom navigation bar and highlights the HOME tab.
     */
    private void setupBottomNavigation() {

        NavWiring.wire(
                this,
                EntrantMainActivity.class,   // home
                null,                        // explore placeholder
                NotificationsActivity.class,
                ProfileHostActivity.class
        );

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

    // -------------------------------------------------------------------------
    // Refresh on Resume
    // -------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        if (eventListHelper != null) {
            eventListHelper.loadEvents();
        }
    }

}
