package com.example.lotterysystemproject.views.entrant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.firebasemanager.RepositoryCallback;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.firebasemanager.UserRepository;
import com.example.lotterysystemproject.models.DeviceIdentityManager;
import com.example.lotterysystemproject.models.ProfilePrefs;
import com.example.lotterysystemproject.utils.AuthState;

/**
 * Screen that lets an entrant permanently delete (deactivate) their profile.
 * - Marks user document as inactive in Firestore
 * - Clears local profile/auth SharedPreferences
 * - Returns RESULT_OK to the calling fragment/activity on success
 */
public class DeleteProfileActivity extends AppCompatActivity {

    /** Intent extra key used by EntrantProfileFragment to pass the user id. */
    public static final String EXTRA_USER_ID =
            "com.example.lotterysystemproject.views.entrant.DeleteProfileActivity.EXTRA_USER_ID";

    private Button btnDelete;
    private Button btnCancel;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        // Resolve user id
        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (userId == null || userId.trim().isEmpty()) {
            // Fallback to device-based id
            userId = DeviceIdentityManager.getUserId(this);
        }

        // Wire views
        btnDelete = findViewById(R.id.btn_delete);
        btnCancel = findViewById(R.id.btn_cancel);

        // Cancel finishes and signals RESULT_CANCELED
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                setResult(RESULT_CANCELED);
                finish();
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmThenDelete());
        }
    }

    /** Show confirmation dialog before hitting Firebase. */
    private void confirmThenDelete() {
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, "No user id available.", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete profile?")
                .setMessage("This will deactivate your account and clear your local profile. " +
                        "This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> performDelete())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Actually call Firebase + clear local data. */
    private void performDelete() {

        final UserRepository repo = RepositoryProvider.getUserRepository();
        if (repo == null) {
            Toast.makeText(this, "User repository not available.", Toast.LENGTH_LONG).show();
            return;
        }

        repo.deactivateAccount(userId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Clear local profile & auth prefs
                new ProfilePrefs(DeleteProfileActivity.this).deleteUser(userId);
                AuthState.clearUserPrefs(DeleteProfileActivity.this);
                Toast.makeText(DeleteProfileActivity.this,
                        "Profile deleted.", Toast.LENGTH_LONG).show();

                // Notify caller that deletion succeeded
                setResult(RESULT_OK, new Intent());
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(DeleteProfileActivity.this,
                        "Delete failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
