package com.example.lotterysystemproject.Controllers;

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


public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.ImagesViewHolder>{
    private final Context context;
    private final List<String> imageUrls;
    private final List<String> selectedImages = new ArrayList<>();

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    private OnSelectionChangedListener selectionListener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    public AdminImagesAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    public List<String> getSelectedImages() {
        return selectedImages;
    }

    public class ImagesViewHolder  extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImagesViewHolder (@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }


    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_image, viewGroup, false);
        return new ImagesViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder viewHolder, final int position) {
        String imageUrl = imageUrls.get(position);

        // Load image with Glide
        Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .into(viewHolder.imageView);

        // Change appearance when selected
        viewHolder.itemView.setAlpha(selectedImages.contains(imageUrl) ? 0.5f : 1.0f);

        // Toggle selection on click
        viewHolder.itemView.setOnClickListener(v -> toggleSelection(imageUrl));
    }

    private void toggleSelection(String imageUrl) {
        if (selectedImages.contains(imageUrl)) {
            selectedImages.remove(imageUrl);
        } else {
            selectedImages.add(imageUrl);
        }

        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedImages.size());
        }

        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }


}
