package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.controllers.AdminUserProfileDialog;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.databinding.EventDetailsBinding;
import com.example.lotterysystemproject.views.entrant.EntryCriteriaDialogue;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.content.SharedPreferences;

/**
 * Activity for displaying detailed information about a specific event.
 * Allows entrants to view event details and join/leave the waiting list.
 *
 * Related User Stories:
 * - US 01.01.01: Join waiting list for a specific event
 * - US 01.01.02: Leave waiting list for a specific event
 * - US 01.05.04: View total entrants on waiting list
 * - US 01.06.01: View event details by scanning QR code
 */
public class EventDetailsActivity extends AppCompatActivity {
    private EventDetailsBinding binding;
    private String eventId;
    private Event event;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    private boolean requestInProgress = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch the event from the firebase
        loadEventDetails();

        // Back button
        binding.backButton.setOnClickListener(v -> finish());

        // Favorite button empty handler
        binding.btnFavorite.setOnClickListener(v -> {
            // TODO: Implement add/remove favorite logic
            Toast.makeText(this, "Favorite clicked", Toast.LENGTH_SHORT).show();
        });

        // More options button empty handler
        binding.btnMore.setOnClickListener(v -> {
            // TODO: Implement popup menu/actions
            Toast.makeText(this, "More options clicked", Toast.LENGTH_SHORT).show();
        });

        // Notification switch (stub)
        binding.notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Implement notifications toggle
        });

        // View Entry Criteria (stub)
        binding.viewEntryCriteria.setOnClickListener(v -> handleEntryCriteriaDialogue());
        // Join/leave waiting list logic
        binding.joinWaitingListButton.setOnClickListener(v -> handleWaitingListAction());
    }

    /**
     * Loads event details from FirebaseEventRepository.
     * Fetches the event data and populates the UI.
     */
    private void loadEventDetails() {
        RepositoryProvider.getEventRepository().getAllEvents().observe(this, events -> {
            if (events == null) {
                Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            for (Event e : events) {
                if (eventId.equals(e.getId())) {
                    event = e;
                    break;
                }
            }

            if (event == null) {
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            populateDetails();
        });
    }

    /**
     * Handles the join/leave waiting list button click.
     * Checks user authentication, determines current state, and performs
     * the appropriate action (join or leave).
     */
    private void handleWaitingListAction() {
        if (event == null || requestInProgress) return;

        String userId = getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        requestInProgress = true;
        binding.joinWaitingListButton.setEnabled(false);

        if (!event.isUserOnWaitingList(userId)) {
            // Join waiting list
            joinWaitingList(userId);
        } else {
            // Leave waiting list
            leaveWaitingList(userId);
        }
    }

    /**
     * Adds the current user to the event's waiting list.
     * Refreshes event data after successful operation.
     *
     * @param userId The ID of the user joining the waiting list
     */
    private void joinWaitingList(String userId) {
        RepositoryProvider.getInstance().joinWaitingList(event.getId(), userId, new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                // Refresh event data to get updated waiting list
                refreshEventData(() -> {
                    requestInProgress = false;
                    binding.joinWaitingListButton.setEnabled(true);
                    Toast.makeText(EventDetailsActivity.this, "Joined waiting list!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                requestInProgress = false;
                binding.joinWaitingListButton.setEnabled(true);
                Toast.makeText(EventDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Removes the current user from the event's waiting list.
     * Refreshes event data after successful operation.
     *
     * @param userId The ID of the user leaving the waiting list
     */
    private void leaveWaitingList(String userId) {
        EventRepository repository = RepositoryProvider.getInstance();
        repository.leaveWaitingList(event.getId(), userId, new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                // Refresh event data to get updated waiting list
                refreshEventData(() -> {
                    requestInProgress = false;
                    binding.joinWaitingListButton.setEnabled(true);
                    Toast.makeText(EventDetailsActivity.this, "Left waiting list.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                requestInProgress = false;
                binding.joinWaitingListButton.setEnabled(true);
                Toast.makeText(EventDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Refreshes the event data from FirebaseEventRepository to ensure UI shows
     * the most current waiting list state.
     *
     * @param onComplete Callback to execute after refresh completes
     */
    private void refreshEventData(Runnable onComplete) {
        RepositoryProvider.getEventRepository().getAllEvents().observe(this, events -> {
            if (events == null) {
                if (onComplete != null) {
                    onComplete.run();
                }
                return;
            }

            for (Event e : events) {
                if (eventId.equals(e.getId())) {
                    event = e;
                    break;
                }
            }

            updateJoinButton();
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    /**
     * Populates the UI with event details.
     * Displays event name, location, date, and updates the join button state.
     */
    private void populateDetails() {
        binding.eventName.setText(event.getName());
        binding.eventLocation.setText(event.getLocation());
        binding.eventDate.setText(event.getEventDate() != null ? dateFormat.format(event.getEventDate()) : "Date TBD");
        binding.eventDescription.setText(event.getDescription());
        updateJoinButton();
        // TODO: Load image, setup map, set state for notifications and joined status
    }

    /**
     * Updates the join/leave button text based on current waiting list state.
     * Shows the current number of entrants on the waiting list.
     *
     * Related User Story: US 01.05.04 - View total entrants on waiting list
     */
    private void updateJoinButton() {
        String userId = getCurrentUserId();
        boolean isUserOnWaitingList = event.isUserOnWaitingList(userId);
        int count = event.getWaitingList() != null ? event.getWaitingList().size() : 0;

        if (isUserOnWaitingList) {
            binding.joinWaitingListButton.setText("Leave Waiting List (" + count + ")");
        } else {
            binding.joinWaitingListButton.setText("Join Waiting List (" + count + ")");
        }
        binding.notificationsSwitch.setVisibility(isUserOnWaitingList ? View.VISIBLE : View.GONE);
    }

    /**
     * Retrieves the current user's ID from SharedPreferences.
     *
     * @return The user's ID, or null if not logged in
     */
    private String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("userId", null);
    }
    
    /**
     * Handles displaying the entry criteria dialog.
     */
    private void handleEntryCriteriaDialogue() {
        EntryCriteriaDialogue dialog = new EntryCriteriaDialogue();
        dialog.show(getSupportFragmentManager(), "EntryCriteriaDialogue");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}