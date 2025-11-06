package com.example.lotterysystemproject.Views.Entrant;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Utils.NavWiring;


public class EntrantProfileActivity extends AppCompatActivity {
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
                null,
                EntrantProfileActivity.class
        );

        // Select PROFILE tab in this screen
        com.example.lotterysystemproject.Utils.BottomNavigationHelper.setSelectedItem(
                com.example.lotterysystemproject.Utils.BottomNavigationHelper.NavItem.PROFILE,
                findViewById(R.id.nav_home),
                findViewById(R.id.nav_explore),
                findViewById(R.id.nav_qr_scanner),
                findViewById(R.id.nav_notifications),
                findViewById(R.id.nav_profile)
        );
    }

}

