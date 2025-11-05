package com.example.lotterysystemproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.Models.Registration;
import com.example.lotterysystemproject.Models.User;
import com.example.lotterysystemproject.R;

import java.util.List;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "FirestoreTest";
    private static final String TEST_USER_ID = "test001";
    private FirebaseManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use the test layout you already have
        setContentView(R.layout.activity_test);

        try {
            fm = FirebaseManager.getInstance();

            // 1) Create / update a dummy user
            User user = new User(TEST_USER_ID, "Zifan", "zifan@example.com", "5871271234");
            fm.addUser(user, new FirebaseManager.FirebaseCallback() {
                @Override public void onSuccess() {
                    Log.d(TAG, "✅ User added");
                    Toast.makeText(TestActivity.this, "User added", Toast.LENGTH_SHORT).show();

                    // 2) Create/merge a registration record
                    fm.upsertRegistrationOnJoin(
                            TEST_USER_ID,
                            "event_swim101",
                            "Beginner Swimming Lessons",
                            new FirebaseManager.FirebaseCallback() {
                                @Override public void onSuccess() {
                                    Log.d(TAG, "✅ Registration created");
                                    Toast.makeText(TestActivity.this, "Registration created", Toast.LENGTH_SHORT).show();

                                    // 3) Listen for realtime updates
                                    startListening();
                                }
                                @Override public void onError(Exception e) {
                                    Log.e(TAG, "❌ Registration create failed", e);
                                    Toast.makeText(TestActivity.this, "Reg failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                }
                @Override public void onError(Exception e) {
                    Log.e(TAG, "❌ addUser failed", e);
                    Toast.makeText(TestActivity.this, "addUser failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "🔥 Crash in onCreate", t);
            Toast.makeText(this, "Crash: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startListening() {
        try {
            fm.listenUserRegistrations(TEST_USER_ID, new FirebaseManager.RegistrationsListener() {
                @Override public void onChanged(List<Registration> items) {
                    Log.d(TAG, "📜 History size=" + (items == null ? 0 : items.size()));
                    if (items != null) {
                        for (Registration r : items) {
                            Log.d(TAG, "→ " + r.getEventTitleSnapshot() + " | " + r.getStatus() + " | " + r.getUpdatedAt());
                        }
                    }
                }
                @Override public void onError(Exception e) {
                    Log.e(TAG, "🔥 listen error", e);
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "🔥 Crash starting listener", t);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fm != null) fm.stopListeningUserRegistrations();
    }
}
