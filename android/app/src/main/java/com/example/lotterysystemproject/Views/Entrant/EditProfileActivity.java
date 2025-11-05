package com.example.lotterysystemproject.Views.Entrant;

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

import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.Models.User;
import com.example.lotterysystemproject.R;

import java.util.HashMap;
import java.util.Map;

/**
 * US 01.02.02: Entrant can update profile (name, email, phone).
 * Expects caller to pass EXTRA_USER_ID (String).
 */
public class EditProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    private EditText nameEt, emailEt, phoneEt;
    private Button saveBtn, cancelBtn;
    private ProgressBar progress;

    private FirebaseManager fm;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        fm = FirebaseManager.getInstance();
        userId = getIntent().getStringExtra(EXTRA_USER_ID);

        nameEt   = findViewById(R.id.input_name);
        emailEt  = findViewById(R.id.input_email);
        phoneEt  = findViewById(R.id.input_phone);
        saveBtn  = findViewById(R.id.btn_save);
        cancelBtn= findViewById(R.id.btn_cancel);
        progress = findViewById(R.id.progress);

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Missing userId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setLoading(true);
        fm.getUser(userId, user -> {
            if (user != null) {
                if (!TextUtils.isEmpty(user.getName()))  nameEt.setText(user.getName());
                if (!TextUtils.isEmpty(user.getEmail())) emailEt.setText(user.getEmail());
                if (!TextUtils.isEmpty(user.getPhone())) phoneEt.setText(user.getPhone());
            }
            setLoading(false);
        }, e -> {
            setLoading(false);
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });

        saveBtn.setOnClickListener(v -> attemptSave());
        cancelBtn.setOnClickListener(v -> finish());
    }

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
        if (!TextUtils.isEmpty(phone) && phone.length() < 7) {
            phoneEt.setError("Phone looks too short"); phoneEt.requestFocus(); return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", TextUtils.isEmpty(phone) ? null : phone);

        setLoading(true);
        fm.updateUser(userId, updates, new FirebaseManager.FirebaseCallback() {
            @Override public void onSuccess() {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
            @Override public void onError(Exception e) {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveBtn.setEnabled(!loading);
        cancelBtn.setEnabled(!loading);
        nameEt.setEnabled(!loading);
        emailEt.setEnabled(!loading);
        phoneEt.setEnabled(!loading);
    }

    private static String trim(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
