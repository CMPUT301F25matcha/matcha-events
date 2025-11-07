package com.example.lotterysystemproject.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.lotterysystemproject.R;

public class BottomNavigationHelper {
    
    public enum NavItem {
        HOME,
        EXPLORE,
        QR_SCANNER,
        NOTIFICATIONS,
        PROFILE
    }
    
    /**
     * Sets the selected state for a navigation item
     * @param navItem The navigation item to select
     * @param homeLayout Home navigation layout
     * @param exploreLayout Explore navigation layout
     * @param qrScannerLayout QR Scanner navigation layout
     * @param notificationsLayout Notifications navigation layout
     * @param profileLayout Profile navigation layout
     */
    public static void setSelectedItem(
            NavItem navItem,
            @Nullable LinearLayout homeLayout,
            @Nullable LinearLayout exploreLayout,
            @Nullable LinearLayout qrScannerLayout,
            @Nullable LinearLayout notificationsLayout,
            @Nullable LinearLayout profileLayout) {

        if (homeLayout == null || exploreLayout == null || qrScannerLayout == null
                || notificationsLayout == null || profileLayout == null) {
            // Gracefully bail if bar is missing
            return;
        }
        // Reset all items to unselected state
        resetAllItems(homeLayout, exploreLayout, qrScannerLayout, notificationsLayout, profileLayout);
        
        // Set the selected item
        switch (navItem) {
            case HOME:
                setSelectedState(homeLayout.findViewById(R.id.nav_home_icon), null);
                break;
            case EXPLORE:
                setSelectedState(exploreLayout.findViewById(R.id.nav_explore_icon), null);
                break;
            case QR_SCANNER:
                setSelectedState(qrScannerLayout.findViewById(R.id.nav_qr_scanner_icon), null);
                break;
            case NOTIFICATIONS:
                setSelectedState(notificationsLayout.findViewById(R.id.nav_notifications_icon), null);
                break;
            case PROFILE:
                ImageView profileIcon = profileLayout.findViewById(R.id.nav_profile_icon);
                View profileBorder = profileLayout.findViewById(R.id.profile_icon_border);
                setSelectedState(profileIcon, profileBorder);
                break;
        }
    }
    
    /**
     * Resets all navigation items to unselected state
     */
    private static void resetAllItems(
            LinearLayout homeLayout,
            LinearLayout exploreLayout,
            LinearLayout qrScannerLayout,
            LinearLayout notificationsLayout,
            LinearLayout profileLayout) {
        
        // Reset home
        ImageView homeIcon = homeLayout.findViewById(R.id.nav_home_icon);
        homeIcon.setImageResource(R.drawable.ic_home);
        
        // Reset explore
        ImageView exploreIcon = exploreLayout.findViewById(R.id.nav_explore_icon);
        exploreIcon.setImageResource(R.drawable.ic_compass);
        
        // Reset QR scanner
        ImageView qrIcon = qrScannerLayout.findViewById(R.id.nav_qr_scanner_icon);
        qrIcon.setImageResource(R.drawable.ic_qr_code);
        
        // Reset notifications
        ImageView notificationsIcon = notificationsLayout.findViewById(R.id.nav_notifications_icon);
        notificationsIcon.setImageResource(R.drawable.ic_bell);
        
        // Reset profile
        ImageView profileIcon = profileLayout.findViewById(R.id.nav_profile_icon);
        View profileBorder = profileLayout.findViewById(R.id.profile_icon_border);
        profileIcon.setImageResource(R.drawable.ic_account_circle);
        if (profileBorder != null) {
            profileBorder.setVisibility(View.GONE);
        }
    }
    
    /**
     * Sets the selected state for an icon
     */
    private static void setSelectedState(ImageView icon, View border) {
        if (icon == null) return;
        
        int iconId = icon.getId();
        
        if (iconId == R.id.nav_home_icon) {
            icon.setImageResource(R.drawable.ic_home_selected);
        } else if (iconId == R.id.nav_explore_icon) {
            icon.setImageResource(R.drawable.ic_compass_selected);
        } else if (iconId == R.id.nav_qr_scanner_icon) {
            icon.setImageResource(R.drawable.ic_qr_code_selected);
        } else if (iconId == R.id.nav_notifications_icon) {
            icon.setImageResource(R.drawable.ic_bell_selected);
        } else if (iconId == R.id.nav_profile_icon) {
            icon.setImageResource(R.drawable.ic_account_circle_selected);
            //if (border != null) {
            //    border.setVisibility(View.VISIBLE);
            //}
        }
    }
}

