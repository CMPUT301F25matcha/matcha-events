package com.example.lotterysystemproject.Models;

import android.content.Context;
import android.provider.Settings;

/**
 * Simplest possible way to identify a user by device.
 * - Uses ANDROID_ID, which is unique to each device+app signing key.
 * - Currently requires no permissions or Firebase.
 * - Resets when the device is factory-reset or the app’s signing key changes.
 */
public class DeviceIdentityManager {

    /** Returns a unique user ID string for this device. */
    public static String getUserId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (androidId == null || androidId.isEmpty()) {
            // Fallback if ANDROID_ID not available
            androidId = java.util.UUID.randomUUID().toString().replace("-", "");
        }

        // Optional prefix that makes IDs recognizable as app-generated
        return "usr_" + androidId;
    }
}
