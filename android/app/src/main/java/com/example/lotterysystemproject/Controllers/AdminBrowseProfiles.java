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

public class AdminBrowseProfiles extends Fragment {

    private AdminBrowseProfilesBinding binding;
    private AdminProfilesAdapter adapter;
    private final List<User> userList = new ArrayList<>();
    private final List<User> allUsers = new ArrayList<>();
    private FirebaseFirestore db;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AdminBrowseProfilesBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

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


        // Setup search bar
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

        loadProfiles();


    }


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