package com.example.lotterysystemproject.Views.Organizer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterysystemproject.R;

public class OrganizerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_main);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}