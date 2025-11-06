package com.example.lotterysystemproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Models.Entrant;
import java.util.ArrayList;
import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_WAITING = 0;
    private static final int TYPE_SELECTED = 1;

    private List<Entrant> entrants;
    private int viewType;
    private OnEntrantActionListener listener;

    public interface OnEntrantActionListener {
        void onCancelEntrant(Entrant entrant);
        void onDrawReplacement(Entrant entrant);
    }

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

    public void updateEntrants(List<Entrant> newEntrants) {
        this.entrants = newEntrants != null ? newEntrants : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ViewHolder for Waiting List items
    static class WaitingViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, joinedText;

        WaitingViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.entrant_name);
            emailText = itemView.findViewById(R.id.entrant_email);
            joinedText = itemView.findViewById(R.id.joined_time);
        }

        void bind(Entrant entrant) {
            nameText.setText(entrant.getName());
            emailText.setText(entrant.getEmail());
            joinedText.setText("Joined: " + entrant.getTimeAgo());
        }
    }

    // ViewHolder for Selected items (INVITED, ENROLLED, CANCELLED)
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

        void bind(Entrant entrant, OnEntrantActionListener listener) {
            nameText.setText(getStatusIcon(entrant.getStatus()) + " " + entrant.getName());
            emailText.setText(entrant.getEmail());
            statusText.setText("Status: " + getStatusText(entrant.getStatus()));
            timeText.setText(entrant.getTimeAgo());

            // Show/hide buttons based on status
            if (entrant.getStatus() == Entrant.Status.INVITED) {
                // Show cancel button for invited entrants
                cancelButton.setVisibility(View.VISIBLE);
                replacementButton.setVisibility(View.GONE);

                cancelButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCancelEntrant(entrant);
                    }
                });
            } else if (entrant.getStatus() == Entrant.Status.CANCELLED) {
                // Show replacement button for cancelled entrants
                cancelButton.setVisibility(View.GONE);
                replacementButton.setVisibility(View.VISIBLE);

                replacementButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDrawReplacement(entrant);
                    }
                });
            } else {
                // ENROLLED - no buttons
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