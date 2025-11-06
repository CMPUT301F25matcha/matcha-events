package com.example.lotterysystemproject.Controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Models.User;


public class AdminProfilesAdapter extends RecyclerView.Adapter<AdminProfilesAdapter.ProfileViewHolder> {

    private final Context context;
    private List<User> users;

    public AdminProfilesAdapter(Context context, List<User> users) {
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
            //role = itemView.findViewById(R.id.dialog_role);
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
        //viewHolder.role.setText(user.getRole());

        // Handle "View Details" button click
        viewHolder.viewDetails.setOnClickListener(v -> {
            // Show UserProfileDialog
            AdminUserProfileDialog dialog = new AdminUserProfileDialog(user);
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "UserProfileDialog");
        });

    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

}