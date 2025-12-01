package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.utils.BottomNavigationHelper;
import com.example.lotterysystemproject.utils.NavWiring;

/**
 * Serves as the container for entrant profile-related fragments.
 */
public class ProfileHostActivity extends AppCompatActivity {
    private NavController navController;

    /**
     * Called when activity is created.
     * Initializes navigation host, sets up the bottom navigation bar, ensures the profile tab
     * remains highlighted when navigating between fragments.
     * @param savedInstanceState Previous instance state if re-created
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_main_activity);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.entrant_nav_host);
        navController = host.getNavController();

        NavWiring.wire(
                this,
                com.example.lotterysystemproject.views.entrant.EntrantMainActivity.class,
                null,
                NotificationsActivity.class,
                ProfileHostActivity.class
        );

        // Keeps Profile icon selected while in Profile/Settings
        navController.addOnDestinationChangedListener((c, dest, args) -> {
            LinearLayout home  = findViewById(R.id.nav_home);
            LinearLayout qr    = findViewById(R.id.nav_qr_scanner);
            LinearLayout notif = findViewById(R.id.nav_notifications);
            LinearLayout prof  = findViewById(R.id.nav_profile);
            BottomNavigationHelper.setSelectedItem(
                    BottomNavigationHelper.NavItem.PROFILE, home, qr, notif, prof
            );
        });
    }
}
