package com.example.lotterysystemproject.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.lotterysystemproject.Views.fragments.organizer.SelectedListFragment;
import com.example.lotterysystemproject.Views.fragments.organizer.WaitingListFragment;

public class TabsPagerAdapter extends FragmentStateAdapter {

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
        return 2; // Waiting + Selected tabs
    }
}