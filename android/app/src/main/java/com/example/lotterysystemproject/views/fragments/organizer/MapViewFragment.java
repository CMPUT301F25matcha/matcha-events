package com.example.lotterysystemproject.views.fragments.organizer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.viewmodels.EntrantViewModel;
import com.example.lotterysystemproject.viewmodels.EventViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment displaying a map of where entrants joined the waiting list.
 * Shows markers for each entrant location, clustered when zoomed out.
 * Color-coded by status: waiting (blue), invited (orange), enrolled (green).
 */
public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private ClusterManager<EntrantClusterItem> clusterManager;
    private EntrantViewModel entrantViewModel;
    private EventViewModel eventViewModel;
    private String eventId;
    private Event currentEvent;
    private LinearLayout emptyStateText;
    private View mapContainer;

    /**
     * Cluster item representing an entrant's location on the map.
     */
    private static class EntrantClusterItem implements ClusterItem {
        private final LatLng position;
        private final String title;
        private final String snippet;
        private final Entrant.Status status;

        public EntrantClusterItem(double lat, double lng, String name, Entrant.Status status) {
            this.position = new LatLng(lat, lng);
            this.title = name;
            this.snippet = "Status: " + status.name();
            this.status = status;
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return position;
        }

        @Nullable
        @Override
        public String getTitle() {
            return title;
        }

        @Nullable
        @Override
        public String getSnippet() {
            return snippet;
        }

        @Nullable
        @Override
        public Float getZIndex() {
            return 0f;
        }

        public Entrant.Status getStatus() {
            return status;
        }
    }

    /**
     * Custom cluster renderer that colors markers by entrant status.
     */
    private class StatusClusterRenderer extends DefaultClusterRenderer<EntrantClusterItem> {

        public StatusClusterRenderer(GoogleMap map, ClusterManager<EntrantClusterItem> clusterManager) {
            super(MapViewFragment.this.requireContext(), map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull EntrantClusterItem item, @NonNull MarkerOptions markerOptions) {
            markerOptions.icon(getMarkerIcon(item.getStatus()));
            markerOptions.title(item.getTitle());
            markerOptions.snippet(item.getSnippet());
        }

        /**
         * Creates colored marker icons based on entrant status.
         */
        private BitmapDescriptor getMarkerIcon(Entrant.Status status) {
            int color;
            switch (status) {
                case WAITING:
                    color = Color.parseColor("#2196F3"); // Blue
                    break;
                case INVITED:
                    color = Color.parseColor("#FF9800"); // Orange
                    break;
                case ENROLLED:
                    color = Color.parseColor("#4CAF50"); // Green
                    break;
                case CANCELLED:
                    color = Color.parseColor("#9E9E9E"); // Gray
                    break;
                default:
                    color = Color.parseColor("#757575"); // Default gray
                    break;
            }
            return BitmapDescriptorFactory.defaultMarker(getHueFromColor(color));
        }

        /**
         * Converts RGB color to hue for Google Maps marker.
         */
        private float getHueFromColor(int color) {
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            return hsv[0];
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        // Get eventId from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        // Initialize UI components
        emptyStateText = view.findViewById(R.id.empty_state_text);
        mapContainer = view.findViewById(R.id.map_container);

        // Initialize ViewModels
        entrantViewModel = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Load event details to get event location
        loadEventDetails();

        return view;
    }

    /**
     * Loads the current event to get its center coordinates.
     */
    private void loadEventDetails() {
        eventViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                for (Event event : events) {
                    if (event.getId().equals(eventId)) {
                        currentEvent = event;
                        if (googleMap != null) {
                            // Reload markers if map is already ready
                            loadEntrantLocations();
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Configure map settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Setup cluster manager
        setupClusterManager();

        // Load entrant locations
        loadEntrantLocations();
    }

    /**
     * Initializes the cluster manager for grouping nearby markers.
     */
    private void setupClusterManager() {
        clusterManager = new ClusterManager<>(requireContext(), googleMap);
        clusterManager.setRenderer(new StatusClusterRenderer(googleMap, clusterManager));

        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
    }

    /**
     * Loads all entrants with geolocation data and displays them on the map.
     */
    private void loadEntrantLocations() {
        if (googleMap == null) return;

        entrantViewModel.getEntrants().observe(getViewLifecycleOwner(), entrants -> {
            if (entrants == null || entrants.isEmpty()) {
                showEmptyState();
                return;
            }

            // Filter entrants with valid location data
            List<Entrant> entrantsWithLocation = new ArrayList<>();
            for (Entrant entrant : entrants) {
                if (entrant.getLatitude() != 0.0 && entrant.getLongitude() != 0.0) {
                    entrantsWithLocation.add(entrant);
                }
            }

            if (entrantsWithLocation.isEmpty()) {
                showEmptyState();
                return;
            }

            // Hide empty state and show map
            emptyStateText.setVisibility(View.GONE);
            mapContainer.setVisibility(View.VISIBLE);

            // Clear existing markers
            clusterManager.clearItems();

            // Add markers for each entrant
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boolean hasValidBounds = false;

            for (Entrant entrant : entrantsWithLocation) {
                double lat = entrant.getLatitude();
                double lng = entrant.getLongitude();

                // Validate coordinates
                if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                    EntrantClusterItem item = new EntrantClusterItem(
                            lat,
                            lng,
                            entrant.getName() != null ? entrant.getName() : "Anonymous",
                            entrant.getStatus()
                    );
                    clusterManager.addItem(item);
                    boundsBuilder.include(new LatLng(lat, lng));
                    hasValidBounds = true;
                }
            }

            // Cluster the markers
            clusterManager.cluster();

            // Zoom to show all markers, or center on event location
            if (hasValidBounds) {
                try {
                    LatLngBounds bounds = boundsBuilder.build();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                } catch (Exception e) {
                    centerOnEventLocation();
                }
            } else {
                centerOnEventLocation();
            }

            // Show summary
            showLocationSummary(entrantsWithLocation);
        });
    }

    /**
     * Centers the map on the event's location if available.
     */
    private void centerOnEventLocation() {
        if (currentEvent != null && currentEvent.getLatitude() != 0.0 && currentEvent.getLongitude() != 0.0) {
            LatLng eventLocation = new LatLng(currentEvent.getLatitude(), currentEvent.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 12f));

            // Add marker for event location
            googleMap.addMarker(new MarkerOptions()
                    .position(eventLocation)
                    .title(currentEvent.getName())
                    .snippet("Event Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            // Default to Edmonton if no event location
            LatLng edmonton = new LatLng(53.5461, -113.4938);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 10f));
        }
    }

    /**
     * Displays empty state when no location data is available.
     */
    private void showEmptyState() {
        // Make the entire LinearLayout container visible
        emptyStateText.setVisibility(View.VISIBLE);

        // Hide the map container
        mapContainer.setVisibility(View.GONE);

        // The text is already defined in your XML layout, so you just need to show the container.
        // The .setText() line has been removed.
    }

    /**
     * Shows a summary toast with location statistics.
     */
    private void showLocationSummary(List<Entrant> entrants) {
        Map<Entrant.Status, Integer> statusCounts = new HashMap<>();
        for (Entrant entrant : entrants) {
            Entrant.Status status = entrant.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }

        StringBuilder summary = new StringBuilder("Locations: ");
        summary.append(entrants.size()).append(" entrants\n");

        if (statusCounts.containsKey(Entrant.Status.WAITING)) {
            summary.append("🔵 Waiting: ").append(statusCounts.get(Entrant.Status.WAITING)).append("\n");
        }
        if (statusCounts.containsKey(Entrant.Status.INVITED)) {
            summary.append("🟠 Invited: ").append(statusCounts.get(Entrant.Status.INVITED)).append("\n");
        }
        if (statusCounts.containsKey(Entrant.Status.ENROLLED)) {
            summary.append("🟢 Enrolled: ").append(statusCounts.get(Entrant.Status.ENROLLED));
        }

        Toast.makeText(requireContext(), summary.toString().trim(), Toast.LENGTH_LONG).show();
    }
}

