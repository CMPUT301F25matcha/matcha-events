package com.example.lotterysystemproject.controllers;

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

import com.bumptech.glide.Glide;
import com.example.lotterysystemproject.models.Event;

import com.example.lotterysystemproject.R;

import java.util.List;


/**
 * AdminEventsAdapter is a RecyclerView adapter responsible for displaying
 * a list of Event objects in the admin browse events interface.
 *
 * Each event is shown as a card containing its name, image, and a button to
 * view more details through a dialog.
 *
 * - Binds event data to the RecyclerView layout items.</li>
 * - Handles button clicks to display detailed event information.</li>
 *
 */
public class AdminEventsAdapter extends RecyclerView.Adapter<AdminEventsAdapter.EventViewHolder> {

    /** The application or activity context used for layout inflation and dialog display. */
    private final Context context;

    /** The list of Event objects displayed in the RecyclerView. */
    private List<Event> events;


    /**
     * Constructs a new AdminEventsAdapter
     *
     * @param context The context used to access resources and the fragment manager.
     * @param events  The list of events to display in the RecyclerView.
     */
    public AdminEventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    /**
     * EventViewHolder represents a single item within the RecyclerView.
     *
     * It holds references to the layout's UI components and is reused as the user scrolls
     * through the list.
     *
     */
    public class EventViewHolder extends RecyclerView.ViewHolder {

        /** The image associated with the event. */
        ImageView eventImage;


        /** The name of the event. */
        TextView eventName;

        TextView organizerName;

        /** Button allowing the admin to view event details. */
        Button viewEventButton;

        /**
         * Creates a new EventViewHolder instance and binds its views.
         *
         * @param itemView The view representing a single event card layout.
         */

        public EventViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_image);
            eventName = itemView.findViewById(R.id.event_name);
            organizerName = itemView.findViewById(R.id.organizer_name);
            viewEventButton = itemView.findViewById(R.id.btn_view_event);
        }

    }

    /**
     * Inflates the layout for a single RecyclerView item and creates a new EventViewHolder.
     *
     * @param viewGroup The parent ViewGroup into which the new view will be added.
     * @param viewType  The type of view to create.
     * @return A new  EventViewHolder instance that contains the inflated view.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.admin_item_event_card, viewGroup, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds data from a specific Event object to a EventViewHolder.
     *
     * Sets the event name and handles the click event for the "View Event" button,
     * which opens an AdminEventsDialog with detailed information about event.
     *
     *
     * @param viewHolder The ViewHolder representing the current list item.
     * @param position The position of the event within the data list.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder viewHolder, final int position) {
        Event event = events.get(position);

        viewHolder.eventName.setText(event.getName());
        // Set organizer name with "Hosted by" prefix
        String organizerText = "Hosted by " + event.getHostName();
        viewHolder.organizerName.setText(organizerText);


        if (event.getPosterImageUrl() != null && !event.getPosterImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(event.getPosterImageUrl())
                    .centerCrop()
                    .into(viewHolder.eventImage);

        } else {
            viewHolder.eventImage.setImageResource(R.drawable.ic_launcher_background);
        }
        // Handle "View Event" button click
        viewHolder.viewEventButton.setOnClickListener(v -> {
            // Show Event Dialog
            AdminEventsDialog dialog = new AdminEventsDialog(event);
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "EventsDialog");

        });

    }

    /**
     * Returns the total number of events currently in the adapter's dataset.
     *
     * @return The number of Event items displayed in the RecyclerView.
     */
    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }







}