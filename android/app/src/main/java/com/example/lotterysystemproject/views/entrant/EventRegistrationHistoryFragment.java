package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.controllers.EventHistoryAdapter;
import com.example.lotterysystemproject.models.DeviceIdentityManager;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.models.EventHistoryItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Date;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the entrant’s registration history for past and current events.
 * Data is loaded from Firestore based on the current device user.
 */
public class EventRegistrationHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventHistoryAdapter adapter;
    private final List<EventHistoryItem> historyItems = new ArrayList<>();

    private TextView emptyState;
    private ProgressBar progress;

    private FirebaseFirestore db;
    private ListenerRegistration entrantsListener;

    public EventRegistrationHistoryFragment() {
        // Required empty constructor
    }

    /**
     * Inflates the layout for the registration history screen.
     *
     * @param inflater LayoutInflater used to inflate view hierarchy
     * @param container parent ViewGroup containing this fragment's view
     * @param savedInstanceState previously saved state (if any)
     * @return root View for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Re-use the existing layout
        return inflater.inflate(R.layout.activity_registration_history, container, false);
    }

    /**
     * Sets up RecyclerView, back button, and listens to Firestore for current user's entrant records.
     *
     * @param view root view returned by onCreateView
     * @param savedInstanceState previous instance state
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.history_recycler);
        emptyState   = view.findViewById(R.id.empty_state);
        progress     = view.findViewById(R.id.progress);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventHistoryAdapter(historyItems);
        recyclerView.setAdapter(adapter);

        // Back button: return to profile
        Button backToProfile = view.findViewById(R.id.btn_back_to_profile);
        if (backToProfile != null) {
            backToProfile.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager().popBackStack()
            );
        }

        // Load history for current user
        String currentUserId = DeviceIdentityManager.getUserId(requireContext());
        listenForHistory(currentUserId);
    }

    /**
     * Attaches a Firestore listener for all entrants belonging to this user
     * and builds EventHistoryItems from entrant + event data.
     */
    private void listenForHistory(String userId) {
        if (userId == null || userId.isEmpty()) {
            showEmpty();
            return;
        }

        showLoading();

        // Listen to all entrants for this user
        entrantsListener = db.collection("entrants")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (!isAdded()) return; // fragment no longer attached

                    hideLoading();

                    historyItems.clear();

                    // Error or no results = empty state
                    if (error != null || snapshots == null || snapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        showEmpty();
                        return;
                    }
                    buildHistoryFromEntrants(snapshots);
                });
    }

    /**
     * Builds the EventHistoryItem list by:
     *  - reading entrant docs for this user
     *  - fetching each related event to get the event name
     */
    private void buildHistoryFromEntrants(@NonNull QuerySnapshot snapshots) {
        final List<Entrant> entrantList = new ArrayList<>();
        final List<Task<DocumentSnapshot>> eventTasks = new ArrayList<>();
        final List<DocumentSnapshot> entrantDocs = new ArrayList<>();

        // Collect entrants + prepare event fetch operations
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            Entrant entrant = doc.toObject(Entrant.class);
            if (entrant == null) continue;

            // Ensure Firestore document id preservation
            entrant.setId(doc.getId());

            String eventId = entrant.getEventId();
            if (eventId == null || eventId.isEmpty()) continue;

            entrantList.add(entrant);
            entrantDocs.add(doc);
            eventTasks.add(db.collection("events").document(eventId).get());
        }

        // If no events show empty state
        if (eventTasks.isEmpty()) {
            historyItems.clear();
            adapter.notifyDataSetChanged();
            showEmpty();
            return;
        }

        // Wait for all event lookups to finish
        Tasks.whenAllSuccess(eventTasks)
                .addOnSuccessListener(results -> {
                    historyItems.clear();

                    DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(requireContext());

                    for (int i = 0; i < results.size(); i++) {
                        DocumentSnapshot eventDoc = (DocumentSnapshot) results.get(i);
                        Entrant entrant = entrantList.get(i);
                        DocumentSnapshot entrantDoc = entrantDocs.get(i);

                        // Event name (fallback to eventId if missing)
                        String eventName = "Event";
                        if (eventDoc != null && eventDoc.exists()) {
                            String name = eventDoc.getString("name");
                            if (name != null && !name.isEmpty()) {
                                eventName = name;
                            }
                        }

                        // Status: use enum if present, otherwise string
                        String statusLabel;
                        if (entrant.getStatus() != null) {
                            switch (entrant.getStatus()) {
                                case WAITING:
                                    statusLabel = "On Waiting List";
                                    break;
                                case INVITED:
                                    statusLabel = "Invited";
                                    break;
                                case ENROLLED:
                                    statusLabel = "Enrolled";
                                    break;
                                case CANCELLED:
                                default:
                                    statusLabel = "Cancelled / Declined";
                                    break;
                            }
                        } else {
                            statusLabel = "Unknown";
                        }

                        // Use raw Firestore timestamps
                        Long statusTs = entrantDoc.getLong("statusTimestamp");
                        Long joinedTs = entrantDoc.getLong("joinedTimestamp");

                        long ts = 0L;
                        if (statusTs != null && statusTs > 0) {
                            ts = statusTs;
                        } else if (joinedTs != null && joinedTs > 0) {
                            ts = joinedTs;
                        }

                        String dateStr;
                        if (ts > 0) {
                            dateStr = dateFormat.format(new Date(ts));
                        } else {
                            dateStr = "-";
                        }

                        // Create history entry
                        historyItems.add(
                                new EventHistoryItem(
                                        eventName,
                                        statusLabel,
                                        dateStr
                                )
                        );
                    }

                    adapter.notifyDataSetChanged();
                    updateVisibility();
                })
                .addOnFailureListener(e -> {
                    historyItems.clear();
                    adapter.notifyDataSetChanged();
                    showEmpty();
                });
    }

    /** Shows the loading spinner. */
    private void showLoading() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
    }

    /** Hides the loading spinner. */
    private void hideLoading() {
        if (progress != null) progress.setVisibility(View.GONE);
    }

    /** Shows empty-state text and hides RecyclerView. */
    private void showEmpty() {
        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
    }

    /** Updates which views are visible depending on if history present. */
    private void updateVisibility() {
        if (historyItems.isEmpty()) {
            showEmpty();
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Removes the Firestore listener when view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (entrantsListener != null) {
            entrantsListener.remove();
            entrantsListener = null;
        }
    }
}
