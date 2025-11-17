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


        loadOrganizers();
    }

    private void loadOrganizers() {
        adminRepository.getAllOrganizers(organizers -> {
            allOrganizers.clear();
            organizerList.clear();
            allOrganizers.addAll(organizers);
            organizerList.addAll(organizers);
            adapter.notifyDataSetChanged();
        }, e -> Log.e("FirestoreError", "Error loading organizers", e));
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
