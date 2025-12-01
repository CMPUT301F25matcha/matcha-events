package com.example.lotterysystemproject.controllers;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.NotificationItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter responsible for displaying admin notification logs.
 * <p>
 * This adapter converts user-facing notification text into admin-readable
 * context, shows notification metadata, formats timestamps, and color-codes
 * entries based on the notification type.
 * </p>
 */
public class NotificationLogsAdapter extends RecyclerView.Adapter<NotificationLogsAdapter.LogsViewHolder> {

    private final List<NotificationItem> notifications;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

    /**
     * Constructs a new NotificationLogsAdapter.
     *
     * @param notifications List of {@link NotificationItem} objects to display.
     */
    public NotificationLogsAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    /**
     * Inflates the layout for each notification row item.
     *
     * @param parent   The parent ViewGroup
     * @param viewType Type of the view (only one type is used here)
     * @return A new {@link LogsViewHolder}
     */
    @NonNull
    @Override
    public LogsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new LogsViewHolder(view);
    }

    /**
     * Binds a notification item to UI components in the ViewHolder.
     *
     * @param holder   The ViewHolder for the row
     * @param position Item position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull LogsViewHolder holder, int position) {
        NotificationItem item = notifications.get(position);

        holder.eventTitle.setText(convertToAdminTitle(item.getTitle()));
        holder.notificationType.setText(getTypeDescription(item.getNotificationType()));
        holder.message.setText(convertToAdminMessage(item.getMessage()));
        holder.timestamp.setText(formatTimeStamp(item.getTimestamp()));

        String recipientText = "Sent to: " + (item.getUserId() != null ? item.getUserId() : "Unknown");
        holder.recipientId.setText(recipientText);

        // Show organizer ID if present
        if (item.getOrganizerId() != null) {
            holder.recipientId.setText(recipientText + " • From: " + item.getOrganizerId());
        }

        holder.typeIndicator.setBackgroundColor(getColorType(item.getNotificationType()));
    }

    /**
     * @return The number of notifications in the list
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Converts a user-facing notification title into an admin-readable perspective.
     *
     * @param originalTitle The original user-facing title
     * @return Admin-friendly rewritten title
     */
    private String convertToAdminTitle(String originalTitle) {
        if (originalTitle == null) return "Notification";

        return originalTitle
                .replace("You've been invited to", "User invited to")
                .replace("You've been", "User")
                .replace("You have been", "User")
                .replace("You were", "User was")
                .replace("You joined", "User joined")
                .replace("Your", "User's");
    }

    /**
     * Converts a user-facing notification message into admin-readable perspective.
     *
     * @param originalMessage The original message text
     * @return Message rewritten in third person for admin viewing
     */
    private String convertToAdminMessage(String originalMessage) {
        if (originalMessage == null) return "";

        return originalMessage
                .replace("You were selected", "User was selected")
                .replace("You have been selected", "User was selected")
                .replace("You joined", "User joined")
                .replace("You were not selected", "User was not selected")
                .replace("You have been removed", "User was removed")
                .replace("You are invited", "User was invited")
                .replace("your", "their")
                .replace("You", "User");
    }

    /**
     * Converts the notification type into a readable string description.
     *
     * @param type The notification type enum
     * @return Human-readable description for display
     */
    private String getTypeDescription(NotificationItem.NotificationType type) {
        if (type == null) return "Unknown";

        switch (type) {
            case WAITING:
                return "Joined Waiting List";
            case INVITED:
                return "Invited to Event";
            case DECLINED:
                return "Not Selected";
            case CANCELLED:
                return "Removed from Event";
            default:
                return type.toString();
        }
    }

    /**
     * Returns a color value based on the notification type.
     *
     * @param type Notification type enum
     * @return Color integer representing the type category
     */
    private int getColorType(NotificationItem.NotificationType type) {
        if (type == null) return Color.GRAY;

        switch (type) {
            case WAITING:
                return Color.parseColor("#FFC107");
            case INVITED:
                return Color.parseColor("#4CAF50");
            case DECLINED:
                return Color.parseColor("#F44336");
            case CANCELLED:
                return Color.parseColor("#9E9E9E");
            default:
                return Color.GRAY;
        }
    }

    /**
     * Formats a timestamp (milliseconds) into a readable date/time string.
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string, or "Unknown date" if parsing fails
     */
    private String formatTimeStamp(long timestamp) {
        try {
            return dateFormat.format(new Date(timestamp));
        } catch (Exception e) {
            return "Unknown date";
        }
    }

    /**
     * ViewHolder class used for caching views for each list item.
     */
    public static class LogsViewHolder extends RecyclerView.ViewHolder {

        TextView eventTitle, notificationType, message, timestamp, recipientId;
        View typeIndicator;

        /**
         * Initializes ViewHolder and binds UI elements.
         *
         * @param itemView The row item view
         */
        public LogsViewHolder(@NonNull View itemView) {
            super(itemView);

            eventTitle = itemView.findViewById(R.id.event_title);
            notificationType = itemView.findViewById(R.id.notification_type);
            message = itemView.findViewById(R.id.message);
            timestamp = itemView.findViewById(R.id.timestamp);
            recipientId = itemView.findViewById(R.id.recipient_id);
            typeIndicator = itemView.findViewById(R.id.type_indicator);
        }
    }
}

