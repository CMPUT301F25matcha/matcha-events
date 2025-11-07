package com.example.lotterysystemproject.Views.Entrant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.Models.EventFirebase;
import com.example.lotterysystemproject.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows entrants to edit and update their personal profile information such as name, email, and phone number.
 * Prefills from SharedPreferences (same source as Profile screen) and
 * writes back to SharedPreferences on save. Also attempts a Firestore update if available.
 */
public class EditProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    private EditText nameEt, emailEt, phoneEt;
    private Button saveBtn, cancelBtn;
    private ProgressBar progress;

    private String userId;
    private SharedPreferences prefs;
    private EventFirebase fm;


    /**
     * Initializes the Edit Profile screen.
     * Loads user data from SharedPreferences, pre-fills the form, and sets up listeners for Save and Cancel buttons.
     * @param savedInstanceState the saved state bundle (if any).
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        fm = EventFirebase.getInstance();
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Resolve userId: intent extra → prefs → fallback
        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (TextUtils.isEmpty(userId)) {
            userId = prefs.getString("userId", "unknown");
        }

        nameEt   = findViewById(R.id.input_name);
        emailEt  = findViewById(R.id.input_email);
        phoneEt  = findViewById(R.id.input_phone);
        saveBtn  = findViewById(R.id.btn_save);
        cancelBtn= findViewById(R.id.btn_cancel);
        progress = findViewById(R.id.progress);

        // Prefill directly from SharedPreferences (this matches your Profile fragment)
        prefillFromPrefs();

        saveBtn.setOnClickListener(v -> attemptSave());
        cancelBtn.setOnClickListener(v -> finish());
    }

    /**
     * Prefills form fields using values from SharedPreferences.
     */
    private void prefillFromPrefs() {
        String name  = prefs.getString("userName",  "");
        String email = prefs.getString("userEmail", "");
        String phone = prefs.getString("userPhone", "");

        nameEt.setText(name);
        emailEt.setText(email);
        phoneEt.setText(phone);
    }

    /**
     * Validates user input and saves updated profile data.
     * Updates both local and remote Firestore (once integrated).
     * Displays appropriate error messages for invalid input and progress indicators during save.
     */
    private void attemptSave() {
        String name  = trim(nameEt);
        String email = trim(emailEt);
        String phone = trim(phoneEt);

        if (TextUtils.isEmpty(name)) {
            nameEt.setError("Name required"); nameEt.requestFocus(); return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Valid email required"); emailEt.requestFocus(); return;
        }

        setLoading(true);

        // 1) Save to SharedPreferences (source of truth for your current flow)
        prefs.edit()
                .putString("userName",  name)
                .putString("userEmail", email)
                .putString("userPhone", phone)
                .apply();

        // 2) Best-effort Firestore update (safe to keep; harmless with mock FM)
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", TextUtils.isEmpty(phone) ? null : phone);

        fm.updateUser(userId, updates, new EventFirebase.FirebaseCallback() {
            @Override public void onSuccess() {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onError(Exception e) {
                // Even if Firestore fails (e.g., demo/mock), we already updated local prefs.
                setLoading(false);
                Toast.makeText(EditProfileActivity.this,
                        "Saved locally. Cloud update failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * Toggles visibility of progress bar and enables/disables buttons during save operations.
     * @param loading true to show loading state, false to restore interactivity.
     */
    private void setLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (saveBtn != null)  saveBtn.setEnabled(!loading);
        if (cancelBtn != null) cancelBtn.setEnabled(!loading);
    }

    private static String trim(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
