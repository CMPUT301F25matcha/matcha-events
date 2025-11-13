package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.R;

public class SearchResultsActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "EXTRA_QUERY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // You need to create this layout file: res/layout/activity_search_results.xml
        setContentView(R.layout.activity_search_results);

        String query = getIntent().getStringExtra(EXTRA_QUERY);

        if (query != null) {
            // Example: Set the title to the query
            setTitle("Results for: " + query);
            // Now you would perform your search/filtering logic
        }
    }
}
