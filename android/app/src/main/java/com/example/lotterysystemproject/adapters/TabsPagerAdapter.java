package com.example.lotterysystemproject.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.lotterysystemproject.views.fragments.organizer.SelectedListFragment;
import com.example.lotterysystemproject.views.fragments.organizer.WaitingListFragment;

/**
 * Adapter for managing tabs in the organizer view pager.
 * Provides fragments for the waiting list and selected entrants list.
 */
public class TabsPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructs a TabsPagerAdapter associated with the given fragment.
     *
     * @param fragment The parent fragment hosting the ViewPager2.
     */
    public TabsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WaitingListFragment();
            case 1:
                return new SelectedListFragment();
            default:
                return new WaitingListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Waiting and Selected tabs
    }
}
