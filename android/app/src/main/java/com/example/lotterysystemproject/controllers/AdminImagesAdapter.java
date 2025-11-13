package com.example.lotterysystemproject.controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.lotterysystemproject.R;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminImagesAdapter is a adapter used by administrators to
 * browse, select, and manage image stored in Firebase Storage.
 *
 * This adapter displays a grid of images, allows multiple selection for deletion,
 * and notifies listeners when changing the selection (from deletion to unselecting images or vice versa).
 *
 * - Displays images from URLs using the Glide image loading library.
 * - Supports multi-selection of images.
 *
 */
public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.ImagesViewHolder>{

    /** The application or activity context used for inflating layouts and loading images. */
    private final Context context;

    /** List of all image URLs displayed in the RecyclerView. */
    private final List<String> imageUrls;

    /** List of currently selected image URLs. */
    private final List<String> selectedImages = new ArrayList<>();

    /**
     * Callback interface used to report selection changes to the parent fragment or activity.
     */
    public interface OnSelectionChangedListener {

        /**
         * Called whenever the number of selected images changes.
         *
         * @param count The current number of selected images.
         */
        void onSelectionChanged(int count);
    }


    /** Listener to notify when the number of selected images changes. */
    private OnSelectionChangedListener selectionListener;


    /**
     * Sets a listener to be notified when image selections change.
     *
     * @param listener The OnSelectionChangedListener instance to register.
     */
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }


    /**
     * Constructs a new AdminImagesAdapter.
     *
     * @param context   The context used for inflating layouts and loading images.
     * @param imageUrls The list of image URLs to display.
     */
    public AdminImagesAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }


    /**
     * Returns the list of currently selected image URLs.
     *
     * @return A List of selected image URL strings.
     */
    public List<String> getSelectedImages() {
        return selectedImages;
    }


    /**
     * ImagesViewHolder represents a single image item view in the RecyclerView.
     *
     * Each ViewHolder holds a reference to an ImageView displaying one image.
     *
     */
    public class ImagesViewHolder extends RecyclerView.ViewHolder {

        /** The ImageView used to display the image thumbnail. */
        ImageView imageView;

        /**
         * Creates a new ImagesViewHolder and binds its view reference.
         *
         * @param itemView The inflated layout for a single image item.
         */
        public ImagesViewHolder (@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }


    /**
     * Inflates the layout for an image item and returns a new ImagesViewHolder.
     *
     * @param viewGroup The parent ViewGroup that the view will be attached to.
     * @param viewType  The type of the view.
     * @return A new ImagesViewHolder for the item view.
     */
    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_image, viewGroup, false);
        return new ImagesViewHolder(view);

    }


    /**
     * Binds the image data (from a URL) to the corresponding ImagesViewHolder.
     *
     * Uses Glide for asynchronous image loading and applies a visual transparency effect
     * if the image is currently selected. Tapping the image/item    toggles its selection state.
     *
     * @param viewHolder The holder for the current item view.
     * @param position   The position of the image in the dataset.
     */
    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder viewHolder, final int position) {
        String imageUrl = imageUrls.get(position);

        // Load image with Glide
        Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .into(viewHolder.imageView);

        // Apply transparency if selected
        viewHolder.itemView.setAlpha(selectedImages.contains(imageUrl) ? 0.5f : 1.0f);

        // Toggle selection on click
        viewHolder.itemView.setOnClickListener(v -> toggleSelection(imageUrl));
    }


    /**
     * Toggles the selection state of the given image URL.
     *
     * If the image is already selected, it is deselected; otherwise, it is added to the
     * selection list. This method also notifies the selection listener and refreshes the adapter.
     *
     *
     * @param imageUrl The URL of the image whose selection state is to be toggled.
     */
    private void toggleSelection(String imageUrl) {
        if (selectedImages.contains(imageUrl)) {
            selectedImages.remove(imageUrl);
        } else {
            selectedImages.add(imageUrl);
        }


        // Notify the listener of the updated selection count
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedImages.size());
        }

        notifyDataSetChanged();

    }

    /**
     * Returns the total number of image items in the dataset.
     *
     * @return The total count of images.
     */
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }


}
