package com.example.lotterysystemproject.views.entrant;

import android.content.Intent;
import android.net.Uri;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.controllers.AdminUserProfileDialog;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.databinding.EventDetailsBinding;
import com.example.lotterysystemproject.views.entrant.EntryCriteriaDialogue;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Locale;
import android.content.SharedPreferences;

/**
 * Activity for displaying detailed information about a specific event.
 * Allows entrants to view event details and join/leave the waiting list.
 * Handles geolocation capture when required by the event.
 *
 * Related User Stories:
 * - US 01.01.01: Join waiting list for a specific event
 * - US 01.01.02: Leave waiting list for a specific event
 * - US 01.05.04: View total entrants on waiting list
 * - US 01.06.01: View event details by scanning QR code
 * - US 02.02.02: See on a map where entrants joined from
 * - US 02.02.03: Enable/disable geolocation requirement
 */
public class EventDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private EventDetailsBinding binding;
    private String eventId;
    private Event event;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    private boolean requestInProgress = false;

    // Geolocation components
    private FusedLocationProviderClient fusedLocationClient;
    private Location capturedLocation;
    private boolean waitingForLocation = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ATTENTION: This was auto-generated to handle app links.
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");

        // just in case we receive from app link
        if (eventId == null){
            Uri uri = intent.getData();
            if(uri != null)
                eventId = uri.getQueryParameter("eventId");
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

            populateDetails();
        });
    }

    private void initMap() {
        if (event == null || isFinishing() || isDestroyed()) return;

        // Only load map if we have valid coordinates
        if (event.getLatitude() != 0.0 || event.getLongitude() != 0.0) {
            binding.mapContainer.setVisibility(View.VISIBLE);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_container);

            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                // Use commitAllowingStateLoss to avoid crashes if activity state saved
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.map_container, mapFragment)
                        .commitAllowingStateLoss();
            }

            mapFragment.getMapAsync(this);
        } else {
            binding.mapContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (event != null) {
            LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(eventLocation).title(event.getLocation()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15f));
            googleMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    /**
     * Handles the join/leave waiting list button click.
     * Checks user authentication, determines current state, and performs
     * the appropriate action (join or leave).
     *
     * For joining: checks if geolocation is required and captures location if needed.
     */
    private void handleWaitingListAction() {
        if (event == null || requestInProgress) return;

        String userId = getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!event.isUserOnWaitingList(userId)) {
            // User is NOT on the waiting list - JOIN action
            requestInProgress = true;
            binding.joinWaitingListButton.setEnabled(false);

            // Check if geolocation is required for this event
            if (event.isGeolocationRequired()) {
                checkLocationPermissionAndCapture();
            } else {
                // No geolocation required, proceed directly
                joinWaitingList(userId, null, null);
            }
        } else {
            // User IS on the waiting list - LEAVE action
            requestInProgress = true;
            binding.joinWaitingListButton.setEnabled(false);
            leaveWaitingList(userId);
        }
    }

    /**
     * Checks if location permission is granted. If not, requests it.
     * If granted, captures the current location.
     */
    private void checkLocationPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, capture location
            captureCurrentLocation();
        } else {
            // Request permission
            waitingForLocation = true;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Handles the result of the location permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, capture location
                if (waitingForLocation) {
                    captureCurrentLocation();
                }
            } else {
                // Permission denied - reset state and update UI
                waitingForLocation = false;
                requestInProgress = false;
                binding.joinWaitingListButton.setEnabled(true);
                updateJoinButton(); // Restore button text
                Toast.makeText(this,
                        "Location permission is required to join this event",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Captures the current location using FusedLocationProviderClient.
     * Once captured, proceeds to join the waiting list.
     */
    private void captureCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestInProgress = false;
            binding.joinWaitingListButton.setEnabled(true);
            updateJoinButton(); // Restore button text
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        waitingForLocation = false;

                        if (location != null) {
                            // Location captured successfully
                            capturedLocation = location;
                            String userId = getCurrentUserId();

                            // Proceed to join with location data
                            joinWaitingList(userId,
                                    location.getLatitude(),
                                    location.getLongitude());
                        } else {
                            // Location is null, possibly location services are off
                            requestInProgress = false;
                            binding.joinWaitingListButton.setEnabled(true);
                            updateJoinButton(); // Restore button text
                            Toast.makeText(EventDetailsActivity.this,
                                    "Could not get location. Please enable location services.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(this, e -> {
                    waitingForLocation = false;
                    requestInProgress = false;
                    binding.joinWaitingListButton.setEnabled(true);
                    updateJoinButton(); // Restore button text
                    Toast.makeText(EventDetailsActivity.this,
                            "Failed to get location: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Adds the current user to the event's waiting list.
     * Refreshes event data after successful operation.
     *
     * If geolocation is required, the latitude and longitude are saved with the entrant record.
     *
     * @param userId The ID of the user joining the waiting list
     * @param latitude The latitude where the user joined (null if not required)
     * @param longitude The longitude where the user joined (null if not required)
     */
    private void joinWaitingList(String userId, Double latitude, Double longitude) {
        RepositoryProvider.getEventRepository().joinWaitingList(event.getId(), userId,
                new EventRepository.RepositoryCallback() {
                    @Override
                    public void onSuccess() {
                        // If geolocation was captured, save it to the entrant record
                        if (latitude != null && longitude != null) {
                            saveEntrantGeolocation(userId, latitude, longitude);
                        }

                        // Refresh event data to get updated waiting list
                        refreshEventData(() -> {
                            requestInProgress = false;
                            binding.joinWaitingListButton.setEnabled(true);
                            updateJoinButton(); // Update button text to "Leave Waiting List"

                            String message = latitude != null && longitude != null
                                    ? "Joined waiting list! (Location saved)"
                                    : "Joined waiting list!";
                            Toast.makeText(EventDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        requestInProgress = false;
                        binding.joinWaitingListButton.setEnabled(true);
                        updateJoinButton(); // Restore button text
                        Toast.makeText(EventDetailsActivity.this,
                                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves the geolocation data for the entrant.
     * This creates a geolocation record in Firebase that can be used by organizers
     * to see where entrants joined from on a map.
     *
     * Related User Story: US 02.02.02 - See on a map where entrants joined from
     *
     * @param userId The ID of the user
     * @param latitude The latitude where the user joined
     * @param longitude The longitude where the user joined
     */
    private void saveEntrantGeolocation(String userId, double latitude, double longitude) {
        // Get Firestore instance
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        // Find the entrant document for this specific event and user
        db.collection("entrants")
                .whereEqualTo("eventId", event.getId())
                .whereEqualTo("userId", userId) // Ensure your Entrant documents actually have a "userId" field
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the document reference
                        com.google.firebase.firestore.DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                        // Create update map
                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
                        updates.put("latitude", latitude);
                        updates.put("longitude", longitude);

                        // Update the existing entrant document
                        document.getReference().update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    android.util.Log.d("EventDetails", "Geolocation saved successfully");
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("EventDetails", "Failed to save geolocation", e);
                                });
                    } else {
                        android.util.Log.e("EventDetails", "Entrant not found to save location");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("EventDetails", "Error finding entrant to save location", e);
                });
    }

    /**
     * Removes the current user from the event's waiting list.
     * Refreshes event data after successful operation.
     *
     * @param userId The ID of the user leaving the waiting list
     */
    private void leaveWaitingList(String userId) {
        EventRepository repository = RepositoryProvider.getEventRepository();
        repository.leaveWaitingList(event.getId(), userId, new EventRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                // Refresh event data to get updated waiting list
                refreshEventData(() -> {
                    requestInProgress = false;
                    binding.joinWaitingListButton.setEnabled(true);
                    updateJoinButton(); // Update button text to "Join Waiting List"
                    Toast.makeText(EventDetailsActivity.this, "Left waiting list.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                requestInProgress = false;
                binding.joinWaitingListButton.setEnabled(true);
                updateJoinButton(); // Restore button text
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
        initMap();
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