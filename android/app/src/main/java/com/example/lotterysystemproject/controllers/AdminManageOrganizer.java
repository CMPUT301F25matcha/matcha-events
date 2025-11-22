package com.example.lotterysystemproject.controllers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterysystemproject.databinding.AdminManageOrganizersBinding;
import com.example.lotterysystemproject.firebasemanager.AdminRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.material.textfield.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;


public class AdminManageOrganizer extends Fragment {

    private AdminManageOrganizersBinding binding;
    private AdminOrganizerAdapter adapter;
    private final List<User> organizerList = new ArrayList<>();
    private final List<User> allOrganizers = new ArrayList<>();
    private AdminRepository adminRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AdminManageOrganizersBinding.inflate(inflater, container, false);
        adminRepository = RepositoryProvider.getAdminRepository();
        return binding.getRoot();
    }



    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerOrganizers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminOrganizerAdapter(requireContext(), organizerList);
        binding.recyclerOrganizers.setAdapter(adapter);

        // Back button
        binding.backArrow.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminManageOrganizer.this).navigateUp()
        );

        // Search bar
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
                filterOrganizers(s.toString());

            }
        });


        listenToOrganizers();
    }

    private void listenToOrganizers() {
        adminRepository.listenToAllOrganizers(organizers -> {
            allOrganizers.clear();
            organizerList.clear();
            allOrganizers.addAll(organizers);
            organizerList.addAll(organizers);
            adapter.notifyDataSetChanged();
        }, e -> Log.e("FirestoreError", "Error listening to organizers", e));
    }

    private void filterOrganizers(String query) {
        organizerList.clear();

        if (query.isEmpty()) {
            organizerList.addAll(allOrganizers);

        } else {
            for (User user: allOrganizers) {
                if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                    organizerList.add(user);
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