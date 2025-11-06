package com.example.lotterysystemproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Models.Event;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(List<Event> events, OnEventClickListener listener) {
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
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDateTime, enrollmentCount, eventStatus;

        EventViewHolder(View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDateTime = itemView.findViewById(R.id.event_date_time);
            enrollmentCount = itemView.findViewById(R.id.enrollment_count);
            eventStatus = itemView.findViewById(R.id.event_status);
        }

        void bind(Event event, OnEventClickListener listener) {
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
                if (listener != null) {
                    listener.onEventClick(event);
                }
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