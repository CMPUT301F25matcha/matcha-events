package com.example.lotterysystemproject.Views.Entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.Controllers.EventHistoryAdapter;
import com.example.lotterysystemproject.Models.EventHistoryItem;
import com.example.lotterysystemproject.R;

import java.util.Arrays;
import java.util.List;

public class EventRegistrationHistoryFragment extends Fragment {

    public EventRegistrationHistoryFragment() {}

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_registration_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mock data for RecyclerView/ListView
        List<EventHistoryItem> mockData = Arrays.asList(
                new EventHistoryItem("Piano Lessons", "Accepted", "2025-10-01 14:23"),
                new EventHistoryItem("Cooking Workshop", "Declined", "2025-09-27 09:15"),
                new EventHistoryItem("Beginner Swimming Lessons", "Pending", "2025-09-15 17:40"),
                new EventHistoryItem("Art Exhibition", "Cancelled", "2025-08-01 18:00")
        );

        RecyclerView recycler = view.findViewById(R.id.history_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(new EventHistoryAdapter(mockData));

        Button backToProfile = view.findViewById(R.id.btn_back_to_profile);
        backToProfile.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }
}
