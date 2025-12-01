package com.example.lotterysystemproject.views.fragments.organizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.adapters.EntrantAdapter;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.firebasemanager.EntrantRepository;
import com.example.lotterysystemproject.viewmodels.EntrantViewModel;
import com.example.lotterysystemproject.viewmodels.EventViewModel;
import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private EventViewModel eventViewModel;

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
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        entrantViewModel = provider.get(EntrantViewModel.class);
        eventViewModel = provider.get(EventViewModel.class);

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

    // ##################### CSV Exportational magic #################

    /** Launcher that writes CSV to file after file picking */
    private final ActivityResultLauncher<Intent> csvDocumentWriteLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    activityResult -> {
                        // Handle the result (the URI where you will write the CSV)
                        if (activityResult != null) {
                            Intent intent = activityResult.getData();

                            if (intent != null) {
                                Uri uri = intent.getData();

                                if (uri != null) {
                                    writeCsvFile(uri);
                                }
                            }
                        }
                    });

    private void openCSVWriteChooser(String defaultFilename) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/csv") // e.g., "application/pdf", "text/plain"
                .putExtra(Intent.EXTRA_TITLE, defaultFilename);

        csvDocumentWriteLauncher.launch(intent);
    }


    private void writeCsvFile(@NonNull Uri uri) {
        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
             // 1. Wrap the OutputStream in an OutputStreamWriter to handle character encoding (UTF-8 is best)
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             // 2. Pass the Writer to OpenCSV's CSVWriter
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // --- Data to be written ---

            List<Entrant> entrants = entrantViewModel.getFilteredSelected(currentFilter).getValue();

            if(entrants == null) {
                Toast.makeText(getContext(), "Entrants is null", Toast.LENGTH_SHORT).show();
                return;
            }

            // Write the header
            csvWriter.writeNext(new String[]{"Name", "Email", "Phone"});
            for (Entrant e : entrants) {
                csvWriter.writeNext(new String[]{e.getName(), e.getEmail(), e.getPhone()});
            }

            Toast.makeText(getContext(),
                    "Exported CSV",
                    Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("FileHandler", "Error writing CSV file", e);
            // Handle the exception, perhaps show a Toast to the user
        } catch (Exception e) {
            Log.e("FileHandler", "Unexpected error", e);
        }
    }

    /**
     * Placeholder for exporting the current entrant list to CSV.
     * Currently shows a Toast message indicating the export action.
     */
    private void exportToCsv() {
        String filterText = currentFilter == null
                ? "all selected"
                : currentFilter == Entrant.Status.ENROLLED
                    ? "enrolled"
                    : "cancelled";

        Toast.makeText(getContext(),
                "📥 Exporting " + filterText + " entrants to CSV...",
                Toast.LENGTH_SHORT).show();

        // TODO: Implement actual CSV export with event name
        String dateNow = DateTimeFormatter
                .ofPattern("yyyy-MM-dd_hhmmss")
                .format(ZonedDateTime.now());

        openCSVWriteChooser("list" + "-" + filterText + "_" + dateNow + ".csv");
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