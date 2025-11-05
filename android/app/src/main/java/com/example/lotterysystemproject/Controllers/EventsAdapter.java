package com.example.lotterysystemproject.Controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lotterysystemproject.Models.Event;

import com.example.lotterysystemproject.R;

import java.util.List;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
    private final Context context;
    private List<Event> events;

    public EventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventName, organizerName;
        Button viewEventButton;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_image);
            eventName = itemView.findViewById(R.id.event_name);
            organizerName = itemView.findViewById(R.id.organizer_name);
            viewEventButton = itemView.findViewById(R.id.btn_view_event);
        }

    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_event, viewGroup, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder viewHolder, final int position) {
        Event event = events.get(position);

        viewHolder.eventName.setText(event.getName());
        viewHolder.organizerName.setText(event.get);


        // Handle "View Event" button click
        viewHolder.viewEventButton.setOnClickListener(v -> {
            // Show Event Dialog
            EventsDialog dialog =

        });

    }





}