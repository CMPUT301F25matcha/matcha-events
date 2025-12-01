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

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.NotificationItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.List;

/**
 * RecyclerView adapter that renders a list of NotificationItems in entrant's notifications screen.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    private final List<NotificationItem> items;

    /**
     * Creates a new adapter instance.
     *
     * @param items backing list of notifications to display.
     */
    public NotificationsAdapter(List<NotificationItem> items) {
        this.items = items;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, message, timeView, statusView;
        LinearLayout actionRow;
        Button acceptBtn, declineBtn;

        /**
         * ViewHolder holding references to all views in a single notification row.
         */
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notify_title);
            message = itemView.findViewById(R.id.notify_message);
            timeView = itemView.findViewById(R.id.notify_time);
            statusView = itemView.findViewById(R.id.invite_status);

            actionRow = itemView.findViewById(R.id.action_row);
            acceptBtn = itemView.findViewById(R.id.btn_accept);
            declineBtn = itemView.findViewById(R.id.btn_decline);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    /**
     * Binds a NotificationItem to the given VH.
     */
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NotificationItem item = items.get(position);

        // Text fields
        holder.title.setText(item.getTitle());
        holder.message.setText(item.getMessage());

        // Relative time
        if (holder.timeView != null && item.getTimestamp() > 0) {
            long diff = System.currentTimeMillis() - item.getTimestamp();
            holder.timeView.setText(getRelativeTime(diff));
        }

        // Hide actions by default
        holder.actionRow.setVisibility(View.GONE);
        holder.acceptBtn.setVisibility(View.GONE);
        holder.declineBtn.setVisibility(View.GONE);

        NotificationItem.NotificationType type = item.getNotificationType();
        NotificationItem.Decision decision = item.getDecision();


        // Only show status for Accept/Decline notifs
        if (holder.statusView != null) {
            holder.statusView.setVisibility(View.GONE); // default

            if (type == NotificationItem.NotificationType.INVITED &&
                    decision != null &&
                    decision != NotificationItem.Decision.NONE) {

                holder.statusView.setVisibility(View.VISIBLE);
                if (decision == NotificationItem.Decision.ACCEPTED) {
                    holder.statusView.setText("Invitation accepted");
                } else if (decision == NotificationItem.Decision.DECLINED) {
                    holder.statusView.setText("Invitation declined");
                }
            }
        }

        // Accept / Decline logic
        if (type == NotificationItem.NotificationType.INVITED) {

            // If user hasn't decided, show buttons, otherwise hide
            if (decision == null || decision == NotificationItem.Decision.NONE) {
                holder.actionRow.setVisibility(View.VISIBLE);
                holder.acceptBtn.setVisibility(View.VISIBLE);
                holder.declineBtn.setVisibility(View.VISIBLE);
            } else {
                holder.actionRow.setVisibility(View.GONE);
                holder.acceptBtn.setVisibility(View.GONE);
                holder.declineBtn.setVisibility(View.GONE);
                return;
            }

            // Parse entrantId from notification id
            String notifId = item.getId();
            String[] parts = notifId != null ? notifId.split(":") : new String[0];

            final String eventIdFromNotif   = (parts.length == 2) ? parts[0] : null;
            final String entrantIdFromNotif = (parts.length == 2) ? parts[1] : null;

            // If we cannot parse entrant id do not hook up actions
            if (entrantIdFromNotif == null) {
                holder.acceptBtn.setOnClickListener(v ->
                        Toast.makeText(v.getContext(),
                                "Cannot accept: invalid notification id format.",
                                Toast.LENGTH_SHORT).show());
                holder.declineBtn.setOnClickListener(v ->
                        Toast.makeText(v.getContext(),
                                "Cannot decline: invalid notification id format.",
                                Toast.LENGTH_SHORT).show());
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // ACCEPT
            holder.acceptBtn.setOnClickListener(v -> {
                long now = System.currentTimeMillis();

                // Mark entrant as enrolled
                db.collection("entrants")
                        .document(entrantIdFromNotif)
                        .update("status", "ENROLLED",
                                "statusTimestamp", now)
                        .addOnSuccessListener(aVoid -> {

                            // Increment event's currentEnrolled if we have eventId
                            if (eventIdFromNotif != null) {
                                db.collection("events")
                                        .document(eventIdFromNotif)
                                        .update("currentEnrolled", FieldValue.increment(1));
                            }

                            // Persist decision in notification document
                            db.collection("notifications")
                                    .document(item.getId())
                                    .update("decision",
                                            NotificationItem.Decision.ACCEPTED.name());

                            // Update in-memory object
                            item.setDecision(NotificationItem.Decision.ACCEPTED);
                            notifyItemChanged(holder.getBindingAdapterPosition());

                            Toast.makeText(v.getContext(),
                                    "Invitation accepted.",
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(
                                v.getContext(),
                                "Failed to accept: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show());
            });

            // DECLINE
            holder.declineBtn.setOnClickListener(v -> {
                long now = System.currentTimeMillis();

                // Mark entrant as cancelled
                db.collection("entrants")
                        .document(entrantIdFromNotif)
                        .update("status", "CANCELLED",
                                "statusTimestamp", now)

                        // Persist decision in notification document
                        .addOnSuccessListener(aVoid -> {
                            db.collection("notifications")
                                    .document(item.getId())
                                    .update("decision",
                                            NotificationItem.Decision.DECLINED.name());

                            item.setDecision(NotificationItem.Decision.DECLINED);
                            notifyItemChanged(holder.getBindingAdapterPosition());

                            Toast.makeText(v.getContext(),
                                    "Invitation declined.",
                                    Toast.LENGTH_SHORT).show();
                        })

                        .addOnFailureListener(e -> Toast.makeText(
                                v.getContext(),
                                "Failed to decline: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show());
            });
        }
    }


    /**
     * Converts elapsed time in milliseconds into readable relative time string.
     *
     * @param diffMillis elapsed time between now and event timestamp.
     * @return relative time.
     */
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
     * @return number of notifications currently in adapter backing list.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }
}
