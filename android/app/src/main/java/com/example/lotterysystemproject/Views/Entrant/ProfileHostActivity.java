package com.example.lotterysystemproject.Views.Entrant;

import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Utils.BottomNavigationHelper;
import com.example.lotterysystemproject.Utils.NavWiring;

public class ProfileHostActivity extends AppCompatActivity {
    private NavController navController;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_main_activity);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.entrant_nav_host);
        navController = host.getNavController();

        // Wire the same custom bottom bar here
        NavWiring.wire(
                this,
                com.example.lotterysystemproject.Views.Entrant.EntrantMainActivity.class, // home
                null,
                null,
                NotificationsActivity.class,
                ProfileHostActivity.class // profile (self)
        );

        // Keeps Profile icon selected while in Profile/Settings
        navController.addOnDestinationChangedListener((c, dest, args) -> {
            LinearLayout home  = findViewById(R.id.nav_home);
            LinearLayout expl  = findViewById(R.id.nav_explore);
            LinearLayout qr    = findViewById(R.id.nav_qr_scanner);
            LinearLayout notif = findViewById(R.id.nav_notifications);
            LinearLayout prof  = findViewById(R.id.nav_profile);
            BottomNavigationHelper.setSelectedItem(
                    BottomNavigationHelper.NavItem.PROFILE, home, expl, qr, notif, prof
            );
        });
    }
}
