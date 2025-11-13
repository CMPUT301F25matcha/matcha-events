package com.example.lotterysystemproject.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.R;
import com.google.android.material.button.MaterialButton;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Helper class to populate the events list container with event cards.
 */
public class EventListHelper {
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";

    private final Context context;
    private final LinearLayout container;
    private final EventRepository eventFirebase;
    private final SimpleDateFormat dateFormat;
    private Runnable onEventsLoaded;

    /**
     * Constructs a new EventListHelper.
     *
     * @param context        The application context for inflating views and accessing resources.
     * @param container      The LinearLayout container where event cards will be added.
     * @param onEventsLoaded A callback to be executed after events have been loaded and rendered.
     */
    public EventListHelper(Context context, LinearLayout container, Runnable onEventsLoaded) {
        this.context = context;
        this.container = container;
        this.onEventsLoaded = onEventsLoaded;
        this.eventFirebase = RepositoryProvider.getInstance();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    }

    /**
     * Loads all events from Firestore and populates the container.
     */
    public void loadEvents() {
        eventFirebase.getAllEvents(
                events -> {
                    container.removeAllViews();

                    if (events == null || events.isEmpty()) {
                        showEmptyState();
                        if (onEventsLoaded != null) onEventsLoaded.run();
                        return;
                    }

                    for (Event event : events) {
                        View eventCardView = createEventCard(event);
                        eventCardView.setTag(event.getId());  // SET TAG HERE
                        container.addView(eventCardView);
                    }

                    if (onEventsLoaded != null) onEventsLoaded.run();
                },
                error -> {
                    container.removeAllViews();
                    showErrorState(error.getMessage());
                    if (onEventsLoaded != null) onEventsLoaded.run();
                }
        );
    }

    /**
     * Gets the current user ID from SharedPreferences or returns null.
     * @return The current user's unique ID, or null if not found.
     */
    private String getCurrentUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Creates a view for a single event card.
     * @param event The event data to display.
     * @return A View representing the event card.
     */
    private View createEventCard(Event event) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View cardView = inflater.inflate(R.layout.entrant_item_event_card, container, false);

        // Get views
        TextView eventName = cardView.findViewById(R.id.event_name);
        TextView eventHostName = cardView.findViewById(R.id.event_host_name);
        TextView eventDate = cardView.findViewById(R.id.event_date);
        ImageView eventImage = cardView.findViewById(R.id.event_image);
        MaterialButton joinButton = cardView.findViewById(R.id.btn_join_waiting_list);

        // Set event data
        eventName.setText(event.getName() != null ? event.getName() : "Event Name");
        eventHostName.setText(event.getHostName() != null ? "Hosted by " + event.getHostName() : "Host Name");

        // Format date
        if (event.getDate() != null) {
            String dateStr = dateFormat.format(event.getDate());
            if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                eventDate.setText(dateStr + " • " + event.getLocation());
            } else {
                eventDate.setText(dateStr);
            }
        } else {
            eventDate.setText("Date TBD");
        }

        // Load event image if available
        // Note: For production, consider using an image loading library like Glide or Picasso
        // For now, we'll use a placeholder. You can implement image loading later.
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            // TODO: Implement image loading from URL
            // For now, the image view will show the placeholder background
        }

        // Handle join waiting list button click
        String userId = getCurrentUserId();
        if (userId != null && !userId.isEmpty()) {
            // Check if user is already on waiting list
            if (event.isUserOnWaitingList(userId)) {
                joinButton.setText("On Waiting List");
                joinButton.setEnabled(false);
            } else if (event.isUserParticipant(userId)) {
                joinButton.setText("Already Participating");
                joinButton.setEnabled(false);
            } else {
                joinButton.setOnClickListener(v -> joinWaitingList(event, userId, joinButton));
            }
        } else {
            // User not logged in
            joinButton.setOnClickListener(v -> {
                Toast.makeText(context, "Please log in to join waiting list", Toast.LENGTH_SHORT).show();
            });
        }

        return cardView;
    }

    /**
     * Handles joining the waiting list for an event.
     * @param event The event to join.
     * @param userId The ID of the user joining.
     * @param button The button that was clicked, to update its state.
     */
    private void joinWaitingList(Event event, String userId, MaterialButton button) {
        button.setEnabled(false);
        button.setText("Joining...");

        eventFirebase.joinWaitingList(event.getId(), userId, new com.example.lotterysystemproject.firebasemanager.EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                button.setText("On Waiting List");
                button.setEnabled(false);
                Toast.makeText(context, "Successfully joined waiting list!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(Exception error) {
                button.setEnabled(true);
                button.setText("Join Waiting List");
                Toast.makeText(context, "Failed to join waiting list: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows an empty state message when no events are available.
     */
    private void showEmptyState() {
        TextView emptyTextView = new TextView(context);
        emptyTextView.setText("No events available at this time.");
        emptyTextView.setTextSize(16);
        emptyTextView.setPadding(32, 64, 32, 32);
        emptyTextView.setGravity(android.view.Gravity.CENTER);
        container.addView(emptyTextView);
    }

    /**
     * Shows an error state message when loading events fails.
     * @param errorMessage The error message to display.
     */
    private void showErrorState(String errorMessage) {
        TextView errorTextView = new TextView(context);
        errorTextView.setText("Error loading events: " + errorMessage);
        errorTextView.setTextSize(16);
        errorTextView.setPadding(32, 64, 32, 32);
        errorTextView.setGravity(android.view.Gravity.CENTER);
        container.addView(errorTextView);
    }
}
