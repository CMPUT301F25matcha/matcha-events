package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.models.DeviceIdentityManager;
import com.example.lotterysystemproject.models.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.lotterysystemproject.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class EventsHistoryActivity extends AppCompatActivity {

    private LinearLayout container;
    private ProgressBar progress;
    private TextView empty;
    private FirebaseFirestore db;
    private ListenerRegistration entrantsListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_history);

        container = findViewById(R.id.history_container);
        progress  = findViewById(R.id.progress);
        empty     = findViewById(R.id.empty_state);

        db = FirebaseFirestore.getInstance();
        String uid = DeviceIdentityManager.getUserId(this);

        progress.setVisibility(View.VISIBLE);

        // Listen to entrants collection filtered by userId
        entrantsListener = db.collection("entrants")
                .whereEqualTo("userId", uid)
                .orderBy("statusTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    progress.setVisibility(View.GONE);

                    if (error != null) {
                        empty.setText("Failed to load history: " + error.getMessage());
                        empty.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<Entrant> entrants = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            if (entrant != null) {
                                entrant.setId(doc.getId());
                                entrants.add(entrant);
                            }
                        }
                    }

                    render(entrants);
                });
    }

    private void render(List<Entrant> items) {
        container.removeAllViews();
        if (items == null || items.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            return;
        }
        empty.setVisibility(View.GONE);
        DateFormat df = android.text.format.DateFormat.getMediumDateFormat(this);

        for (Entrant entrant : items) {
            View card = getLayoutInflater().inflate(R.layout.item_recent_event, container, false);

            TextView title  = card.findViewById(R.id.history_event_name);
            TextView status = card.findViewById(R.id.history_event_status);
            TextView time   = card.findViewById(R.id.history_event_time);

            // Fetch event name from events collection
            if (title != null) {
                // Show loading state first
                title.setText("Loading...");

                if (entrant.getEventId() != null) {
                    db.collection("events").document(entrant.getEventId()).get()
                            .addOnSuccessListener(eventDoc -> {
                                if (eventDoc.exists()) {
                                    String eventName = eventDoc.getString("name");
                                    String eventLocation = eventDoc.getString("location");

                                    // Build display text
                                    StringBuilder displayText = new StringBuilder();
                                    if (eventName != null && !eventName.isEmpty()) {
                                        displayText.append(eventName);
                                    } else {
                                        displayText.append("(Untitled Event)");
                                    }

                                    // Optionally add location
                                    if (eventLocation != null && !eventLocation.isEmpty()) {
                                        displayText.append(" @ ").append(eventLocation);
                                    }

                                    title.setText(displayText.toString());
                                } else {
                                    title.setText("(Event not found)");
                                }
                            })
                            .addOnFailureListener(e -> {
                                title.setText("(Failed to load event)");
                            });
                } else {
                    title.setText("(Unknown event)");
                }
            }

            if (status != null) {
                status.setText("Status: " +
                        (entrant.getStatus() == null ? "—" : entrant.getStatus().toString()));
            }

            if (time != null) {
                long timestamp = entrant.getStatusTimestamp();
                if (timestamp > 0) {
                    time.setText("Updated: " + df.format(new java.util.Date(timestamp)));
                } else {
                    time.setText("");
                }
            }

            container.addView(card);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (entrantsListener != null) {
            entrantsListener.remove();
            entrantsListener = null;
        }
    }
}
