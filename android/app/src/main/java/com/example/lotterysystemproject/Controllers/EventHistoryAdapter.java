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

/**
 * Adapter class that binds EventHistoryItem objects to RecyclerView items
 * for displaying a user's event registration history.
 */
public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.ViewHolder> {

    private final List<EventHistoryItem> items;

    /**
     * Constructs an EventHistoryAdapter with the given list of history items
     * @param items a list of EventHistoryItem objects to be displayed.
     */
    public EventHistoryAdapter(List<EventHistoryItem> items) {
        this.items = items;
    }

    /**
     * Inflates the layout for an individual history item view.
     * @param parent   the parent view group into which the new view will be added
     * @param viewType view type of the new view
     * @return a new viewHolder instance containing inflated view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_event, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds an EventHistoryItem to its corresponding ViewHolder.
     * @param holder   ViewHolder containing the view references
     * @param position position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventHistoryItem item = items.get(position);
        holder.eventName.setText(item.getEventName());
        holder.status.setText("Status: " + item.getStatus());
        holder.dateTime.setText("Invited: " + item.getDateTime());
    }

    /**
     * Returns total number of items in the data set held by the adapter.
     * @return the number of items in items.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * A RecyclerView.ViewHolder subclass that holds the views for each event history item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, status, dateTime;


        /**
         * Initializes view holder and binds its UI elements.
         * @param itemView the item view inflated from XML.
         */
        ViewHolder(View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.history_event_name);
            status = itemView.findViewById(R.id.history_event_status);
            dateTime = itemView.findViewById(R.id.history_event_time);
        }
    }
}
