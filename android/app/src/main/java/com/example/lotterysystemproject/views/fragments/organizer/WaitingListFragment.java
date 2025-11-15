package com.example.lotterysystemproject.views.fragments.organizer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.lotterysystemproject.viewmodels.EntrantViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of entrants currently on the waiting list.
 * <p>
 * Provides search functionality and allows organizers to initiate
 * a draw lottery to move entrants into selected status based on
 * available event capacity.
 */
public class WaitingListFragment extends Fragment {

    private EntrantViewModel entrantViewModel;
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private Button drawLotteryButton;
    private EditText searchInput;
    private TextView titleText;

    private List<Entrant> allWaitingEntrants = new ArrayList<>();

    /**
     * Inflates the waiting list fragment layout and initializes UI components.
     *
     * @param inflater  the LayoutInflater object that can be used to inflate views
     * @param container the parent view that this fragment's UI should be attached to
     * @param savedInstanceState if non-null, contains previous state information
     * @return the root view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waiting_list, container, false);

        // Get shared ViewModel from parent activity
        entrantViewModel = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        // Initialize views
        recyclerView = view.findViewById(R.id.waiting_recycler_view);
        drawLotteryButton = view.findViewById(R.id.draw_lottery_button);
        searchInput = view.findViewById(R.id.search_input);
        titleText = view.findViewById(R.id.waiting_list_title);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EntrantAdapter(0, null); // 0 = TYPE_WAITING
        recyclerView.setAdapter(adapter);

        // Observe waiting list data
        entrantViewModel.getWaitingList().observe(getViewLifecycleOwner(), entrants -> {
            allWaitingEntrants = entrants;
            adapter.updateEntrants(entrants);
            titleText.setText("Waiting List (" + entrants.size() + ")");
        });

        // Handle draw lottery button click
        drawLotteryButton.setOnClickListener(v -> showDrawLotteryDialog());

        // Search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEntrants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    /**
     * Filters entrants in the waiting list based on the given query string.
     * Matches name or email fields (case-insensitive).
     *
     * @param query the text to filter entrants by
     */
    private void filterEntrants(String query) {
        if (query.isEmpty()) {
            adapter.updateEntrants(allWaitingEntrants);
            return;
        }

        List<Entrant> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Entrant e : allWaitingEntrants) {
            if (e.getName().toLowerCase().contains(lowerQuery) ||
                    e.getEmail().toLowerCase().contains(lowerQuery)) {
                filtered.add(e);
            }
        }

        adapter.updateEntrants(filtered);
    }

    /**
     * Displays a dialog to perform a lottery draw for entrants on the waiting list.
     * <p>
     * The dialog allows the organizer to select how many entrants will be moved
     * into the selected list, depending on available event capacity.
     */
    private void showDrawLotteryDialog() {
        if (allWaitingEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No entrants in waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Replace with dynamic value from event capacity
        int availableSpots = 20;

        DrawLotteryDialogFragment dialog = DrawLotteryDialogFragment.newInstance(
                allWaitingEntrants.size(),
                availableSpots
        );
        dialog.show(getParentFragmentManager(), "draw_lottery");
    }
}