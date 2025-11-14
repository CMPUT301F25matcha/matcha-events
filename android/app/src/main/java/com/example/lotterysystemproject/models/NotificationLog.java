package com.example.matchamonday.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Model for notification logs
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * Audit log for notifications sent by the system
 * Used for admin review (US 03.08.01)
 */
public class NotificationLog {

    private String logId;
    private String eventId;
    private String eventName;
    private String recipientUserId;
    private String notificationType; // LOTTERY_WIN, LOTTERY_LOSS, ORGANIZER_MESSAGE, etc.
    private String title;
    private String body;
    private long timestamp;
    private boolean sentSuccessfully;
    private String organizerId;
    private String errorMessage; // If failed
// ========================================================================
    // FIRESTORE CONVERSION
    // ========================================================================

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("logId", logId);
        map.put("eventId", eventId);
        map.put("eventName", eventName);
        map.put("recipientUserId", recipientUserId);
        map.put("notificationType", notificationType);
        map.put("title", title);
        map.put("body", body);
        map.put("timestamp", timestamp);
        map.put("sentSuccessfully", sentSuccessfully);
        map.put("organizerId", organizerId);
        map.put("errorMessage", errorMessage);
        return map;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Get formatted timestamp for display
     */
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Get status icon
     */
    public String getStatusIcon() {
        return sentSuccessfully ? "✓" : "✗";
    }

    /**
     * Get human-readable notification type
     */
    public String getTypeDisplayName() {
        switch (notificationType) {
            case "LOTTERY_WIN":
                return "Lottery Winner";
            case "LOTTERY_LOSS":
                return "Lottery Not Selected";
            case "ORGANIZER_MESSAGE":
                return "Organizer Message";
            case "REPLACEMENT_SELECTED":
                return "Replacement Selected";
            case "EVENT_CANCELLED":
                return "Event Cancelled";
            default:
                return notificationType;
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s to %s: %s - %s",
                getFormattedTimestamp(),
                getStatusIcon(),
                getTypeDisplayName(),
                recipientUserId,
                title,
                sentSuccessfully ? "Success" : "Failed");
    }

    /**
     * Create admin-friendly log entry
     */
    public String toAdminLogEntry() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("Notification Log ID: ").append(logId).append("\n");
        sb.append("Time: ").append(getFormattedTimestamp()).append("\n");
        sb.append("Event: ").append(eventName).append(" (").append(eventId).append(")\n");
        sb.append("Recipient: ").append(recipientUserId).append("\n");
        sb.append("Type: ").append(getTypeDisplayName()).append("\n");
        sb.append("Title: ").append(title).append("\n");
        sb.append("Body: ").append(body).append("\n");
        sb.append("Status: ").append(sentSuccessfully ? "✓ Sent Successfully" : "✗ Failed").append("\n");
        if (!sentSuccessfully && errorMessage != null) {
            sb.append("Error: ").append(errorMessage).append("\n");
        }
        if (organizerId != null) {
            sb.append("Organizer: ").append(organizerId).append("\n");
        }
        sb.append("═══════════════════════════════════════════\n");
        return sb.toString();
    }
}
