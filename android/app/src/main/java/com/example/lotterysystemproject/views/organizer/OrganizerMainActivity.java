package com.example.lotterysystemproject.views.organizer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterysystemproject.R;

/**
 * Main activity for organizers.
 * Serves as the primary entry point for organizer-related features and navigation.
 */
public class OrganizerMainActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState The previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_main);
    }

    /**
     * Handles navigation when the "up" button is pressed.
     *
     * @return true if navigation was handled successfully.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
