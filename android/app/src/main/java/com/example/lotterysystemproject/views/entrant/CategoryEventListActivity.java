package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.adapters.CategoryEventAdapter;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.models.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Activity that displays a list of events filtered by category.
 * Provides sorting controls: Recency and Location (placeholder).
 */
public class CategoryEventListActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category";

    private EventRepository eventRepository;
    private RecyclerView recyclerView;
    private CategoryEventAdapter adapter;
    private Button btnSortRecency;
    private Button btnSortLocation;

    private String category;
    private List<Event> currentEvents = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_event_list);

        // Repository provider - adapt to your DI/provider
        eventRepository = com.example.lotterysystemproject.firebasemanager.RepositoryProvider.getEventRepository();

        recyclerView = findViewById(R.id.recycler_category_events);
        btnSortRecency = findViewById(R.id.btn_sort_recency);
        btnSortLocation = findViewById(R.id.btn_sort_location);

        adapter = new CategoryEventAdapter(new ArrayList<>(), this::onEventCardClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        category = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (category == null) category = "";

        setTitle(category + " Events");

        btnSortRecency.setOnClickListener(v -> sortByRecency());
        btnSortLocation.setOnClickListener(v -> sortByLocationPlaceholder());

        loadCategoryEvents();
    }

    private void loadCategoryEvents() {
        eventRepository.getEventsByCategory(category,
                (Consumer<List<Event>>) events -> runOnUiThread(() -> {
                    if (events == null) {
                        Toast.makeText(this, "No events found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentEvents.clear();
                    currentEvents.addAll(events);
                    adapter.update(events);
                }),
                (Consumer<Exception>) e -> runOnUiThread(() ->
                        Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show())
        );
    }

    private void onEventCardClicked(Event event) {
        if (event == null || event.getId() == null) return;
        // reuse your existing details flow
        startActivity(new android.content.Intent(this, EventDetailsActivity.class)
                .putExtra("eventId", event.getId()));
    }

    // Sort by recency -> the most recent eventDate first (newest upcoming first)
    private void sortByRecency() {
        Collections.sort(currentEvents, (a, b) -> {
            if (a.getEventDate() == null && b.getEventDate() == null) return 0;
            if (a.getEventDate() == null) return 1;
            if (b.getEventDate() == null) return -1;
            return b.getEventDate().compareTo(a.getEventDate());
        });
        adapter.update(currentEvents);
    }

    // Location placeholder: currently fallback sort by name (alphabetical).
    // Replace with real geolocation sorting when you add lat/lng.
    private void sortByLocationPlaceholder() {
        Collections.sort(currentEvents, Comparator.comparing(e -> {
            String loc = e.getLocation();
            return loc == null ? "" : loc;
        }));
        adapter.update(currentEvents);
    }
}

