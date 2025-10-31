package com.example.lotterysystemproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // or your test layout

        // Create a dummy user
        User user = new User("test001", "Zifan", "zifan@example.com", "5871271234");

        FirebaseManager.getInstance().addUser(user, new FirebaseManager.FirebaseCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(TestActivity.this, "✅ User added successfully!", Toast.LENGTH_SHORT).show();
                Log.d("FirebaseTest", "User added successfully!");
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(TestActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseTest", "Error adding user", e);
            }
        });
    }
}
