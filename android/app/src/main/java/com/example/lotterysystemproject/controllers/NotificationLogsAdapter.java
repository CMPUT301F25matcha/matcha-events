package com.example.lotterysystemproject.adapters;

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

public class NotificationLogsAdapter extends RecyclerView.Adapter<NotificationLogsAdapter.LogsViewHolder> {

    private final List<NotificationItem> notifications;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());


    public NotificationLogsAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }


    @NonNull
    @Override
    public LogsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new LogsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull LogsViewHolder holder, int position) {
        NotificationItem item = notifications.get(position);

        // Set event title (notification title) - convert to admin perspective
        holder.eventTitle.setText(convertToAdminTitle(item.getTitle()));

        // Set notification type description
        holder.notificationType.setText(getTypeDescription(item.getNotificationType()));

        // Set message - convert to admin perspective
        holder.message.setText(convertToAdminMessage(item.getMessage()));

        // Set timestamp
        holder.timestamp.setText(formatTimeStamp(item.getTimestamp()));

        // Set recipient ID with prefix
        String recipientText = "Sent to: " + (item.getUserId() != null ? item.getUserId() : "Unknown");
        holder.recipientId.setText(recipientText);

        // Set organizer ID if available
        if (item.getOrganizerId() != null) {
            holder.recipientId.setText(recipientText + " • From: " + item.getOrganizerId());
        }

        // Set type indicator color
        holder.typeIndicator.setBackgroundColor(getColorType(item.getNotificationType()));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Convert user-facing title to admin perspective
     */
    private String convertToAdminTitle(String originalTitle) {
        if (originalTitle == null) return "Notification";

        // Replace "You've" with "User"
        String adminTitle = originalTitle
                .replace("You've been invited to", "User invited to")
                .replace("You've been", "User")
                .replace("You have been", "User")
                .replace("You were", "User was")
                .replace("You joined", "User joined")
                .replace("Your", "User's");

        return adminTitle;
    }

    /**
     * Convert user-facing message to admin perspective
     */
    private String convertToAdminMessage(String originalMessage) {
        if (originalMessage == null) return "";

        // Replace second-person pronouns with third-person
        String adminMessage = originalMessage
                .replace("You were selected", "User was selected")
                .replace("You have been selected", "User was selected")
                .replace("You joined", "User joined")
                .replace("You were not selected", "User was not selected")
                .replace("You have been removed", "User was removed")
                .replace("You are invited", "User was invited")
                .replace("your", "their")
                .replace("You", "User");

        return adminMessage;
    }

    /**
     * Get user description for notification type
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

    /** Choose indicator color based on type */
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
     * Format timestamp from long to readable date string
     */
    private String formatTimeStamp(long timestamp) {
        try {
            Date date = new Date(timestamp);
            return dateFormat.format(date);
        } catch (Exception e) {
            return "Unknown date";
        }
    }


    /**
     * ViewHolder class to hold references to views
     */
    public static class LogsViewHolder extends RecyclerView.ViewHolder {

        TextView eventTitle, notificationType, message, timestamp, recipientId;
        View typeIndicator;
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
