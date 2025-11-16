package com.example.lotterysystemproject.views.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.Event;

import java.util.List;

public class FeaturedEventsAdapter extends RecyclerView.Adapter<FeaturedEventsAdapter.ViewHolder> {

    public interface OnEventClick {
        void onClick(Event e);
    }

    private final List<Event> events;
    private final OnEventClick callback;

    public FeaturedEventsAdapter(List<Event> events, OnEventClick callback) {
        this.events = events;
        this.callback = callback;
    }

    @NonNull
    @Override
    public FeaturedEventsAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_event_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull FeaturedEventsAdapter.ViewHolder holder, int position) {

        Event e = events.get(position);
        holder.title.setText(e.getName());
        holder.host.setText(e.getHostName());
        holder.itemView.setOnClickListener(v -> callback.onClick(e));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView host;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.featured_event_title);
            host = v.findViewById(R.id.featured_event_host);
        }
    }
}
