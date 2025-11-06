package com.example.lotterysystemproject.Views.Entrant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.Models.ProfilePrefs;
import com.example.lotterysystemproject.Models.User;
import com.example.lotterysystemproject.R;

/**
 * Provides entrants with an interface to permanently
 * delete their local profile data and return to the login screen.
 */
public class DeleteProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    private ProgressBar progress;
    private Button btnDelete, btnCancel;

    //private FirebaseManager fm;
    private ProfilePrefs prefs;
    private String userId;

    /**
     * Initializes the UI components, retrieves userId from the
     * intent extras, and sets up button listeners for deletion and cancellation.
     * @param savedInstanceState Contains saved instance data if the activity is being re-initialized
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        //fm = FirebaseManager.getInstance();
        prefs = new ProfilePrefs(getApplicationContext());
        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (userId == null || userId.trim().isEmpty()) {
            userId = "device-123"; // fallback demo ID
        }

        progress = findViewById(R.id.progress);
        btnDelete = findViewById(R.id.btn_delete);
        btnCancel = findViewById(R.id.btn_cancel);

        btnDelete.setOnClickListener(v -> confirmThenDelete());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Displays a confirmation dialog asking the user to verify that they want
     * to permanently delete their profile.
     */
    private void confirmThenDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete profile?")
                .setMessage("This will permanently delete your local profile data.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> performDeletion())
                .show();
    }

    /**
     * Performs the actual profile deletion.
     * Clears all local user data by calling deleteUser and clearUserPrefs
     */
    private void performDeletion() {
        setLoading(true);
        try {
            // Delete local profile data you keep (mock store)
            prefs.deleteUser(userId);

            // Clear  same prefs that UserInfo uses
            com.example.lotterysystemproject.Utils.AuthState.clearUserPrefs(this);

            // Go to login and wipe back stack
            android.widget.Toast.makeText(this, "Profile deleted", android.widget.Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, com.example.lotterysystemproject.Views.Entrant.UserInfoView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            setLoading(false);
        }
    }

    /**
     * Toggles the visibility of the progress bar and enables/disables the action buttons.
     * @param loading
     * - true to show the loading spinner and disable buttons;
     * - false to hide it and re-enable buttons.
     */
    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnDelete.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
    }

    // Firebase usage, will be commented out for demo and use alternate function
    /*
    private void confirmThenDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete profile?")
                .setMessage("This will anonymize your registrations, revoke notifications, and delete your profile. This can’t be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> performDeletion())
                .show();
    }


    private void performDeletion() {
        setLoading(true);

        // 1) Revoke FCM token (best-effort)
        fm.revokeFcmToken(new FirebaseManager.FirebaseCallback() {
            @Override public void onSuccess() { step2Anonymize(); }
            @Override public void onError(Exception e) { step2Anonymize(); } // proceed anyway
        });
    }

    private void step2Anonymize() {
        // 2) Anonymize registrations
        fm.anonymizeRegistrationsForUser(userId, new FirebaseManager.FirebaseCallback() {
            @Override public void onSuccess() { step3DeleteUser(); }
            @Override public void onError(Exception e) { step3DeleteUser(); } // proceed anyway
        });
    }

    private void step3DeleteUser() {
        // 3) Delete user document
        fm.deleteUser(userId, new FirebaseManager.FirebaseCallback() {
            @Override public void onSuccess() {
                setLoading(false);
                Toast.makeText(DeleteProfileActivity.this, "Profile deleted", Toast.LENGTH_LONG).show();
                // Optionally: clear local state / navigate out
                finish();
            }
            @Override public void onError(Exception e) {
                setLoading(false);
                Toast.makeText(DeleteProfileActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnDelete.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
    }

     */
}
