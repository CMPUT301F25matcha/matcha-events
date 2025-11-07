package com.example.lotterysystemproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lotterysystemproject.Models.EventAdmin;
import com.example.lotterysystemproject.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying a list of events.
 * Provides click handling and formatted display for event details.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventAdmin> events;
    private OnEventClickListener listener;

    /**
     * Interface for handling event item clicks.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event card is clicked.
         *
         * @param event The clicked event.
         */
        void onEventClick(EventAdmin event);
    }

    /**
     * Constructs a new EventAdapter.
     *
     * @param events   Initial list of events to display.
     * @param listener Listener for event click actions.
     */
    public EventAdapter(List<EventAdmin> events, OnEventClickListener listener) {
        this.events = events != null ? events : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventAdmin event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Updates the adapter with a new list of events.
     *
     * @param newEvents Updated event list.
     */
    public void updateEvents(List<EventAdmin> newEvents) {
        this.events = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder representing an event item in the RecyclerView.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDateTime, enrollmentCount, eventStatus;

        EventViewHolder(View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDateTime = itemView.findViewById(R.id.event_date_time);
            enrollmentCount = itemView.findViewById(R.id.enrollment_count);
            eventStatus = itemView.findViewById(R.id.event_status);
        }

        /**
         * Binds event data to UI components.
         *
         * @param event    The event being displayed.
         * @param listener Click listener for event selection.
         */
        void bind(EventAdmin event, OnEventClickListener listener) {
            eventName.setText(getEmoji(event.getName()) + " " + event.getName());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US);
            eventDateTime.setText(dateFormat.format(event.getEventDate()));
            enrollmentCount.setText("👥 " + event.getEnrolled() + "/" + event.getCapacity() + " enrolled");

            if (event.getStatus().equals("open")) {
                eventStatus.setText("⏳ Registration open");
            } else {
                eventStatus.setText("✓ Registration closed");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onEventClick(event);
            });
        }

        private String getEmoji(String eventName) {
            if (eventName.toLowerCase().contains("swim")) return "🏊";
            if (eventName.toLowerCase().contains("piano") || eventName.toLowerCase().contains("music")) return "🎹";
            if (eventName.toLowerCase().contains("dance")) return "💃";
            return "📅";
        }
    }
}
