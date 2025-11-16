package com.example.lotterysystemproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.Event;

import java.text.DateFormat;
import java.util.List;

/**
 * Simple adapter for category event list. Uses item_category_event_card.xml.
 */
public class CategoryEventAdapter extends RecyclerView.Adapter<CategoryEventAdapter.ViewHolder> {

    public interface OnEventClick {
        void onClick(Event event);
    }

    private List<Event> items;
    private final OnEventClick callback;

    public CategoryEventAdapter(List<Event> items, OnEventClick callback) {
        this.items = items;
        this.callback = callback;
    }

    public void update(List<Event> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryEventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_category_event_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryEventAdapter.ViewHolder holder, int position) {
        Event e = items.get(position);
        holder.title.setText(e.getName());
        holder.host.setText(e.getHostName() != null ? e.getHostName() : "");
        if (e.getEventDate() != null) {
            holder.date.setText(DateFormat.getDateTimeInstance().format(e.getEventDate()));
        } else {
            holder.date.setText("");
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
        TextView title;
        TextView host;
        TextView date;
        ImageView thumb;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.cat_event_title);
            host = itemView.findViewById(R.id.cat_event_host);
            date = itemView.findViewById(R.id.cat_event_date);
            thumb = itemView.findViewById(R.id.cat_event_thumb);
        }
    }
}
