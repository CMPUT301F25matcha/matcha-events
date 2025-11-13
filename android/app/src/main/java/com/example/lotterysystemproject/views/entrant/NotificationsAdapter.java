package com.example.lotterysystemproject.views.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.models.NotificationItem;
import com.example.lotterysystemproject.R;

import java.util.List;

/**
 * Bridges a list of NotificationItems to the RecyclerView UI in NotificationsActivity.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    private final List<NotificationItem> items;

    /**
     * Constructs adapter with list of notifications.
     * @param items List of NotificationItem objects to display.
     */
    public NotificationsAdapter(List<NotificationItem> items) {
        this.items = items;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, message;
        LinearLayout actionRow;
        Button acceptBtn, declineBtn;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notify_title);
            message = itemView.findViewById(R.id.notify_message);
            actionRow = itemView.findViewById(R.id.action_row);
            acceptBtn = itemView.findViewById(R.id.btn_accept);
            declineBtn = itemView.findViewById(R.id.btn_decline);
        }
    }

    /**
     * Inflates new item view when no suitable recycled view available.
     * @param parent   Parent view group.
     * @param viewType Type of view to inflate.
     * @return A new VH representing inflated view.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    /**
     * Binds data from a NotificationItem to corresponding UI components.
     * @param holder   VH instance containing view references.
     * @param position Position of item in dataset.
     */
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NotificationItem item = items.get(position);


        holder.title.setText(item.getTitle());
        holder.message.setText(item.getMessage());

        // time
        TextView timeView = holder.itemView.findViewById(R.id.notify_time);
        if (timeView != null) timeView.setText(getRelativeTime(System.currentTimeMillis() - item.getTimestamp()));

        TextView status = holder.itemView.findViewById(R.id.invite_status);

        // Restore persisted response for this item id
        NotificationItem.InvitationResponse persisted =
                com.example.lotterysystemproject.utils.NotificationsLocalStore
                        .loadResponse(holder.itemView.getContext(), item.getId());
        item.setResponse(persisted);

        if (item.isInvitation()) {
            // If user already responded, show status; else show buttons
            switch (item.getResponse()) {
                case ACCEPTED:
                    holder.actionRow.setVisibility(View.GONE);
                    status.setVisibility(View.VISIBLE);
                    status.setText("Invitation accepted");
                    break;
                case DECLINED:
                    holder.actionRow.setVisibility(View.GONE);
                    status.setVisibility(View.VISIBLE);
                    status.setText("Invitation declined");
                    break;
                case NONE:
                default:
                    status.setVisibility(View.GONE);
                    holder.actionRow.setVisibility(View.VISIBLE);

                    holder.acceptBtn.setOnClickListener(v -> {
                        item.setResponse(NotificationItem.InvitationResponse.ACCEPTED);
                        com.example.lotterysystemproject.utils.NotificationsLocalStore
                                .saveResponse(v.getContext(), item.getId(), item.getResponse());
                        notifyItemChanged(holder.getBindingAdapterPosition());
                        // TODO: Update backend if needed
                    });

                    holder.declineBtn.setOnClickListener(v -> {
                        item.setResponse(NotificationItem.InvitationResponse.DECLINED);
                        com.example.lotterysystemproject.utils.NotificationsLocalStore
                                .saveResponse(v.getContext(), item.getId(), item.getResponse());
                        notifyItemChanged(holder.getBindingAdapterPosition());
                        // TODO: Trigger replacement draw, notify organizer
                    });
                    break;
            }
        } else {
            holder.actionRow.setVisibility(View.GONE);
            status.setVisibility(View.GONE);
        }


        // Notification re-setter for testing purposes (so id does not have to be changed each time)
        holder.itemView.setOnLongClickListener(v -> {
            // Remove saved response for this notification
            com.example.lotterysystemproject.utils.NotificationsLocalStore.clearFor(
                    v.getContext(), item.getId());

            // Reset in-memory state
            item.setResponse(NotificationItem.InvitationResponse.NONE);

            // Refresh just one row
            notifyItemChanged(holder.getBindingAdapterPosition());

            // Confirmation toast
            Toast.makeText(v.getContext(), "Notification reset", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    /**
     * Converts time difference in milliseconds to relative time string.
     * @param diffMillis Elapsed time in milliseconds.
     * @return Short string representing the relative time.
     */
    // Helper: convert milliseconds into readable text
    private String getRelativeTime(long diffMillis) {
        long minutes = diffMillis / 60000;
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days == 1 ? "Yesterday" : days + "d ago";
    }

    /**
     * Returns the total number of items in the dataset.
     * @return Count of NotificationItems currently displayed.
     */
    // RecyclerView Counter
    @Override
    public int getItemCount() {
        return items.size();
    }
}
