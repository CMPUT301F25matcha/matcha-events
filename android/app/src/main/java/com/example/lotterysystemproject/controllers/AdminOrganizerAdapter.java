package com.example.lotterysystemproject.controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.User;

import org.jetbrains.annotations.NonNls;

public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.OrganizerViewHolder> {

    private final Context context;
    private final List<User> organizers;

    public AdminOrganizerAdapter(Context context, List<User> organizers) {
        this.context = context;
        this.organizers = organizers;
    }

    public class OrganizerViewHolder extends RecyclerView.ViewHolder {

        TextView organizerName;
        TextView organizerEmail;
        Button viewOrganizer;


        public OrganizerViewHolder(View itemView) {
            super(itemView);
            organizerName = itemView.findViewById(R.id.profile_name);
            organizerEmail = itemView.findViewById(R.id.profile_email);
            viewOrganizer = itemView.findViewById(R.id.btn_view_organizer);
        }

    }

    @NonNull
    @Override
    public AdminOrganizerAdapter.OrganizerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_organizer, viewGroup, false);
        return new OrganizerViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrganizerAdapter.OrganizerViewHolder viewHolder, final int position) {
        User organizer = organizers.get(position);

        viewHolder.organizerName.setText(organizer.getName());
        viewHolder.organizerEmail.setText(organizer.getEmail());

        viewHolder.viewOrganizer.setOnClickListener(v -> {

            AdminOrganizerDialog dialog = new AdminOrganizerDialog(organizer);
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "OrganizerDialog");

        });


    }

    @Override
    public int getItemCount() {
        return organizers != null ? organizers.size() : 0;
    }




}