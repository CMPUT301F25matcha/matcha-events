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


/**
 * AdminBrowseImages is a Fragment that allows administrators to
 * view, search, and delete images stored in Firebase Storage.
 *
 * This component retrieves all image URLs from Firebase through FirebaseManager,
 * displays them in a grid using AdminImagesAdapter, and enables multiple deletion
 * of selected images with user confirmation.
 *
 * - Display all uploaded images in a grid layout.
 * - Filter images by filename using a search bar.
 * - Select and delete multiple images with confirmation dialogs.
 *
 */
public class AdminBrowseImages extends Fragment {

    /** View binding for the admin browse images layout. */
    private AdminBrowseImagesBinding binding;

    /** Adapter responsible for displaying image and handling selections. */
    private AdminImagesAdapter adapter;


    /** List of currently visible image URLs shown in the RecyclerView. */
    private final List<String> imageUrls = new ArrayList<>();


    /** Full list of all available image URLs retrieved from Firebase. */
    private final List<String> allImagesUrls = new ArrayList<>();


    private FirebaseManager firebaseManager;



    /**
     * Called to inflate the fragment layout and initialize the Firebase manager.
     *
     * @param inflater  The LayoutInflater used to inflate views in the fragment.
     * @param container The parent ViewGroup into which the fragment's UI should be attached.
     * @param savedInstanceState If not null, the fragment is being re-created from a previous state.
     * @return The root view of the inflated layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AdminBrowseImagesBinding.inflate(inflater, container, false);
        firebaseManager = FirebaseManager.getInstance(); // initialize Firebase model
        return binding.getRoot();
    }



    /**
     * Called once the fragment's view has been created.
     *
     * This method sets up the RecyclerView, search functionality,
     * back navigation, and Firebase image fetching.
     *
     * @param view The created view.
     * @param savedInstanceState The saved state of the fragment, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView as Grid
        binding.recyclerImages.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new AdminImagesAdapter(getContext(), imageUrls);
        binding.recyclerImages.setAdapter(adapter);

        // Back button navigation
        binding.backArrow.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminBrowseImages.this).navigateUp()
        );

        // Show delete icon when one or more images are selected
        adapter.setOnSelectionChangedListener(count ->
                binding.deleteIcon.setVisibility(count > 0 ? View.VISIBLE : View.GONE)
        );

        // Handle delete icon click
        binding.deleteIcon.setOnClickListener(v -> confirmDeleteImages());

        // Fetch images from Firebase
        fetchImages();

        // Setup search filter
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

    /**
     * Retrieves all images from Firebase Storage.
     *
     * Populates imageUrls and allImagesUrls lists with the retrieved URLs
     * and updates the RecyclerView adapter.
     *
     */
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


    /**
     * Filters the list of images based on the user's search query.
     *
     * Matching is performed by comparing the lowercase filename
     * portion of each image URL against the lowercase query text.
     *
     * @param query The search term entered by the user.
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
     * Displays a confirmation dialog before deleting selected images.
     *
     * If the user confirms, the method delegates to deleteImages(List)
     *
     */
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


    /**
     * Deletes the  list of selected images from Firebase Storage.
     *
     * Uses deleteMultipleImages from FirebaseManage to perform the deletion asynchronously,
     * updates the lists, and refreshes the UI.
     *
     * @param selectedImages A list of image URLs selected for deletion.
     */
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
