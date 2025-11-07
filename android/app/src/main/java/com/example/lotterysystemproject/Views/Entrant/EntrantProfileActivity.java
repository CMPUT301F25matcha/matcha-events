package com.example.lotterysystemproject.Views.Entrant;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.utils.NavWiring;

/**
 * Displays the entrant’s personal profile screen, allowing users to view and manage
 * their account information.
 */
public class EntrantProfileActivity extends AppCompatActivity {

    /**
     * Initializes the entrant profile screen and configures bottom navigation.
     * @param savedInstanceState saved instance state of the activity if re-initialized
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_entrant_profile);

        // Wire the bar
        NavWiring.wire(
                this,
                EntrantMainActivity.class,
                null,
                null,
                NotificationsActivity.class,
                EntrantProfileActivity.class
        );

        // Select PROFILE tab in this screen
        com.example.lotterysystemproject.utils.BottomNavigationHelper.setSelectedItem(
                com.example.lotterysystemproject.utils.BottomNavigationHelper.NavItem.PROFILE,
                findViewById(R.id.nav_home),
                findViewById(R.id.nav_explore),
                findViewById(R.id.nav_qr_scanner),
                findViewById(R.id.nav_notifications),
                findViewById(R.id.nav_profile)
        );
    }

}

