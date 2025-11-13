package com.example.lotterysystemproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.Entrant;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying entrants in waiting or selected lists.
 * Handles two view types: waiting entrants and selected entrants.
 */
public class EntrantAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_WAITING = 0;
    private static final int TYPE_SELECTED = 1;

    private List<Entrant> entrants;
    private int viewType;
    private OnEntrantActionListener listener;

    /**
     * Interface defining actions that can be performed on an entrant.
     */
    public interface OnEntrantActionListener {
        /**
         * Called when the organizer cancels an invited entrant.
         *
         * @param entrant The entrant being cancelled.
         */
        void onCancelEntrant(Entrant entrant);

        /**
         * Called when the organizer draws a replacement for a cancelled entrant.
         *
         * @param entrant The entrant being replaced.
         */
        void onDrawReplacement(Entrant entrant);
    }

    /**
     * Constructs a new EntrantAdapter.
     *
     * @param viewType The type of entrants being displayed (waiting or selected).
     * @param listener Listener for entrant-related actions.
     */
    public EntrantAdapter(int viewType, OnEntrantActionListener listener) {
        this.entrants = new ArrayList<>();
        this.viewType = viewType;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_WAITING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_entrant_waiting, parent, false);
            return new WaitingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_entrant_selected, parent, false);
            return new SelectedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);
        if (holder instanceof WaitingViewHolder) {
            ((WaitingViewHolder) holder).bind(entrant);
        } else if (holder instanceof SelectedViewHolder) {
            ((SelectedViewHolder) holder).bind(entrant, listener);
        }
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /**
     * Updates the list of entrants and refreshes the view.
     *
     * @param newEntrants The new list of entrants to display.
     */
    public void updateEntrants(List<Entrant> newEntrants) {
        this.entrants = newEntrants != null ? newEntrants : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for entrants in the waiting list.
     */
    static class WaitingViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, joinedText;

        WaitingViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.entrant_name);
            emailText = itemView.findViewById(R.id.entrant_email);
            joinedText = itemView.findViewById(R.id.joined_time);
        }

        /**
         * Binds waiting entrant data to the view.
         *
         * @param entrant The entrant to bind.
         */
        void bind(Entrant entrant) {
            nameText.setText(entrant.getName());
            emailText.setText(entrant.getEmail());
            joinedText.setText("Joined: " + entrant.getTimeAgo());
        }
    }

    /**
     * ViewHolder for selected entrants (invited, enrolled, or cancelled).
     */
    static class SelectedViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, statusText, timeText;
        Button cancelButton, replacementButton;

        SelectedViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.entrant_name);
            emailText = itemView.findViewById(R.id.entrant_email);
            statusText = itemView.findViewById(R.id.status_text);
            timeText = itemView.findViewById(R.id.status_time);
            cancelButton = itemView.findViewById(R.id.cancel_button);
            replacementButton = itemView.findViewById(R.id.replacement_button);
        }

        /**
         * Binds entrant data and sets up button actions.
         *
         * @param entrant  The entrant being displayed.
         * @param listener Listener for action callbacks.
         */
        void bind(Entrant entrant, OnEntrantActionListener listener) {
            nameText.setText(getStatusIcon(entrant.getStatus()) + " " + entrant.getName());
            emailText.setText(entrant.getEmail());
            statusText.setText("Status: " + getStatusText(entrant.getStatus()));
            timeText.setText(entrant.getTimeAgo());

            if (entrant.getStatus() == Entrant.Status.INVITED) {
                cancelButton.setVisibility(View.VISIBLE);
                replacementButton.setVisibility(View.GONE);
                cancelButton.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelEntrant(entrant);
                });
            } else if (entrant.getStatus() == Entrant.Status.CANCELLED) {
                cancelButton.setVisibility(View.GONE);
                replacementButton.setVisibility(View.VISIBLE);
                replacementButton.setOnClickListener(v -> {
                    if (listener != null) listener.onDrawReplacement(entrant);
                });
            } else {
                cancelButton.setVisibility(View.GONE);
                replacementButton.setVisibility(View.GONE);
            }
        }

        private String getStatusIcon(Entrant.Status status) {
            switch (status) {
                case INVITED: return "⏳";
                case ENROLLED: return "✓";
                case CANCELLED: return "✗";
                default: return "";
            }
        }

        private String getStatusText(Entrant.Status status) {
            switch (status) {
                case INVITED: return "Invited (Pending)";
                case ENROLLED: return "Enrolled";
                case CANCELLED: return "Cancelled";
                default: return "Unknown";
            }
        }
    }
}
