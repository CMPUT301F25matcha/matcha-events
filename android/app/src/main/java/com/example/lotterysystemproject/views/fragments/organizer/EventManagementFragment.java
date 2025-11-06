package com.example.lotterysystemproject.Views.fragments.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.adapters.TabsPagerAdapter;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.viewmodels.EntrantViewModel;
import com.example.lotterysystemproject.viewmodels.EventViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventManagementFragment extends Fragment {

    private EventViewModel eventViewModel;
    private EntrantViewModel entrantViewModel;

    private TextView eventNameHeader, eventDate, eventLocation, eventEnrollment;
    private Button backButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button qrCodeButton;

    private String eventId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_management, container, false);

        // Get event ID from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        // Initialize ViewModels
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        entrantViewModel = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        // Load entrants for this event
        entrantViewModel.loadEntrants(eventId);

        // Initialize views
        backButton = view.findViewById(R.id.back_button);
        eventNameHeader = view.findViewById(R.id.event_name_header);
        eventDate = view.findViewById(R.id.event_date);
        eventLocation = view.findViewById(R.id.event_location);
        eventEnrollment = view.findViewById(R.id.event_enrollment);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        qrCodeButton = view.findViewById(R.id.qr_code_button);

        // Setup tabs
        setupTabs();

        // Load event details
        loadEventDetails();

        // Back button
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        qrCodeButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("eventId", eventId);

            Navigation.findNavController(v).navigate(
                    R.id.action_eventManagement_to_qrCodeDisplay,
                    args
            );
        });

        return view;
    }

    private void setupTabs() {
        TabsPagerAdapter adapter = new TabsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Waiting");
                    break;
                case 1:
                    tab.setText("Selected");
                    break;
            }
        }).attach();
    }

    private void loadEventDetails() {
        // Get event details from EventViewModel
        eventViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                for (Event event : events) {
                    if (event.getId().equals(eventId)) {
                        displayEventDetails(event);
                        break;
                    }
                }
            }
        });
    }

    private void displayEventDetails(Event event) {
        eventNameHeader.setText(event.getName());
        eventDate.setText("📅 " + dateFormat.format(event.getEventDate()));
        eventLocation.setText("📍 " + event.getLocation());
        eventEnrollment.setText("👥 " + event.getEnrolled() + "/" + event.getCapacity() + " enrolled");
    }
    public void switchToSelectedTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1); // 1 = Selected tab
        }
    }
}