package com.example.lotterysystemproject.Controllers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.Models.EventHistoryItem;
import com.example.lotterysystemproject.R;

import java.util.List;

public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.ViewHolder> {

    private final List<EventHistoryItem> items;

    public EventHistoryAdapter(List<EventHistoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventHistoryItem item = items.get(position);
        holder.eventName.setText(item.getEventName());
        holder.status.setText("Status: " + item.getStatus());
        holder.dateTime.setText("Invited: " + item.getDateTime());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, status, dateTime;

        ViewHolder(View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.history_event_name);
            status = itemView.findViewById(R.id.history_event_status);
            dateTime = itemView.findViewById(R.id.history_event_time);
        }
    }
}
