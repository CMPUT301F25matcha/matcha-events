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

    private EventViewsBinding binding;
    private EventListHelper eventListHelper;
    private EventRepository eventRepository;

    private RecentEventsManager recentEventsManager;
    private RecentEventsAdapter recentAdapter;

    private FeaturedEventsManager featuredEventsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EventViewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init repository (ensure RepositoryProvider is implemented)
        eventRepository = RepositoryProvider.getEventRepository();

        // initialize helpers/managers
        recentEventsManager = new RecentEventsManager(eventRepository);
        featuredEventsManager = new FeaturedEventsManager(this);

        setupSearchBar();
        setupCategoryListeners();
        setupFeaturedCard(); // assumes you have featured logic as earlier
        setupRecentEventsRecycler();

        // Setup events list (big list) as before
        eventListHelper = new EventListHelper(this, binding.eventsListContainer, this, this::setupEventCardListeners);
        eventListHelper.loadEvents();

        NavWiring.wire(
                this,
                EntrantMainActivity.class,
                null,
                null,
                NotificationsActivity.class,
                ProfileHostActivity.class
        );

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

        // Load recent events (top 5, uses RecentEventsManager)
        loadRecentEvents();
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
     */
    private void setupCategoryListeners() {

        // Helper method to bind quickly
        bindCategoryToLauncher(R.id.category_music,      "Music");
        bindCategoryToLauncher(R.id.category_sports,     "Sports");
        bindCategoryToLauncher(R.id.category_art,        "Art");
        bindCategoryToLauncher(R.id.category_food,       "Food");
        bindCategoryToLauncher(R.id.category_tech,       "Tech");
        bindCategoryToLauncher(R.id.category_education,  "Education");
    }

    /**
     * Attaches a click listener to a single category card.
     *
     * @param cardId    view ID of the CardView for the category
     * @param category  category name string
     */
    private void bindCategoryToLauncher(int cardId, String category) {
        CardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> launchCategoryScreen(category));
        }
    }

    /**
     * Launches a dedicated screen that displays events for a given category.
     * Uses repository only inside that screen for clean separation.
     *
     * @param category the selected category
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
     * Loads 5 featured events.
     * If the device has not updated today, it fetches from repository, randomly selects 5,
     * saves into SharedPreferences, and binds them.
     * If already updated today, it loads from SharedPreferences.
     */
    private void loadFeaturedEvents() {
    // TODO: eaturedEventsManager.loadFeaturedEvents signature above is assumed
    //  similar to RecentEventsManager with callbacks.
    //  If your manager's API differs, adjust accordingly.
        if (!featuredEventsManager.shouldUpdate()) {
            // load from cache
            List<Event> cached = featuredEventsManager.loadFeaturedEvents();
            if (!cached.isEmpty()) {
                bindFeaturedEvents(cached);
                return;
            }
        }

        // Otherwise fetch active events from repository
        eventRepository.getActiveEvents(
                events -> {
                    if (events == null || events.isEmpty()) return;

                    // Shuffle and pick 5
                    Collections.shuffle(events);
                    List<Event> selected =
                            events.size() > 5 ? events.subList(0, 5) : events;

                    // Save for the day
                    featuredEventsManager.saveFeaturedEvents(selected);

                    // Bind to UI
                    runOnUiThread(() -> bindFeaturedEvents(selected));
                },
                error -> Log.e("EntrantMainActivity", "unable to load featured events", error)
        );
    }

    private void setupDotsIndicator(int count) {
        binding.pageIndicator.removeAllViews();

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            dot.setBackgroundResource(R.drawable.dot_selector);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20, 20);
            lp.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(lp);
            dot.setSelected(i == 0);

            binding.pageIndicator.addView(dot);
        }

        binding.featuredEventsViewpager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        highlightDot(position);
                    }
                }
        );
    }

    private void highlightDot(int index) {
        for (int i = 0; i < binding.pageIndicator.getChildCount(); i++) {
            View dot = binding.pageIndicator.getChildAt(i);
            dot.setSelected(i == index);
        }
    }

    private void bindFeaturedEvents(List<Event> events) {
        FeaturedEventsAdapter adapter = new FeaturedEventsAdapter(events, e -> {
            launchEventDetails(e.getId());
        });
        binding.featuredEventsViewpager.setAdapter(adapter);
        setupDotsIndicator(events.size());
    }

    //
    // Setup Recent Event Cards
    //

    private void setupRecentEventsRecycler() {
        RecyclerView recycler = findViewById(R.id.recent_events_recycler);
        if (recycler == null) {
            Log.w("EntrantMainActivity", "recent_events_recycler not found in layout");
            return;
        }

        recycler.setHasFixedSize(true);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler.setLayoutManager(layout);

        recentAdapter = new RecentEventsAdapter(this, new ArrayList<>(), event -> {
            if (event != null && event.getId() != null) {
                launchEventDetails(event.getId());
            }
        });

        recycler.setAdapter(recentAdapter);
    }
    private void loadRecentEvents() {
        // load top 5
        recentEventsManager.loadRecentEvents(5, events -> runOnUiThread(() -> {
            if (events == null || events.isEmpty()) {
                // optionally hide the recycler
                View recycler = findViewById(R.id.recent_events_recycler);
                if (recycler != null) recycler.setVisibility(View.GONE);
                return;
            }
            recentAdapter.update(events);
            View recycler = findViewById(R.id.recent_events_recycler);
            if (recycler != null) recycler.setVisibility(View.VISIBLE);
        }), e -> runOnUiThread(() -> {
            Log.e("EntrantMainActivity", "Failed to load recent events", e);
            Toast.makeText(this, "Failed to load recent events", Toast.LENGTH_SHORT).show();
        }));
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
                null,                        // QR placeholder
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
