package com.example.lotterysystemproject.Controllers;

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

import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.databinding.AdminBrowseImagesBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseImages extends Fragment {

    private AdminBrowseImagesBinding binding;
    private AdminImagesAdapter adapter;
    private final List<String> imageUrls = new ArrayList<>();
    private final List<String> allImagesUrls = new ArrayList<>();

    // Model layer reference
    private FirebaseManager firebaseManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AdminBrowseImagesBinding.inflate(inflater, container, false);
        firebaseManager = FirebaseManager.getInstance(); // initialize Firebase model
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

        // Fetch images from Firebase (through model)
        fetchImages();

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
    }

    /** Fetch all images from Firebase Storage via FirebaseManager */
    private void fetchImages() {
        firebaseManager.getAllImages(urls -> {
            imageUrls.clear();
            allImagesUrls.clear();
            imageUrls.addAll(urls);
            allImagesUrls.addAll(urls);
            adapter.notifyDataSetChanged();
        }, e -> {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show();
        });
    }

    /** Filter images by name */
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

    /** Confirm deletion before proceeding */
    private void confirmDeleteImages() {
        List<String> selectedImages = adapter.getSelectedImages();
        if (selectedImages.isEmpty()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Images")
                .setMessage("Are you sure you want to delete the selected images?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImages(selectedImages))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /** Delete selected images from Firebase Storage using FirebaseManager */
    private void deleteImages(List<String> selectedImages) {
        firebaseManager.deleteMultipleImages(selectedImages, (deletedCount, e) -> {
            if (e == null) {
                imageUrls.removeAll(selectedImages);
                allImagesUrls.removeAll(selectedImages);
                adapter.getSelectedImages().clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Deleted " + deletedCount + " image(s)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error deleting images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            binding.deleteIcon.setVisibility(View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
