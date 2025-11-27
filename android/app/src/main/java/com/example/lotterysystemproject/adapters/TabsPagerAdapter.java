package com.example.lotterysystemproject.adapters;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.lotterysystemproject.views.fragments.organizer.MapViewFragment;
import com.example.lotterysystemproject.views.fragments.organizer.SelectedListFragment;
import com.example.lotterysystemproject.views.fragments.organizer.WaitingListFragment;

/**
 * Adapter for managing tabs in the Event Management screen.
 * Provides three fragments: Waiting List, Selected Entrants, and Map View.
 */
public class TabsPagerAdapter extends FragmentStateAdapter {

    private final String eventId;

    /**
     * Constructor that takes the parent fragment and eventId.
     *
     * @param fragment The parent fragment hosting the ViewPager2
     * @param eventId  The ID of the event being managed
     */
    public TabsPagerAdapter(@NonNull Fragment fragment, String eventId) {
        super(fragment);
        this.eventId = eventId;
    }

    /**
     * Creates the appropriate fragment for each tab position.
     *
     * @param position The tab position (0 = Waiting, 1 = Selected, 2 = Map)
     * @return The fragment for that tab
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        Bundle args = new Bundle();
        args.putString("eventId", eventId);

        switch (position) {
            case 0:
                // Waiting List Tab
                fragment = new WaitingListFragment();
                break;
            case 1:
                // Selected Entrants Tab
                fragment = new SelectedListFragment();
                break;
            case 2:
                // Map View Tab
                fragment = new MapViewFragment();
                break;
            default:
                fragment = new WaitingListFragment();
                break;
        }

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns the total number of tabs.
     *
     * @return 3 (Waiting, Selected, Map)
     */
    @Override
    public int getItemCount() {
        return 3;
    }
}
