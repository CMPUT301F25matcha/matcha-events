package com.example.lotterysystemproject.Utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.lotterysystemproject.R;

public final class NavWiring {
    private NavWiring() {}

    public static void wire(Activity a,
                            Class<?> home, Class<?> explore, Class<?> qr,
                            Class<?> notif, Class<?> profile) {

        LinearLayout navHome = a.findViewById(R.id.nav_home);
        LinearLayout navExplore = a.findViewById(R.id.nav_explore);
        LinearLayout navQR = a.findViewById(R.id.nav_qr_scanner);
        LinearLayout navNotif = a.findViewById(R.id.nav_notifications);
        LinearLayout navProfile = a.findViewById(R.id.nav_profile);

        View.OnClickListener go = v -> {
            Class<?> target =
                    v.getId() == R.id.nav_home ? home :
                    v.getId() == R.id.nav_explore ? explore :
                    v.getId() == R.id.nav_qr_scanner ? qr :
                    v.getId() == R.id.nav_notifications ? notif : profile;

            // Toast for activites not yet implemented
            if (target == null) {
                Toast.makeText(a, "Error: Not Yet Implemented", Toast.LENGTH_SHORT).show();
                return;
            }

            // Don’t reopen same screen
            if (!a.getClass().equals(target)) {
                a.startActivity(new Intent(a, target));
                a.overridePendingTransition(0, 0);
                a.finish();
            }
        };

        if (navHome != null) navHome.setOnClickListener(go);
        if (navExplore != null) navExplore.setOnClickListener(go);
        if (navQR != null) navQR.setOnClickListener(go);
        if (navNotif != null) navNotif.setOnClickListener(go);
        if (navProfile != null) navProfile.setOnClickListener(go);
    }
}
