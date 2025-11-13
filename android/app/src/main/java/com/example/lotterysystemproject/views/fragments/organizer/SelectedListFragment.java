package com.example.lotterysystemproject.views.fragments.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.adapters.EntrantAdapter;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.repositories.EntrantRepository;
import com.example.lotterysystemproject.viewmodels.EntrantViewModel;

/**
 * Displays and manages the list of selected entrants for an event.
 * <p>
 * Provides filtering options (all, enrolled, cancelled), supports entrant actions
 * such as cancellation and drawing replacements, and includes options for exporting
 * data and sending notifications.
 * </p>
 */
public class SelectedListFragment extends Fragment implements EntrantAdapter.OnEntrantActionListener {

    /** Shared ViewModel for managing entrant data. */
    private EntrantViewModel entrantViewModel;

    /** RecyclerView displaying the list of selected entrants. */
    private RecyclerView recyclerView;

    /** Adapter for managing entrant list items. */
    private EntrantAdapter adapter;

    /** Displays current list title and entrant count. */
    private TextView titleText;

    /** Filter buttons for entrant status. */
    private Button filterAllButton, filterEnrolledButton, filterCancelledButton;

    /** Buttons for exporting data and sending notifications. */
    private Button exportCsvButton, sendNotificationButton;

    /** Currently active entrant filter. Null represents “All”. */
    private Entrant.Status currentFilter = null;

    /**
     * Inflates the selected entrants layout, initializes UI components,
     * sets up the RecyclerView, and attaches observers and listeners.
     *
     * @param inflater LayoutInflater to inflate the layout
     * @param container Parent view container
     * @param savedInstanceState Saved instance state, if any
     * @return The root view of the inflated layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selected_list, container, false);

        // Get shared ViewModel
        entrantViewModel = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        // Initialize views
        recyclerView = view.findViewById(R.id.selected_recycler_view);
        titleText = view.findViewById(R.id.selected_list_title);
        filterAllButton = view.findViewById(R.id.filter_all_button);
        filterEnrolledButton = view.findViewById(R.id.filter_enrolled_button);
        filterCancelledButton = view.findViewById(R.id.filter_cancelled_button);
        exportCsvButton = view.findViewById(R.id.export_csv_button);
        sendNotificationButton = view.findViewById(R.id.send_notification_button);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EntrantAdapter(1, this); // 1 = TYPE_SELECTED
        recyclerView.setAdapter(adapter);

        // Observe entrants initially (All)
        observeSelectedEntrants();

        // Filter button actions
        filterAllButton.setOnClickListener(v -> {
            currentFilter = null;
            updateFilterButtons();
            observeSelectedEntrants();
        });

        filterEnrolledButton.setOnClickListener(v -> {
            currentFilter = Entrant.Status.ENROLLED;
            updateFilterButtons();
            observeSelectedEntrants();
        });

        filterCancelledButton.setOnClickListener(v -> {
            currentFilter = Entrant.Status.CANCELLED;
            updateFilterButtons();
            observeSelectedEntrants();
        });

        // Export and notification actions
        exportCsvButton.setOnClickListener(v -> exportToCsv());
        sendNotificationButton.setOnClickListener(v -> sendNotification());

        return view;
    }

    /**
     * Observes the list of selected entrants filtered by {@link #currentFilter}
     * and updates the RecyclerView and title text accordingly.
     */
    private void observeSelectedEntrants() {
        entrantViewModel.getFilteredSelected(currentFilter).observe(getViewLifecycleOwner(), entrants -> {
            adapter.updateEntrants(entrants);

            String filterText = currentFilter == null ? "All" :
                    currentFilter == Entrant.Status.ENROLLED ? "Enrolled" : "Cancelled";
            titleText.setText("Selected Entrants (" + entrants.size() + ") - " + filterText);
        });
    }

    /**
     * Updates the background tint of filter buttons to visually reflect
     * the currently selected filter.
     */
    private void updateFilterButtons() {
        // Reset all to inactive color
        filterAllButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
        filterEnrolledButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
        filterCancelledButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));

        // Highlight active filter
        if (currentFilter == null) {
            filterAllButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.black, null));
        } else if (currentFilter == Entrant.Status.ENROLLED) {
            filterEnrolledButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.black, null));
        } else if (currentFilter == Entrant.Status.CANCELLED) {
            filterCancelledButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.black, null));
        }
    }

    /**
     * Handles cancellation of an entrant from the selected list.
     * Invokes {@link EntrantViewModel#cancelEntrant(String, EntrantRepository.OnActionCompleteListener)}
     * and displays a success or error message.
     *
     * @param entrant The entrant to be cancelled
     */
    @Override
    public void onCancelEntrant(Entrant entrant) {
        entrantViewModel.cancelEntrant(entrant.getId(), new EntrantRepository.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(),
                        "✓ Cancelled " + entrant.getName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles drawing a replacement for a cancelled entrant.
     * Invokes {@link EntrantViewModel#drawReplacement(EntrantRepository.OnReplacementDrawnListener)}
     * and displays the result via Toast messages.
     *
     * @param entrant The entrant being replaced
     */
    @Override
    public void onDrawReplacement(Entrant entrant) {
        entrantViewModel.drawReplacement(new EntrantRepository.OnReplacementDrawnListener() {
            @Override
            public void onSuccess(Entrant replacement) {
                Toast.makeText(getContext(),
                        "✓ Drew replacement: " + replacement.getName(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Placeholder for exporting the current entrant list to CSV.
     * Currently shows a Toast message indicating the export action.
     */
    private void exportToCsv() {
        String filterText = currentFilter == null ? "all selected" :
                currentFilter == Entrant.Status.ENROLLED ? "enrolled" : "cancelled";
        Toast.makeText(getContext(),
                "📥 Exporting " + filterText + " entrants to CSV...",
                Toast.LENGTH_SHORT).show();

        // TODO: Implement actual CSV export
    }

    /**
     * Placeholder for sending notifications to selected entrants.
     * Currently shows a Toast message indicating the notification action.
     */
    private void sendNotification() {
        String filterText = currentFilter == null ? "all selected" :
                currentFilter == Entrant.Status.ENROLLED ? "enrolled" : "cancelled";
        Toast.makeText(getContext(),
                "📤 Sending notification to " + filterText + " entrants...",
                Toast.LENGTH_SHORT).show();

        // TODO: Navigate to send notification screen
    }
}
