package com.example.lotterysystemproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lotterysystemproject.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



public class ProfilesAdapter extends RecyclerView.Adapter<ProfilesAdapter.ProfileViewHolder> {

    private final Context context;
    private List<User> users;

    public ProfilesAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView name, email;
        Button viewDetails;

        public ProfileViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            viewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_profile, viewGroup, false);

        return new ProfileViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder viewHolder, final int position) {
        User user = users.get(position);

        viewHolder.name.setText(user.getName());
        viewHolder.email.setText(user.getEmail());


    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

}