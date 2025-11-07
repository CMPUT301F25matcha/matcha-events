package com.example.lotterysystemproject.Views.Entrant;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.Models.DeviceIdentityManager;
import com.example.lotterysystemproject.Models.EventFirebase;
import com.example.lotterysystemproject.Models.Registration;
import com.example.lotterysystemproject.R;

import java.text.DateFormat;
import java.util.List;

public class EventsHistoryActivity extends AppCompatActivity {

    private LinearLayout container;
    private ProgressBar progress;
    private TextView empty;
    private EventFirebase fm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_history);

        container = findViewById(R.id.history_container);
        progress  = findViewById(R.id.progress);
        empty     = findViewById(R.id.empty_state);

        fm = EventFirebase.getInstance();
        String uid = DeviceIdentityManager.getUserId(this);

        progress.setVisibility(View.VISIBLE);
        fm.listenUserRegistrations(uid, new EventFirebase.RegistrationsListener() {
            @Override public void onChanged(List<Registration> items) {
                progress.setVisibility(View.GONE);
                render(items);
            }
            @Override public void onError(Exception e) {
                progress.setVisibility(View.GONE);
                empty.setText("Failed to load history: " + e.getMessage());
                empty.setVisibility(View.VISIBLE);
            }
        });

    }

    private void render(List<Registration> items) {
        container.removeAllViews();
        if (items == null || items.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            return;
        }
        empty.setVisibility(View.GONE);
        DateFormat df = android.text.format.DateFormat.getMediumDateFormat(this);

        for (Registration r : items) {
            View card = getLayoutInflater().inflate(R.layout.item_recent_event, container, false);

            TextView title  = card.findViewById(R.id.history_event_name);    // was item_title
            TextView status = card.findViewById(R.id.history_event_status);  // was item_subtitle
            TextView time   = card.findViewById(R.id.history_event_time);    // was item_meta

            if (title  != null) title.setText(
                    r.getEventTitleSnapshot() == null ? "(untitled)" : r.getEventTitleSnapshot()
            );
            if (status != null) status.setText(
                    "Status: " + (r.getStatus() == null ? "—" : r.getStatus())
            );
            if (time   != null) time.setText(
                    r.getUpdatedAt() == null ? "" : "Invited: " + df.format(r.getUpdatedAt().toDate())
            );

            container.addView(card);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fm != null) fm.stopListeningUserRegistrations();
    }
}
