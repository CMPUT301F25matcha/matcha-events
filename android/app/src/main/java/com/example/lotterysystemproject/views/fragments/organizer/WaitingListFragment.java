package com.example.lotterysystemproject.Views.fragments.organizer;

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

public class WaitingListFragment extends Fragment {

    private EntrantViewModel entrantViewModel;
    private RecyclerView recyclerView;
    private EntrantAdapter adapter;
    private Button drawLotteryButton;
    private EditText searchInput;
    private TextView titleText;

    private List<Entrant> allWaitingEntrants = new ArrayList<>();

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

        // Observe waiting list
        entrantViewModel.getWaitingList().observe(getViewLifecycleOwner(), entrants -> {
            allWaitingEntrants = entrants;
            adapter.updateEntrants(entrants);
            titleText.setText("Waiting List (" + entrants.size() + ")");
        });

        // Draw lottery button
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

    private void showDrawLotteryDialog() {
        if (allWaitingEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No entrants in waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get available spots from parent event
        int availableSpots = 20; // TODO: Get from event capacity - enrolled count

        DrawLotteryDialogFragment dialog = DrawLotteryDialogFragment.newInstance(
                allWaitingEntrants.size(),
                availableSpots
        );
        dialog.show(getParentFragmentManager(), "draw_lottery");
    }
}