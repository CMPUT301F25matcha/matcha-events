package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.views.entrant.NotificationsAdapter;
import com.example.lotterysystemproject.firebasemanager.NotificationRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryCallback;
import com.example.lotterysystemproject.firebasemanager.RepositoryListener;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.DeviceIdentityManager;
import com.example.lotterysystemproject.models.NotificationItem;
import com.example.lotterysystemproject.utils.BottomNavigationHelper;
import com.example.lotterysystemproject.utils.NavWiring;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private final List<NotificationItem> notifications = new ArrayList<>();
    private NotificationsAdapter adapter;

    private NotificationRepository notificationRepo;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // bottom nav wiring
        NavWiring.wire(
                this,
                EntrantMainActivity.class,
                null,
                null,
                NotificationsActivity.class,
                ProfileHostActivity.class
        );

        LinearLayout home  = findViewById(R.id.nav_home);
        LinearLayout expl  = findViewById(R.id.nav_explore);
        LinearLayout qr    = findViewById(R.id.nav_qr_scanner);
        LinearLayout notif = findViewById(R.id.nav_notifications);
        LinearLayout prof  = findViewById(R.id.nav_profile);

        if (home != null && expl != null && qr != null && notif != null && prof != null) {
            BottomNavigationHelper.setSelectedItem(
                    BottomNavigationHelper.NavItem.NOTIFICATIONS,
                    home, expl, qr, notif, prof
            );
        }

        RecyclerView rv = findViewById(R.id.recycler_notifications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(notifications);
        rv.setAdapter(adapter);


        notificationRepo = RepositoryProvider.getNotificationRepository();
        currentUserId = DeviceIdentityManager.getUserId(this);


        // listen to real Firestore data
        listenForNotifications();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (notificationRepo != null) {
            notificationRepo.stopListeningUserNotifications();
        }
    }


    private void listenForNotifications() {
        if (notificationRepo == null || currentUserId == null) return;

        notificationRepo.listenUserNotifications(
                currentUserId,
                new RepositoryListener<List<NotificationItem>>() {
                    @Override
                    public void onDataChanged(List<NotificationItem> data) {
                        notifications.clear();
                        if (data != null) notifications.addAll(data);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("NotificationsActivity", "Error listening to notifications", e);
                    }
                }
        );
    }
}
