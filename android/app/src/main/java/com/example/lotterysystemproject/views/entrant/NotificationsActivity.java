package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.models.NotificationItem;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.utils.NavWiring;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a scrollable list of all notifications relevant to current entrant.
 */
public class NotificationsActivity extends AppCompatActivity {
    private NotificationsAdapter adapter;
    private final List<NotificationItem> notifications = new ArrayList<>();

    /**
     * Called when the activity is first created.
     * Initializes notification list, sets up bottom navigation bar, and populates mock data.
     * @param savedInstanceState Previous instance state if re-created
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications); // contains bottom bar include

        // Wire the bar for this screen
        NavWiring.wire(
                this,
                EntrantMainActivity.class,
                null,
                NotificationsActivity.class,
                ProfileHostActivity.class
        );

        // Make NOTIFICATIONS look selected
        LinearLayout home = findViewById(R.id.nav_home);
        LinearLayout expl = findViewById(R.id.nav_explore);
        LinearLayout qr = findViewById(R.id.nav_qr_scanner);
        LinearLayout notif = findViewById(R.id.nav_notifications);
        LinearLayout prof = findViewById(R.id.nav_profile);

        if (home != null && expl != null && qr != null && notif != null && prof != null) {
            com.example.lotterysystemproject.utils.BottomNavigationHelper.setSelectedItem(
                    com.example.lotterysystemproject.utils.BottomNavigationHelper.NavItem.NOTIFICATIONS,
                    home, expl, qr, notif, prof
            );
        }

        RecyclerView rv = findViewById(R.id.recycler_notifications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(notifications);
        rv.setAdapter(adapter);

        seedMock();
    }

    /**
     * Called when the activity resumes.
     * Ensures any updated responses are shown in RecyclerView.
     */
    @Override protected void onResume() {
        super.onResume();
        // Rebind so persisted responses are applied in onBindViewHolder
        adapter.notifyDataSetChanged();
    }


    /**
     * Generates temporary mock notifs for testing and demos.
     * Will later be replaced by Firebase data.
     */
    // Mock data, replace later with firebase
    private void seedMock() {
        long now = System.currentTimeMillis();
        notifications.clear();
        notifications.add(new NotificationItem(
                "event-123:reg-abc",
                "Event Invitation",
                "You’ve been selected for Swimming Lessons!",
                true,
                now - 2 * 60 * 60 * 1000
        ));
        notifications.add(new NotificationItem(
                "news-451",
                "Event Invitation",
                "You’ve been selected for Piano Lessons!",
                true,
                now - 10 * 60 * 1000
        ));
        adapter.notifyDataSetChanged();
    }


}
