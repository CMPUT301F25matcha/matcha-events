package com.example.lotterysystemproject.Controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.example.lotterysystemproject.Models.EventAdmin;

import com.example.lotterysystemproject.R;

import java.util.List;



public class AdminEventsAdapter extends RecyclerView.Adapter<AdminEventsAdapter.EventViewHolder> {
    private final Context context;
    private List<EventAdmin> eventAdmins;

    public AdminEventsAdapter(Context context, List<EventAdmin> eventAdmins) {
        this.context = context;
        this.eventAdmins = eventAdmins;
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventName;
        Button viewEventButton;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_image);
            eventName = itemView.findViewById(R.id.event_name);
            //organizerName = itemView.findViewById(R.id.organizer_name);
            viewEventButton = itemView.findViewById(R.id.btn_view_event);
        }

    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.admin_item_event_card, viewGroup, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder viewHolder, final int position) {
        EventAdmin eventAdmin = eventAdmins.get(position);

        viewHolder.eventName.setText(eventAdmin.getName());
        //viewHolder.organizerName.setText(user.);


        // Handle "View Event" button click
        viewHolder.viewEventButton.setOnClickListener(v -> {
            // Show Event Dialog
            AdminEventsDialog dialog = new AdminEventsDialog(eventAdmin);
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "EventsDialog");

        });

    }

    @Override
    public int getItemCount() {
        return eventAdmins != null ? eventAdmins.size() : 0;
    }







}