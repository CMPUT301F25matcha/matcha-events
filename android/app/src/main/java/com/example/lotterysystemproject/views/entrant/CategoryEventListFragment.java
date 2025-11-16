package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.adapters.CategoryEventAdapter;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.models.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fragment equivalent of CategoryEventListActivity. Can be hosted by the activity if needed.
 */
public class CategoryEventListFragment extends Fragment {

    public static final String ARG_CATEGORY = "category";

    private EventRepository eventRepository;
    private RecyclerView recyclerView;
    private CategoryEventAdapter adapter;
    private Button btnSortRecency;
    private Button btnSortLocation;

    private String category;
    private List<Event> currentEvents = new ArrayList<>();

    public static CategoryEventListFragment newInstance(String category) {
        CategoryEventListFragment f = new CategoryEventListFragment();
        Bundle b = new Bundle();
        b.putString(ARG_CATEGORY, category);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        eventRepository = com.example.lotterysystemproject.firebasemanager.RepositoryProvider.getEventRepository();

        recyclerView = view.findViewById(R.id.recycler_category_events);
        btnSortRecency = view.findViewById(R.id.btn_sort_recency);
        btnSortLocation = view.findViewById(R.id.btn_sort_location);

        adapter = new CategoryEventAdapter(new ArrayList<>(), event -> {
            if (event != null && event.getId() != null) {
                startActivity(new android.content.Intent(getActivity(), EventDetailsActivity.class)
                        .putExtra("eventId", event.getId()));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
        } else {
            category = "";
        }

        btnSortRecency.setOnClickListener(v -> sortByRecency());
        btnSortLocation.setOnClickListener(v -> sortByLocationPlaceholder());

        loadCategoryEvents();
    }

    private void loadCategoryEvents() {
        eventRepository.getEventsByCategory(category,
                (Consumer<List<Event>>) events -> requireActivity().runOnUiThread(() -> {
                    if (events == null) {
                        Toast.makeText(requireContext(), "No events found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentEvents.clear();
                    currentEvents.addAll(events);
                    adapter.update(events);
                }),
                (Consumer<Exception>) e -> requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show())
        );
    }

    private void sortByRecency() {
        Collections.sort(currentEvents, (a, b) -> {
            if (a.getEventDate() == null && b.getEventDate() == null) return 0;
            if (a.getEventDate() == null) return 1;
            if (b.getEventDate() == null) return -1;
            return b.getEventDate().compareTo(a.getEventDate());
        });
        adapter.update(currentEvents);
    }

    private void sortByLocationPlaceholder() {
        Collections.sort(currentEvents, Comparator.comparing(e -> {
            String loc = e.getLocation();
            return loc == null ? "" : loc;
        }));
        adapter.update(currentEvents);
    }
}
