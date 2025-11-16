package com.example.lotterysystemproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.Event;

import java.text.DateFormat;
import java.util.List;

/**
 * Adapter for horizontal recent events list.
 */
public class RecentEventsAdapter extends RecyclerView.Adapter<RecentEventsAdapter.ViewHolder> {

    public interface OnEventClick { void onClick(Event event); }

    private final List<Event> items;
    private final OnEventClick callback;
    private final Context ctx;

    public RecentEventsAdapter(Context ctx, List<Event> items, OnEventClick callback) {
        this.ctx = ctx;
        this.items = items;
        this.callback = callback;
    }

    public void update(List<Event> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentEventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_recent_event_card_main, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentEventsAdapter.ViewHolder holder, int position) {
        final Event e = items.get(position);
        holder.title.setText(e.getName() != null ? e.getName() : "");
        holder.host.setText(e.getHostName() != null ? e.getHostName() : "");

        if (e.getEventDate() != null) {
            holder.dateText = DateFormat.getDateInstance().format(e.getEventDate());
        } else {
            holder.dateText = "";
        }

        // Load image - uses Glide if available, otherwise fallback to placeholder
        if (e.getPosterImageUrl() != null && !e.getPosterImageUrl().isEmpty()) {
            try {
                // Glide is recommended — add dependency if you haven't:
                // implementation 'com.github.bumptech.glide:glide:4.15.1'
                com.bumptech.glide.Glide.with(ctx)
                        .load(e.getPosterImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.ic_image) // add drawable or replace
                        .into(holder.poster);
            } catch (Throwable ex) {
                // If Glide not available, ignore and leave placeholder
                holder.poster.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        } else {
            holder.poster.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (callback != null) callback.onClick(e);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView host;
        TextView title;
        String dateText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.event_poster);
            host = itemView.findViewById(R.id.event_host_name);
            title = itemView.findViewById(R.id.event_name);
        }
    }
}

