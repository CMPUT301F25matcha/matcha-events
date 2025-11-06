package com.example.lotterysystemproject.Views.fragments.organizer;

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

import com.example.lotterysystemproject.Models.Entrant;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.adapters.EntrantAdapter;
import com.example.lotterysystemproject.Models.Entrant;
import com.example.lotterysystemproject.repositories.EntrantRepository;
import com.example.lotterysystemproject.viewmodels.EntrantViewModel;

public class SelectedListFragment extends Fragment implements EntrantAdapter.OnEntrantActionListener {

    private EntrantViewModel entrantViewModel;
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private TextView titleText;
    private Button filterAllButton, filterEnrolledButton, filterCancelledButton;
    private Button exportCsvButton, sendNotificationButton;

    private Entrant.Status currentFilter = null; // null = show all

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selected_list, container, false);

        // Get shared ViewModel from parent activity
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

        // Observe selected entrants (initially all)
        observeSelectedEntrants();

        // Filter buttons
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

        // Export CSV (US 02.06.05)
        exportCsvButton.setOnClickListener(v -> exportToCsv());

        // Send Notification (US 02.07.02)
        sendNotificationButton.setOnClickListener(v -> sendNotification());

        return view;
    }

    private void observeSelectedEntrants() {
        entrantViewModel.getFilteredSelected(currentFilter).observe(getViewLifecycleOwner(), entrants -> {
            adapter.updateEntrants(entrants);

            String filterText = currentFilter == null ? "All" :
                    currentFilter == Entrant.Status.ENROLLED ? "Enrolled" : "Cancelled";
            titleText.setText("Selected Entrants (" + entrants.size() + ") - " + filterText);
        });
    }

    private void updateFilterButtons() {
        // Reset all to inactive style
        filterAllButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
        filterEnrolledButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
        filterCancelledButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));

        // Set active button to primary color
        if (currentFilter == null) {
            filterAllButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.black, null));
        } else if (currentFilter == Entrant.Status.ENROLLED) {
            filterEnrolledButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.black, null));
        } else if (currentFilter == Entrant.Status.CANCELLED) {
            filterCancelledButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.black, null));
        }
    }

    // US 02.06.04 - Cancel entrant
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

    // US 02.05.03 - Draw replacement
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

    // US 02.06.05 - Export CSV
    private void exportToCsv() {
        String filterText = currentFilter == null ? "all selected" :
                currentFilter == Entrant.Status.ENROLLED ? "enrolled" : "cancelled";
        Toast.makeText(getContext(),
                "📥 Exporting " + filterText + " entrants to CSV...",
                Toast.LENGTH_SHORT).show();

        // TODO: Implement actual CSV export
    }

    // US 02.07.02 - Send notification
    private void sendNotification() {
        String filterText = currentFilter == null ? "all selected" :
                currentFilter == Entrant.Status.ENROLLED ? "enrolled" : "cancelled";
        Toast.makeText(getContext(),
                "📤 Sending notification to " + filterText + " entrants...",
                Toast.LENGTH_SHORT).show();

        // TODO: Navigate to send notification screen
    }
}