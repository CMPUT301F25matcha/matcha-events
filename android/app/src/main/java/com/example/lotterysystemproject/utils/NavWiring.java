package com.example.lotterysystemproject.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.views.entrant.QRCodeScannerActivity;

/**
 * Utility class for wiring and managing bottom navigation bar across multiple entrant activities.
 * Helps ensure consistent navigation behavior.
 */
public final class NavWiring {

    /** Default constructor required for proper fragment instantiation. */
    private NavWiring() {}

    /**
     * Sets up navigation listeners for the bottom navigation bar.
     * When a tab for a different screen is tapped, the current
     * activity is finished and the new one is launched without animation.
     *
     * @param a        Current activity hosting the navigation bar.
     * @param home     Target class for the Home screen activity.
     * @param explore  Target class for the Explore screen activity
     * @param notif    Target class for the Notifications screen activity.
     * @param profile  Target class for the Profile screen activity.
     */
    public static void wire(Activity a,
                            Class<?> home,
                            Class<?> explore,
                            Class<?> notif,
                            Class<?> profile) {

        LinearLayout navHome = a.findViewById(R.id.nav_home);
        LinearLayout navQR = a.findViewById(R.id.nav_qr_scanner);
        LinearLayout navNotif = a.findViewById(R.id.nav_notifications);
        LinearLayout navProfile = a.findViewById(R.id.nav_profile);

        // QR Scanner - Special handling (doesn't finish current activity)
        if (navQR != null) {
            navQR.setOnClickListener(v -> {
                Intent intent = new Intent(a, QRCodeScannerActivity.class);
                a.startActivity(intent);
                // Don't finish() - user should return to current screen after scanning
            });
        }

        // Other navigation items - Standard handling
        View.OnClickListener go = v -> {
            Class<?> target =
                    v.getId() == R.id.nav_home ? home :
                            v.getId() == R.id.nav_notifications ? notif : profile;

            // Toast for activities not yet implemented
            if (target == null) {
                Toast.makeText(a, "Error: Not Yet Implemented", Toast.LENGTH_SHORT).show();
                return;
            }

            // Don't reopen same screen
            if (!a.getClass().equals(target)) {
                a.startActivity(new Intent(a, target));
                a.overridePendingTransition(0, 0);
                a.finish();
            }
        };

        if (navHome != null) navHome.setOnClickListener(go);
        if (navNotif != null) navNotif.setOnClickListener(go);
        if (navProfile != null) navProfile.setOnClickListener(go);
    }
}