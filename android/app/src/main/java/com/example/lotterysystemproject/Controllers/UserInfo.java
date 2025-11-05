package com.example.lotterysystemproject.Controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import com.example.lotterysystemproject.Models.User;
import com.example.lotterysystemproject.Views.Admin.AdminLoginActivity;
import com.example.lotterysystemproject.Views.Entrant.EntrantMainActivity;
import com.example.lotterysystemproject.databinding.UserInfoBinding;

import java.util.UUID;

/**
 * Controller class for user information sign-up flow.
 * Handles user input validation, data collection, and navigation logic
 * for the user registration process.
 *
 * Part of the MVC pattern where this class serves as the controller,
 * mediating between the UserInfoView and User model.
 *
 * Related User Stories:
 * - US 01.02.01: Collecting personal information (name, email, phone)
 * - US 01.07.01: Device-based user identification
 *
 * @see com.example.lotterysystemproject.Views.Entrant.UserInfoView
 * @see User
 */
public class UserInfo {

    /**
     * Collects user information from the input fields in the binding.
     * Creates a new User object with a generated unique ID and the
     * information entered by the user.
     *
     * @param binding The view binding containing the input fields
     * @return A User object populated with the collected information
     */
    public User collectUserInfo(UserInfoBinding binding) {
        String name = binding.userName.getText() != null ? binding.userName.getText().toString().trim() : "";
        String email = binding.userEmail.getText() != null ? binding.userEmail.getText().toString().trim() : "";
        String phone = binding.userPhone.getText() != null ? binding.userPhone.getText().toString().trim() : "";

        // Generate unique ID for testing purposes
        String uniqueId = generateUniqueId();

        return new User(uniqueId, name, email, phone);
    }

    /**
     * Generates a unique identifier for the user.
     *
     * For testing purposes, this generates a UUID. In production with Firebase,
     * this will be replaced with Firebase Authentication UID.
     *
     * Implementation note: US 01.07.01 requires device-based identification.
     * For a more persistent device ID, consider using:
     * Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
     *
     * @return A unique identifier string
     */
    private String generateUniqueId() {
        // Generate UUID for testing
        // TODO: Replace with Firebase Authentication UID when Firebase is implemented
        return UUID.randomUUID().toString();
    }

    /**
     * Checks if a user has already completed the sign-up process.
     * This method checks SharedPreferences for an existing userId.
     *
     * Used on app startup to determine whether to show the sign-up screen
     * or navigate directly to the main activity.
     *
     * @param context Application context for accessing SharedPreferences
     * @return true if user has previously signed up (userId exists), false otherwise
     */
    public boolean isUserAlreadySignedUp(Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        return userId != null;
    }

    /**
     * Alternative method to generate a device-specific unique ID.
     * This ID persists across app reinstalls on the same device.
     *
     * Note: This method requires a Context parameter and should be called
     * from methods that have access to Context (like handleContinue/handleSkip).
     *
     * @param context Application context
     * @return Device-specific unique identifier
     */
    private String generateDeviceId(Context context) {
        // Get Android device ID (persists across app installs)
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     * Validates the user information.
     * Ensures that required fields (name and email) are not null or blank.
     * Phone number is optional and not validated.
     *
     * @param model The User object to validate
     * @return true if validation passes (name and email are present), false otherwise
     */
    public boolean validate(User model) {
        // Phone is optional; only require name and email
        return !isNullOrBlank(model.getName())
                && !isNullOrBlank(model.getEmail());
    }

    /**
     * Checks if a string value is null or blank (empty or whitespace only).
     *
     * @param value The string to check
     * @return true if the value is null or blank, false otherwise
     */
    private boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Displays the validation error message in the UI.
     * Makes the error message text view visible to inform the user
     * that their input is incomplete.
     *
     * @param binding The view binding containing the error message view
     */
    public void showValidationError(UserInfoBinding binding) {
        binding.errorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the validation error message in the UI.
     * Makes the error message text view invisible/gone.
     *
     * @param binding The view binding containing the error message view
     */
    public void hideValidationError(UserInfoBinding binding) {
        binding.errorMessage.setVisibility(View.GONE);
    }

    /**
     * Persists user information in memory/storage.
     *
     * Currently saves user data to SharedPreferences for local storage.
     * In production, this should be synced with Firebase Firestore.
     *
     * The same SharedPreferences key ("UserPrefs") is used throughout the app
     * to maintain consistent user session data.
     *
     * @param context Application context for accessing storage
     * @param model   The User object to persist
     */
    public void persistInMemory(Context context, User model) {
        // Save to SharedPreferences for local session management
        android.content.SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("userId", model.getId())
                .putString("userName", model.getName())
                .putString("userEmail", model.getEmail())
                .putString("userPhone", model.getPhone())
                .putString("userRole", model.getRole())
                .putBoolean("signedUp", model.getSignedUp())
                .apply();

        // TODO: Sync with Firebase Firestore when implemented
        // FirebaseManager.getInstance().saveUser(model, callback);
    }

    /**
     * Navigates to the entrant home screen.
     * Starts the EntrantMainActivity and finishes the current activity.
     *
     * @param activity The current activity from which navigation is initiated
     */
    public void navigateToEntrantHome(Activity activity) {
        Intent intent = new Intent(activity, EntrantMainActivity.class);
        activity.startActivity(intent);
        activity.finish(); // Prevent back navigation to sign-up screen
    }

    /**
     * Navigates to the admin login screen.
     * Starts the AdminLoginActivity.
     *
     * @param activity The current activity from which navigation is initiated
     */
    public void navigateToAdminLogin(Activity activity) {
        Intent intent = new Intent(activity, AdminLoginActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Handles the "Continue" button click.
     * Validates user input, persists data if valid, and navigates to home screen.
     * If validation fails, displays an error message.
     *
     * Related User Story: US 01.02.01 - Providing personal information
     *
     * @param activity The current activity
     * @param binding  The view binding containing user input fields
     */
    public void handleContinue(Activity activity, UserInfoBinding binding) {
        hideValidationError(binding);
        User model = collectUserInfo(binding);

        if (!validate(model)) {
            showValidationError(binding);
            return;
        }

        model.setSignedUp(true);
        model.setRole("entrant"); // Default role for users who sign up
        persistInMemory(activity, model);
        navigateToEntrantHome(activity);
    }

    /**
     * Handles the "Skip" button click.
     * Allows users to skip the sign-up process and continue with minimal information.
     * Creates a user profile without personal details and navigates to home screen.
     *
     * Related User Story: US 01.07.01 - Device-based identification without username/password
     *
     * @param activity The current activity
     * @param binding  The view binding containing user input fields
     */
    public void handleSkip(Activity activity, UserInfoBinding binding) {
        hideValidationError(binding);
        User model = collectUserInfo(binding);

        model.setSignedUp(false);
        model.setRole("entrant"); // Default role for users who skip
        persistInMemory(activity, model);
        navigateToEntrantHome(activity);
    }
}