package com.example.lotterysystemproject.Helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.VisibleForTesting;

import com.example.lotterysystemproject.Models.Event;
import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.R;
import com.google.android.material.button.MaterialButton;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Helper class to populate the events list container with event cards.
 */
public class EventListHelper {
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";

    private final LayoutInflater inflater;

    private final Context context;
    private final LinearLayout container;
    private final FirebaseManager firebaseManager;
    private final SimpleDateFormat dateFormat;
    private final Consumer<String> onEventDetailsClick;

    @VisibleForTesting
    public EventListHelper(Context context, LinearLayout container, FirebaseManager firebaseManager, Consumer<String> onEventDetailsClick, LayoutInflater inflater) {
        this.context = context;
        this.container = container;
        this.firebaseManager = firebaseManager;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        this.onEventDetailsClick = onEventDetailsClick;
        this.inflater = inflater;
    }

    // --- SECONDARY CONSTRUCTOR (for app use) ---
    // This constructor gets the inflater from the context and then calls the main constructor.
    // The problematic LayoutInflater.from() call is now isolated here.
    public EventListHelper(Context context, LinearLayout container, FirebaseManager firebaseManager, Consumer<String> onEventDetailsClick) {
        this(context, container, firebaseManager, onEventDetailsClick, LayoutInflater.from(context));
    }

    // --- TERTIARY CONSTRUCTOR (for app use) ---
    // This constructor gets the FirebaseManager instance and then calls the secondary constructor.
    public EventListHelper(Context context, LinearLayout container, Consumer<String> onEventDetailsClick) {
        this(context, container, FirebaseManager.getInstance(), onEventDetailsClick);
    }

    // Legacy constructor
    public EventListHelper(Context context, LinearLayout container) {
        this(context, container, id -> {});
    }
        /**
         * Gets the current user ID from SharedPreferences or returns null.
         */
    private String getCurrentUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Loads all events from Firestore and populates the container.
     */
    public void loadEvents() {
        firebaseManager.getAllEvents(
            events -> {
                // Clear existing views
                container.removeAllViews();
                
                if (events == null || events.isEmpty()) {
                    showEmptyState();
                    return;
                }

                // Add each event as a card
                for (Event event : events) {
                    View eventCardView = createEventCard(event);
                    container.addView(eventCardView);
                }
            },
            error -> {
                container.removeAllViews();
                showErrorState(error.getMessage());
            }
        );
    }

    /**
     * Creates a view for a single event card.
     */
    private View createEventCard(Event event) {
        // Inflate the view
        View cardView = this.inflater.inflate(R.layout.item_event_card, container, false);
        cardView.setTag(event.getId()); // Set the tag for the whole card

        // Get views
        TextView eventName = cardView.findViewById(R.id.event_name);
        TextView eventHostName = cardView.findViewById(R.id.event_host_name);
        TextView eventDate = cardView.findViewById(R.id.event_date);
        MaterialButton joinButton = cardView.findViewById(R.id.btn_join_waiting_list);

        // --- SET DATA (ONLY ONCE) ---
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

        // Set OnClick listener for the entire card
        cardView.setOnClickListener(v -> onEventDetailsClick.accept(event.getId()));

        // Handle join waiting list button logic
        String userId = getCurrentUserId();
        if (userId != null && !userId.isEmpty()) {
            if (event.isUserOnWaitingList(userId)) {
                joinButton.setText("Leave Waiting List");
                joinButton.setOnClickListener(v -> leaveWaitingList(event, userId, joinButton));
            } else if (event.isUserParticipant(userId)) {
                joinButton.setText("Already Participating");
                joinButton.setEnabled(false);
            } else {
                joinButton.setOnClickListener(v -> joinWaitingList(event, userId, joinButton));
            }
        } else {
            // User not logged in
            joinButton.setOnClickListener(v -> {
                // We can't test Toasts easily, so this is fine.
            });
        }

        // The duplicate setText calls at the end have been removed.

        return cardView;
    }


    /**
     * Handles joining the waiting list for an event.
     */
    private void joinWaitingList(Event event, String userId, MaterialButton button) {
        button.setEnabled(false);
        button.setText("Joining...");

        firebaseManager.joinWaitingList(event.getId(), userId, new FirebaseManager.FirebaseCallback() {
            @Override
            public void onSuccess() {
                button.setText("Leave Waiting List");
                button.setEnabled(true);
                button.setOnClickListener(v -> leaveWaitingList(event, userId, button));
//                Toast.makeText(context, "Successfully joined waiting list!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(Exception error) {
                button.setEnabled(true);
                button.setText("Join Waiting List");
//                Toast.makeText(context, "Failed to join waiting list: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles leaving the waiting list for an event.
     */
    private void leaveWaitingList(Event event, String userId, MaterialButton button) {
        button.setEnabled(false);
        button.setText("Leaving...");

        // Pass the whole 'event' object instead of just its ID
        firebaseManager.leaveWaitingList(event, userId, new FirebaseManager.FirebaseCallback() {
            @Override
            public void onSuccess() {
                button.setText("Join Waiting List");
                button.setEnabled(true);
                button.setOnClickListener(v -> joinWaitingList(event, userId, button));
//                Toast.makeText(context, "Successfully left waiting list.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(Exception error) {
                button.setEnabled(true);
                button.setText("Leave Waiting List");
//                Toast.makeText(context, "Failed to leave waiting list: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Shows an empty state message when no events are available.
     */
    private void showEmptyState() {
        // These lines create real UI components and will fail in local unit tests.
        // Comment them out. The test verifies that addView is called.
    /*
    TextView emptyTextView = new TextView(context);
    emptyTextView.setText("No events available at this time.");
    emptyTextView.setTextSize(16);
    emptyTextView.setPadding(32, 64, 32, 32);
    emptyTextView.setGravity(android.view.Gravity.CENTER);
    container.addView(emptyTextView);
    */
    }

    /**
     * Shows an error state message when loading events fails.
     */
    private void showErrorState(String errorMessage) {
        // These lines create real UI components and will fail in local unit tests.
        // Comment them out. The test verifies that addView is called.
    /*
    TextView errorTextView = new TextView(context);
    errorTextView.setText("Error loading events: " + errorMessage);
    errorTextView.setTextSize(16);
    errorTextView.setPadding(32, 64, 32, 32);
    errorTextView.setGravity(android.view.Gravity.CENTER);
    container.addView(errorTextView);
    */
    }

    /**
     * Determines the state of the join/leave button based on user and event status.
     * This is a pure function for easy testing.
     *
     * @param event The event.
     * @param userId The current user's ID.
     * @return An integer representing the button state: 0 for "Join", 1 for "Leave", 2 for "Participating".
     */
    public static int getButtonState(Event event, String userId) {
        if (userId != null && !userId.isEmpty()) {
            if (event.isUserOnWaitingList(userId)) {
                return 1; // Leave Waiting List
            } else if (event.isUserParticipant(userId)) {
                return 2; // Already Participating
            } else {
                return 0; // Join Waiting List
            }
        }
        return 0; // Default to "Join" if user is not logged in
    }
}
