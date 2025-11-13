package com.example.lotterysystemproject.controllers;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.lotterysystemproject.firebasemanager.AdminRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.databinding.AdminBrowseImagesBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseImages extends Fragment {
    private AdminBrowseImagesBinding binding;
    private AdminRepository adminRepository;
    private AdminImagesAdapter adapter;
    private final List<String> imageUrls = new ArrayList<>();
    private final List<String> allImagesUrls = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AdminBrowseImagesBinding.inflate(inflater, container, false);
        adminRepository = RepositoryProvider.getAdminRepository();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView as Grid
        binding.recyclerImages.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new AdminImagesAdapter(getContext(), imageUrls);
        binding.recyclerImages.setAdapter(adapter);

        // Back button
        binding.backArrow.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminBrowseImages.this).navigateUp()
        );

        // Show delete icon when something is selected
        adapter.setOnSelectionChangedListener(count ->
                binding.deleteIcon.setVisibility(count > 0 ? View.VISIBLE : View.GONE)
        );

        // Delete selected images
        binding.deleteIcon.setOnClickListener(v -> confirmDeleteImages());

        // Search filter listener
        TextInputEditText searchInput = binding.searchInput;
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterImages(s.toString());
            }
        });

        // Load images from repository
        loadImages();
    }

    /**
     * Fetch all images from Firebase Storage via AdminRepository
     */
    private void loadImages() {
        adminRepository.getAllImages(
                urls -> {
                    imageUrls.clear();
                    allImagesUrls.clear();
                    imageUrls.addAll(urls);
                    allImagesUrls.addAll(urls);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(),
                            "Failed to load images", Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Filter images by filename
     */
    private void filterImages(String query) {
        imageUrls.clear();

        if (query.isEmpty()) {
            imageUrls.addAll(allImagesUrls);
        } else {
            for (String url : allImagesUrls) {
                String fileName = url.substring(url.lastIndexOf('/') + 1).toLowerCase();
                if (fileName.contains(query.toLowerCase())) {
                    imageUrls.add(url);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Confirm deletion before proceeding
     */
    private void confirmDeleteImages() {
        List<String> selectedImages = adapter.getSelectedImages();
        if (selectedImages.isEmpty()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Images")
                .setMessage("Are you sure you want to delete " + selectedImages.size() + " image(s)?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSelectedImages(selectedImages))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Delete selected images from Firebase Storage using AdminRepository
     */
    private void deleteSelectedImages(List<String> selectedImages) {
        adminRepository.deleteMultipleImages(selectedImages, (deletedCount, error) -> {
            if (error == null) {
                // Remove deleted images from lists
                imageUrls.removeAll(selectedImages);
                allImagesUrls.removeAll(selectedImages);

                // Clear selection and update UI
                adapter.getSelectedImages().clear();
                adapter.notifyDataSetChanged();
                binding.deleteIcon.setVisibility(View.GONE);

                Toast.makeText(requireContext(),
                        deletedCount + " image(s) deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(),
                        "Error deleting images: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
