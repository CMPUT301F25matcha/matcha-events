package com.example.lotterysystemproject.Views.Entrant;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.Models.Registration;
import com.example.lotterysystemproject.R;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.List;

public class RegistrationHistoryActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    private LinearLayout container;   // history_container
    private ProgressBar progress;
    private TextView emptyState;

    private FirebaseManager fm;
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_history);

        container   = findViewById(R.id.history_container);
        progress    = findViewById(R.id.progress);
        emptyState  = findViewById(R.id.empty_state);

        fm  = FirebaseManager.getInstance();

        // Prefer explicit userId passed from the caller; fall back if you added getCurrentUserId()
        uid = getIntent().getStringExtra(EXTRA_USER_ID);
        if (uid == null || uid.trim().isEmpty()) {
            try {
                // replace with your own auth/device id strategy if available
                uid = fm.getDatabase().getApp().getName(); // placeholder so it doesn't crash; pass EXTRA_USER_ID in production
            } catch (Exception ignore) { uid = "unknown"; }
        }

        startListening();
    }

    private void startListening() {
        progress.setVisibility(View.VISIBLE);
        fm.listenUserRegistrations(uid, new FirebaseManager.RegistrationsListener() {
            @Override
            public void onChanged(List<Registration> items) {
                progress.setVisibility(View.GONE);
                // Keep first three children (title/progress/empty); remove any old cards
                int keep = 3;
                if (container.getChildCount() > keep) {
                    container.removeViews(keep, container.getChildCount() - keep);
                }

                if (items == null || items.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    return;
                }
                emptyState.setVisibility(View.GONE);

                for (Registration r : items) {
                    container.addView(buildCard(r));
                    // Optional divider
                    container.addView(buildDivider());
                }
            }

            @Override
            public void onError(Exception e) {
                progress.setVisibility(View.GONE);
                Snackbar.make(container, "Failed to load history", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private View buildDivider() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        v.setBackgroundColor(0x22000000); // light divider
        return v;
    }

    private View buildCard(Registration r) {
        int pad = dp(12);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(pad, pad, pad, pad);

        // Title
        TextView title = new TextView(this);
        title.setText(r.getEventTitleSnapshot() == null ? "(Event)" : r.getEventTitleSnapshot());
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(16);

        // Date/time (if provided)
        TextView date = new TextView(this);
        if (r.getEventStartAt() != null) {
            String formatted = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.SHORT).format(r.getEventStartAt().toDate());
            date.setText(formatted);
        } else {
            date.setText(""); // or remove if you prefer not to show an empty line
        }

        // Status
        TextView status = new TextView(this);
        status.setText(formatStatus(r.getStatus()));
        status.setTextColor(pickStatusColor(r.getStatus()));

        card.addView(title);
        card.addView(date);
        card.addView(status);
        return card;
    }

    private String formatStatus(String s) {
        if (s == null) return "—";
        switch (s) {
            case "JOINED": return "Joined";
            case "SELECTED": return "Selected";
            case "DECLINED": return "Declined";
            case "NOT_SELECTED": return "Not selected";
            default: return s;
        }
    }

    private int pickStatusColor(String s) {
        if (s == null) return getColorCompat(android.R.color.black);
        switch (s) {
            case "SELECTED": return 0xFF2E7D32; // green-ish
            case "DECLINED": return 0xFFB00020; // red-ish
            case "NOT_SELECTED": return 0xFF455A64; // gray/blue
            default: return getColorCompat(android.R.color.holo_blue_dark);
        }
    }

    private int getColorCompat(int resId) {
        return getResources().getColor(resId);
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fm.stopListeningUserRegistrations();
    }
}
