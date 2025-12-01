package com.example.lotterysystemproject.views.entrant;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity that displays search results for events based on keyword search,
 * date filtering, and time filtering.
 * Implements US 01.01.04: Filter events based on interests and availability.
 */
public class SearchResultsActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "EXTRA_QUERY";
    private static final String TAG = "SearchResultsActivity";

    // UI Components
    private ImageButton backButton;
    private TextView queryTextView;
    private TextView resultsCountTextView;
    private LinearLayout resultsContainer;
    private ProgressBar loadingIndicator;
    private TextView noResultsTextView;
    private Button filterByDateButton;
    private Button filterByTimeButton;
    private Button clearFiltersButton;

    // Data
    private EventRepository eventRepository;
    private String searchQuery;
    private List<Event> allEvents;
    private List<Event> filteredEvents;

    // Filter state
    private Date filterDate = null;
    private String filterTime = null; // Format: "HH:mm"
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        initializeViews();
        setupFilterButtons();

        eventRepository = RepositoryProvider.getEventRepository();
        searchQuery = getIntent().getStringExtra(EXTRA_QUERY);

        if (searchQuery != null && !searchQuery.isEmpty()) {
            queryTextView.setText("Search: " + searchQuery);
            performSearch();
        } else {
            queryTextView.setText("Search");
            showNoResults("Please enter a search query");
        }
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        queryTextView = findViewById(R.id.search_query_text);
        resultsCountTextView = findViewById(R.id.results_count_text);
        resultsContainer = findViewById(R.id.search_results_container);
        loadingIndicator = findViewById(R.id.search_loading_indicator);
        noResultsTextView = findViewById(R.id.no_results_text);
        filterByDateButton = findViewById(R.id.filter_by_date_button);
        filterByTimeButton = findViewById(R.id.filter_by_time_button);
        clearFiltersButton = findViewById(R.id.clear_filters_button);

        // Setup back button click listener
        backButton.setOnClickListener(v -> navigateBack());
    }

    /**
     * Setup click listeners for filter buttons
     */
    private void setupFilterButtons() {
        filterByDateButton.setOnClickListener(v -> showDatePicker());
        filterByTimeButton.setOnClickListener(v -> showTimePicker());
        clearFiltersButton.setOnClickListener(v -> clearFilters());

        // Initially hide clear filters button
        clearFiltersButton.setVisibility(View.GONE);
    }

    /**
     * Perform the initial search by fetching all active events and filtering by keywords
     */
    private void performSearch() {
        showLoading(true);

        eventRepository.getActiveEvents(
                events -> {
                    allEvents = events;
                    if (events == null || events.isEmpty()) {
                        runOnUiThread(() -> showNoResults("No events found"));
                        return;
                    }

                    // Filter by search query keywords
                    filteredEvents = filterByKeywords(events, searchQuery);

                    runOnUiThread(() -> {
                        showLoading(false);
                        if (filteredEvents.isEmpty()) {
                            showNoResults("No events match your search");
                        } else {
                            displayResults(filteredEvents);
                        }
                    });
                },
                error -> {
                    Log.e(TAG, "Error loading events", error);
                    runOnUiThread(() -> {
                        showLoading(false);
                        showNoResults("Error loading events. Please try again.");
                        Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                    });
                }
        );
    }

    /**
     * Filter events by keywords from search query.
     * Splits query into words and checks if event name or description contains any keyword.
     *
     * @param events List of events to filter
     * @param query Search query string
     * @return Filtered list of events
     */
    private List<Event> filterByKeywords(List<Event> events, String query) {
        List<Event> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return events;
        }

        // Split query into keywords (words separated by spaces)
        String[] keywords = query.toLowerCase().trim().split("\\s+");

        for (Event event : events) {
            if (matchesKeywords(event, keywords)) {
                results.add(event);
            }
        }

        return results;
    }

    /**
     * Check if an event matches any of the keywords in its name or description
     *
     * @param event Event to check
     * @param keywords Array of keywords to match
     * @return true if event matches any keyword
     */
    private boolean matchesKeywords(Event event, String[] keywords) {
        String name = event.getName() != null ? event.getName().toLowerCase() : "";
        String description = event.getDescription() != null ? event.getDescription().toLowerCase() : "";

        for (String keyword : keywords) {
            if (name.contains(keyword) || description.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Apply date and time filters to the current filtered results
     */
    private void applyFilters() {
        if (allEvents == null) return;

        // Start with keyword-filtered results
        filteredEvents = filterByKeywords(allEvents, searchQuery);

        // Apply date filter
        if (filterDate != null) {
            filteredEvents = filterByDate(filteredEvents, filterDate);
        }

        // Apply time filter
        if (filterTime != null) {
            filteredEvents = filterByTime(filteredEvents, filterTime);
        }

        // Update UI
        if (filteredEvents.isEmpty()) {
            showNoResults("No events match your filters");
        } else {
            displayResults(filteredEvents);
        }

        // Show/hide clear filters button
        clearFiltersButton.setVisibility(
                (filterDate != null || filterTime != null) ? View.VISIBLE : View.GONE
        );

        updateFilterButtonText();
    }

    /**
     * Filter events by date - events occurring on the specified date
     *
     * @param events List of events to filter
     * @param targetDate Date to filter by
     * @return Filtered list of events
     */
    private List<Event> filterByDate(List<Event> events, Date targetDate) {
        List<Event> results = new ArrayList<>();
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(targetDate);

        Calendar eventCal = Calendar.getInstance();

        for (Event event : events) {
            if (event.getEventDate() != null) {
                eventCal.setTime(event.getEventDate());

                // Check if year, month, and day match
                if (targetCal.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                        targetCal.get(Calendar.MONTH) == eventCal.get(Calendar.MONTH) &&
                        targetCal.get(Calendar.DAY_OF_MONTH) == eventCal.get(Calendar.DAY_OF_MONTH)) {
                    results.add(event);
                }
            }
        }

        return results;
    }

    /**
     * Filter events by time - events occurring at or after the specified time
     *
     * @param events List of events to filter
     * @param targetTime Time string in "HH:mm" format
     * @return Filtered list of events
     */
    private List<Event> filterByTime(List<Event> events, String targetTime) {
        List<Event> results = new ArrayList<>();

        for (Event event : events) {
            String eventTime = event.getEventTime();
            if (eventTime != null && !eventTime.isEmpty()) {
                // Simple string comparison works for "HH:mm" format
                if (eventTime.compareTo(targetTime) >= 0) {
                    results.add(event);
                }
            }
        }

        return results;
    }

    /**
     * Show date picker dialog for filtering
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (filterDate != null) {
            calendar.setTime(filterDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth);
                    filterDate = selectedCal.getTime();
                    applyFilters();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    /**
     * Show time picker dialog for filtering
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = 12;
        int minute = 0;

        if (filterTime != null) {
            try {
                String[] parts = filterTime.split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing filter time", e);
            }
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    filterTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    applyFilters();
                },
                hour,
                minute,
                true // 24-hour format
        );

        timePickerDialog.show();
    }

    /**
     * Clear all active filters
     */
    private void clearFilters() {
        filterDate = null;
        filterTime = null;
        applyFilters();
    }

    /**
     * Update filter button text to show active filters
     */
    private void updateFilterButtonText() {
        if (filterDate != null) {
            filterByDateButton.setText("Date: " + dateFormat.format(filterDate));
        } else {
            filterByDateButton.setText("Filter by Date");
        }

        if (filterTime != null) {
            filterByTimeButton.setText("Time: " + filterTime);
        } else {
            filterByTimeButton.setText("Filter by Time");
        }
    }

    /**
     * Display search results in the UI
     *
     * @param events List of events to display
     */
    private void displayResults(List<Event> events) {
        resultsContainer.removeAllViews();
        resultsContainer.setVisibility(View.VISIBLE);
        noResultsTextView.setVisibility(View.GONE);

        resultsCountTextView.setText(events.size() + " event(s) found");
        resultsCountTextView.setVisibility(View.VISIBLE);

        for (Event event : events) {
            View eventCard = createEventCard(event);
            resultsContainer.addView(eventCard);
        }
    }

    /**
     * Create a card view for an event
     *
     * @param event Event to create card for
     * @return View representing the event card
     */
    private View createEventCard(Event event) {
        View cardView = getLayoutInflater().inflate(R.layout.item_search_result, resultsContainer, false);

        TextView nameText = cardView.findViewById(R.id.event_name);
        TextView dateText = cardView.findViewById(R.id.event_date);
        TextView locationText = cardView.findViewById(R.id.event_location);
        TextView descriptionText = cardView.findViewById(R.id.event_description);

        nameText.setText(event.getName());

        if (event.getEventDate() != null) {
            dateText.setText(dateFormat.format(event.getEventDate()));
        }

        if (event.getEventTime() != null && !event.getEventTime().isEmpty()) {
            dateText.append(" at " + event.getEventTime());
        }

        locationText.setText(event.getLocation() != null ? event.getLocation() : "Location TBA");
        descriptionText.setText(event.getDescription() != null ? event.getDescription() : "");

        // Set click listener to open event details
        cardView.setTag(event.getId());
        cardView.setOnClickListener(v -> {
            String eventId = (String) v.getTag();
            launchEventDetails(eventId);
        });

        return cardView;
    }

    /**
     * Launch EventDetailsActivity for a specific event
     *
     * @param eventId ID of the event to view
     */
    private void launchEventDetails(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            Log.w(TAG, "Event ID is null/empty; cannot launch details");
            return;
        }
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    /**
     * Show or hide loading indicator
     *
     * @param show true to show loading, false to hide
     */
    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        resultsContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Show no results message
     *
     * @param message Message to display
     */
    private void showNoResults(String message) {
        resultsContainer.setVisibility(View.GONE);
        noResultsTextView.setVisibility(View.VISIBLE);
        noResultsTextView.setText(message);
        resultsCountTextView.setVisibility(View.GONE);
    }

    /**
     * Navigate back to EntrantMainActivity
     */
    private void navigateBack() {
        Intent intent = new Intent(this, EntrantMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}