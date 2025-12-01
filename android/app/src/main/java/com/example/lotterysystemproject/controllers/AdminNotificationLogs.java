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

import com.example.lotterysystemproject.controllers.NotificationLogsAdapter;
import com.example.lotterysystemproject.databinding.AdminNotificationLogsBinding;
import com.example.lotterysystemproject.firebasemanager.RepositoryCallback;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.NotificationItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying and managing the admin's notification logs.
 * <p>
 * This fragment loads all notifications from the repository, displays them in a
 * RecyclerView, and provides real-time search filtering based on notification
 * title, message, or user ID.
 * </p>
 */
public class AdminNotificationLogs extends Fragment {

    private AdminNotificationLogsBinding binding;
    private NotificationLogsAdapter adapter;
    private List<NotificationItem> allNotifications = new ArrayList<>();
    private List<NotificationItem> filteredNotifications = new ArrayList<>();

    /**
     * Inflates the layout for the Admin Notification Logs screen.
     *
     * @param inflater  Layout inflater used to inflate views
     * @param container Parent view that the fragment's UI will attach to
     * @param savedInstanceState Saved instance state
     * @return The root view of the inflated binding
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AdminNotificationLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view is created. Sets up UI components such as the RecyclerView,
     * back button, search field listener, and triggers notification loading.
     *
     * @param view The created view
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationLogsAdapter(filteredNotifications);
        binding.recyclerLogs.setAdapter(adapter);

        // Navigate back on arrow click
        binding.backArrow.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminNotificationLogs.this).navigateUp()
        );

        // Attach search listener
        TextInputEditText searchEventInput = binding.searchInput;
        searchEventInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filterNotifications(s.toString());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        loadAllNotifications();
    }

    /**
     * Loads all notifications from the Firebase repository.
     * <p>
     * On success, both the master list ({@code allNotifications}) and the
     * filtered list shown in the UI ({@code filteredNotifications}) are refreshed.
     * </p>
     */
    private void loadAllNotifications() {
        RepositoryProvider.getNotificationRepository().getAllNotifications(
                new RepositoryCallback<List<NotificationItem>>() {

                    @Override
                    public void onSuccess(List<NotificationItem> notifications) {
                        allNotifications.clear();
                        allNotifications.addAll(notifications);

                        filteredNotifications.clear();
                        filteredNotifications.addAll(notifications);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(),
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Filters the list of notifications based on a search query.
     * <p>
     * Matches are performed case-insensitively against:
     * <ul>
     *     <li>Notification title</li>
     *     <li>Notification message</li>
     *     <li>User ID</li>
     * </ul>
     * If the query is empty, the full list is restored.
     * </p>
     *
     * @param query The text input used for filtering
     */
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
