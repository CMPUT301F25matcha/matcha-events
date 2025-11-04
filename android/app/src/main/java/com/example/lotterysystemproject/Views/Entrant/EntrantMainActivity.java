package com.example.lotterysystemproject.Views.Entrant;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterysystemproject.Helpers.EventListHelper;
import com.example.lotterysystemproject.databinding.EventViewsBinding;

public class EntrantMainActivity extends AppCompatActivity {
    private EventViewsBinding binding;
    private EventListHelper eventListHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = EventViewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup search view
        SearchView searchView = binding.eventSearchBar;
        if (searchView != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ComponentName componentName = new ComponentName(this, SearchableActivity.class);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
            searchView.setIconifiedByDefault(false);
        }

        // Setup events list
        eventListHelper = new EventListHelper(this, binding.eventsListContainer);
        eventListHelper.loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events list when returning to this activity
        if (eventListHelper != null) {
            eventListHelper.loadEvents();
        }
    }
}
