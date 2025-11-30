package com.example.lotterysystemproject.controllers;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterysystemproject.adapters.NotificationLogsAdapter;
import com.example.lotterysystemproject.databinding.AdminNotificationLogsBinding;
import com.example.lotterysystemproject.firebasemanager.RepositoryCallback;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.NotificationItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminNotificationLogs extends Fragment {

    private AdminNotificationLogsBinding binding;
    private NotificationLogsAdapter adapter;
    private List<NotificationItem> allNotifications = new ArrayList<>();
    private List<NotificationItem> filteredNotifications = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AdminNotificationLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerLogs.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NotificationLogsAdapter(filteredNotifications);
        binding.recyclerLogs.setAdapter(adapter);

        binding.backArrow.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminNotificationLogs.this).navigateUp()
        );

        TextInputEditText searchEventInput = binding.searchInput;
        searchEventInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filterNotifications(s.toString());

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        loadAllNotifications();

    }

    private void loadAllNotifications() {
        RepositoryProvider.getNotificationRepository().getAllNotifications(
                new RepositoryCallback<List<NotificationItem>>() {

                    @Override
                    public void onSuccess(List<NotificationItem> notifications) {
                        allNotifications.clear();
                        allNotifications.addAll(notifications);

                        // Sync filtered list initially
                        filteredNotifications.clear();
                        filteredNotifications.addAll(notifications);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void filterNotifications(String query) {
        filteredNotifications.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredNotifications.addAll(allNotifications);
        } else {
            String lower = query.toLowerCase();

            for (NotificationItem item : allNotifications) {
                if (item.getTitle().toLowerCase().contains(lower) ||
                        item.getMessage().toLowerCase().contains(lower) ||
                        item.getUserId().toLowerCase().contains(lower)) {

                    filteredNotifications.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }


}
