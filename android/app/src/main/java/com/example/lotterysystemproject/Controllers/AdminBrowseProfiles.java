package com.example.lotterysystemproject.Controllers;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterysystemproject.Models.User;
import com.example.lotterysystemproject.databinding.AdminBrowseProfilesBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import com.google.android.material.textfield.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;

/**
 *  AdminBrowseProfiles is a Fragment that allows administrators to view and
 * manage user profiles stored in Firebase.
 *
 * It displays a list of objects using RecyclerView and supports live updates from Firestore,
 * as well as filtering by username.
 *
 * - Search functionality for filtering users by name.
 * - Navigation back to the previous fragment.
 * - Can remove profile with confirmation dialog
 */
public class AdminBrowseProfiles extends Fragment {

    private AdminBrowseProfilesBinding binding;
    private AdminProfilesAdapter adapter;
    private final List<User> userList = new ArrayList<>();
    private final List<User> allUsers = new ArrayList<>();
    private FirebaseFirestore db;



    /**
     * Called to inflate the fragment layout and initialize the Firebase manager.
     *
     * @param inflater  The LayoutInflater used to inflate views in the fragment.
     * @param container The parent ViewGroup into which the fragment's UI should be attached.
     * @param savedInstanceState If not null, the fragment is being re-created from a previous state.
     * @return The root view of the inflated layout.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AdminBrowseProfilesBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    /**
     * Called once the fragment's view has been created.
     *
     * This method sets up the RecyclerView, search functionality,
     * back navigation, Firestore.
     *
     * @param view The created view.
     * @param savedInstanceState The saved state of the fragment, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        binding.recyclerProfiles.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter
        adapter = new AdminProfilesAdapter(getContext(), userList);

        binding.recyclerProfiles.setAdapter(adapter);

        // Handle back arrow
        binding.backArrow.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminBrowseProfiles.this).navigateUp()
        );


        // Setup search input for filtering users
        TextInputEditText searchInput = binding.searchInput;
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());

            }
        });

        // Load and listen for user profile changes
        loadProfiles();


    }


    /**
     * Loads all user profiles from the Firestore "users" collection and listens
     * for real-time updates.
     *
     * This method uses a snapshot listener so any changes in the Firestore collection
     * are automatically updated in the RecyclerView.
     *
     */
    private void loadProfiles() {
        db.collection("users").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.e("FirestoreError", "Listen failed", e);
                return;
            }
            if (queryDocumentSnapshots != null) {
                userList.clear();
                allUsers.clear();
                for (DocumentSnapshot doc: queryDocumentSnapshots) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        userList.add(user);
                        allUsers.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
                Log.d("FirestoreListener", "Updated users: " + userList.size());
            }
        });

    }


    /**
     * Filters the list of displayed users based on the given query string.
     *
     * The filtering is case-insensitive and matches any part of the user's name.
     *
     * @param query The text entered by the admin in the search bar.
     */
    private void filterUsers(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(allUsers);
        } else {
            for (User user: allUsers) {
                if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                    userList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }





}