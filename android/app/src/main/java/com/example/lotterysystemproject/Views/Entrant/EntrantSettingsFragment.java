package com.example.lotterysystemproject.Views.Entrant;

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

public class EntrantSettingsFragment extends Fragment {

    public EntrantSettingsFragment() {}

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        // Back arrow in toolbar
        MaterialToolbar toolbar = v.findViewById(R.id.settings_toolbar);
        toolbar.setNavigationOnClickListener(back ->
                Navigation.findNavController(v).navigateUp()
        );
    }
}