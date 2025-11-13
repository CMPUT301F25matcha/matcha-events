package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.lotterysystemproject.R;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Represents the settings screen accessible from the entrant's profile section of the app.
 * Provides an interface for users to view and modify their account preferences, and access application options,
 */
public class EntrantSettingsFragment extends Fragment {

    /** Default constructor required for proper fragment instantiation. */
    public EntrantSettingsFragment() {}

    /**
     * Inflates the layout for the settings screen.
     * @param inflater  Used to inflate the layout XML.
     * @param container Parent container that holds this fragment
     * @param savedInstanceState Previous instance state if re-created
     */
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_settings, container, false);
    }

    /**
     * Called after the fragment’s view hierarchy is created.
     * Initializes toolbar and sets back navigation click listener to return to the previous screen.
     * @param v Root view of fragment
     * @param savedInstanceState Previous instance state if re-created
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        // Back arrow in toolbar
        MaterialToolbar toolbar = v.findViewById(R.id.settings_toolbar);
        toolbar.setNavigationOnClickListener(back ->
                Navigation.findNavController(v).navigateUp()
        );
    }
}