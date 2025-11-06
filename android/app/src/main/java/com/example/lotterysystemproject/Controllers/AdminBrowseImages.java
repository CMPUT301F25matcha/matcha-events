package com.example.lotterysystemproject.Controllers;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.lotterysystemproject.databinding.AdminBrowseImagesBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseImages extends Fragment {

    private AdminBrowseImagesBinding binding;
    private AdminImagesAdapter adapter;
    private final List<String> imageUrls = new ArrayList<>();
    private final List<String> allImagesUrls = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AdminBrowseImagesBinding.inflate(inflater, container, false);
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
        adapter.setOnSelectionChangedListener(count -> {
            binding.deleteIcon.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        });

        binding.deleteIcon.setOnClickListener(v -> confirmDeleteImages());

        fetchImagesFromStorage();

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
                filterImages(s.toString());
            }
        });

    }

    private void fetchImagesFromStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("images");

        storageReference.listAll()
                .addOnSuccessListener(listResult -> {
                    imageUrls.clear();
                    allImagesUrls.clear();

                    for (StorageReference item: listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            String url = uri.toString();
                            imageUrls.add(url);
                            allImagesUrls.add(url);
                            adapter.notifyDataSetChanged();
                        });
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void filterImages(String query) {
        imageUrls.clear();

        if (query.isEmpty()) {
            imageUrls.addAll(allImagesUrls);
        } else {
            for (String url: allImagesUrls) {
                // Extract file name from URL
                String fileName = url.substring(url.lastIndexOf('/') + 1).toLowerCase();
                if (fileName.contains(query.toLowerCase())) {
                    imageUrls.add(url);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void confirmDeleteImages() {
        List<String> selectedImages = adapter.getSelectedImages();

        if (selectedImages.isEmpty()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Images")
                .setMessage("Are you sure you want to delete the selected images?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImagesFromStorage(selectedImages))
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();

    }

    private void deleteImagesFromStorage(List<String> selectedImages) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        for (String url: selectedImages) {
            StorageReference imageRef = storage.getReferenceFromUrl(url);

            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        imageUrls.remove(url);
                        allImagesUrls.remove(url);
                        adapter.getSelectedImages().remove(url);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(getContext(), "Image deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to delete image", Toast.LENGTH_SHORT).show();
                    });
        }

        binding.deleteIcon.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }






}
