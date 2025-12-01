package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.adapters.CategoryEventAdapter;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.models.Event;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Activity that displays a list of events filtered by category.
 * Provides sorting and filtering via chips: Newest, Distance, Open Only, Popular.
 */
public class CategoryEventListActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category";
    private static final String TAG = "CategoryEventListActivity";

    private EventRepository eventRepository;
    private RecyclerView recyclerView;
    private CategoryEventAdapter adapter;
    private ChipGroup sortFilterChips;
    private Chip chipRecency, chipLocation;

    private LinearLayout emptyState;
    private ProgressBar loadingIndicator;
    private TextView categoryTitle;

    private String category;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();

    // Current filter state
    private String currentSort = "recency"; // recency, distance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_event_list);

        // Repository provider
        eventRepository = com.example.lotterysystemproject.firebasemanager.RepositoryProvider.getEventRepository();

        initializeViews();
        setupBackButton();
        setupChipListeners();

        category = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (category == null) category = "";

        setCategoryHeader();
        loadCategoryEvents();
    }

    /**
     * Initialize all UI views from the layout.
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_category_events);
        sortFilterChips = findViewById(R.id.sort_filter_chips);
        chipRecency = findViewById(R.id.chip_recency);
        chipLocation = findViewById(R.id.chip_location);

        emptyState = findViewById(R.id.empty_state);
        loadingIndicator = findViewById(R.id.loading_indicator);
        categoryTitle = findViewById(R.id.category_title);

        adapter = new CategoryEventAdapter(new ArrayList<>(), this::onEventCardClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Set up category header with title and icon based on category type.
     */
    private void setCategoryHeader() {
        if (categoryTitle != null) {
            categoryTitle.setText(category + " Events");
        }
    }

    /**
     * Setup back button click listener.
     */
    private void setupBackButton() {
        Button backButton = findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
    }

    /**
     * Setup listeners for all filter chips.
     */
    private void setupChipListeners() {
        chipRecency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentSort = "recency";
                applyFiltersAndSort();
            }
        });

        chipLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentSort = "distance";
                applyFiltersAndSort();
            }
        });
    }

    /**
     * Load events filtered by category from Firebase.
     */
    private void loadCategoryEvents() {
        showLoading(true);

        eventRepository.getEventsByCategory(category,
                (Consumer<List<Event>>) events -> runOnUiThread(() -> {
                    showLoading(false);

                    if (events == null || events.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    allEvents.clear();
                    allEvents.addAll(events);
                    applyFiltersAndSort();
                }),
                (Consumer<Exception>) e -> runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showEmptyState();
                })
        );
    }

    /**
     * Apply current filters and sort to the event list.
     */
    private void applyFiltersAndSort() {
        filteredEvents.clear();
        filteredEvents.addAll(allEvents);

        // Apply sorting
        switch (currentSort) {
            case "recency":
                sortByRecency();
                break;
            case "distance":
                sortByDistance();
                break;
        }

        // Update UI
        if (filteredEvents.isEmpty()) {
            showEmptyState();
        } else {
            showEventList();
            adapter.update(filteredEvents);
        }
    }

    /**
     * Sort by recency -> newest events first.
     */
    private void sortByRecency() {
        Collections.sort(filteredEvents, (a, b) -> {
            if (a.getEventDate() == null && b.getEventDate() == null) return 0;
            if (a.getEventDate() == null) return 1;
            if (b.getEventDate() == null) return -1;
            return b.getEventDate().compareTo(a.getEventDate());
        });
    }

    /**
     * Sort by distance -> closest events first (requires geolocation).
     * Currently sorts alphabetically by location as placeholder.
     */
    private void sortByDistance() {
        Collections.sort(filteredEvents, Comparator.comparing(e -> {
            String loc = e.getLocation();
            return loc == null ? "" : loc;
        }));
    }

    /**
     * Handle event card click.
     */
    private void onEventCardClicked(Event event) {
        if (event == null || event.getId() == null) return;
        startActivity(new android.content.Intent(this, EventDetailsActivity.class)
                .putExtra("eventId", event.getId()));
    }

    /**
     * Show loading indicator and hide other content.
     */
    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    /**
     * Show event list and hide empty state.
     */
    private void showEventList() {
        loadingIndicator.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    /**
     * Show empty state and hide event list.
     */
    private void showEmptyState() {
        loadingIndicator.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
}