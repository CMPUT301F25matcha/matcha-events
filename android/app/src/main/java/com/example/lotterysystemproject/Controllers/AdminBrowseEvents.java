package com.example.lotterysystemproject.Controllers;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterysystemproject.Models.EventAdmin;
import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.databinding.AdminBrowseEventsBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AdminBrowseEvents is a Fragment that allows administrators to browse, search events stored
 * in Firebase.
 *
 * - It displays a list of EventAdmin items in a RecyclerView, supports live searching.
 * - Initializing and binding the RecyclerView and its adapter.
 * - Listening to real-time event updates from Firebase.
 * - Providing search functionality to filter events by name or location.
 */
public class AdminBrowseEvents extends Fragment {

    /** View binding for the admin browse events layout. */
    private AdminBrowseEventsBinding binding;

    /** Adapter used to display event data in the RecyclerView. */
    private AdminEventsAdapter adapter;

    /** Current list of events displayed in the RecyclerView. */
    private final List<EventAdmin> eventAdminList = new ArrayList<>();

    /** Full list of all events retrieved from Firebase (used for filtering). */
    private final List<EventAdmin> allEventAdmins = new ArrayList<>();

    /** Instance of FirebaseManager for interacting with the Firebase database. */
    private FirebaseManager firebaseManager;



    /**
     * Inflates the fragment's layout and initializes the FirebaseManager
     *
     * @param inflater  The LayoutInflater used to inflate views in the fragment.
     * @param container The parent ViewGroup into which the fragment's UI should be attached.
     * @param savedInstanceState If not null, the fragment is being re-created from a previous state.
     * @return The root view of the inflated layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AdminBrowseEventsBinding.inflate(inflater, container, false);
        firebaseManager = FirebaseManager.getInstance();
        return binding.getRoot();
    }

    /**
     * Immediately called after the view is created. Sets up components including
     * the RecyclerView, search bar, navigation, adapter.
     *
     * @param view The created view.
     * @param savedInstanceState The saved state of the fragment, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize  Adapter
        adapter = new AdminEventsAdapter(getContext(), eventAdminList);
        binding.recyclerEvents.setAdapter(adapter);

        // Handle back arrow/navigation
        binding.backArrow.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminBrowseEvents.this).navigateUp()
        );

        // Search Events
        TextInputEditText searchEventInput = binding.searchInput;
        searchEventInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filterEvents(s.toString());

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });


        // addSampleEventsToFirebase();

        // Fetch events from Firebase
        fetchEvents();


    }

    /**
     * Fetches all events from Firebase.
     *
     * Updates both allEventAdmins and eventAdminList and refreshes the adapter.
     * If no events are found, it automatically adds sample events to Firebase (temporary implementation)
     *
     */
    private void fetchEvents() {
        firebaseManager.listenToAllEvents(events -> {
            allEventAdmins.clear();
            allEventAdmins.addAll(events);

            eventAdminList.clear();
            eventAdminList.addAll(events);
            adapter.notifyDataSetChanged();

            // Populate Firebase with sample events if empty (temporary implementation)
            if (events.isEmpty()) {
                addSampleEventsToFirebase();
            }

        }, e -> e.printStackTrace());
    }


    /**
     * Adds a set of predefined sample EventAdmin objects to Firebase.
     * Only for testing purposes when no events exist.
     */
    private void addSampleEventsToFirebase() {
        // Use existing instance
        // Create a few example events
        EventAdmin e1 = new EventAdmin("Charity Marathon", new Date(), "10:00 AM", "Downtown Park", 50);
        e1.setId(firebaseManager.getDatabase().collection("events").document().getId());
        e1.setDescription("A community marathon to raise funds for hospitals.");

        EventAdmin e2 = new EventAdmin("Winter Festival", new Date(), "5:00 PM", "City Hall", 100);
        e2.setId(firebaseManager.getDatabase().collection("events").document().getId());
        e2.setDescription("Enjoy local food, music, and winter festivities.");

        EventAdmin e3 = new EventAdmin("Tech Conference", new Date(), "9:00 AM", "UofA Campus", 200);
        e3.setId(firebaseManager.getDatabase().collection("events").document().getId());
        e3.setDescription("A day of talks, networking, and innovation.");

        firebaseManager.addEvent(e1, null);
        firebaseManager.addEvent(e2, null);
        firebaseManager.addEvent(e3, null);
    }

    /**
     * Filters events based on a search query entered by the admin.
     *
     * The method performs a case-insensitive search on event names and locations.
     * If the query is empty, all events are displayed.
     *
     * @param query The text input used to filter events.
     */
    private void filterEvents(String query) {
        eventAdminList.clear();
        if (query.isEmpty()) {
            eventAdminList.addAll(allEventAdmins);
        } else {
            for (EventAdmin eventAdmin : allEventAdmins) {
                if (eventAdmin.getName().toLowerCase().contains(query.toLowerCase()) ||
                eventAdmin.getLocation().toLowerCase().contains(query.toLowerCase())) {
                    eventAdminList.add(eventAdmin);
                }
            }
        }

        adapter.notifyDataSetChanged();

    }


    /**
     * Cleans up resources by nullifying the binding reference to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }





}




