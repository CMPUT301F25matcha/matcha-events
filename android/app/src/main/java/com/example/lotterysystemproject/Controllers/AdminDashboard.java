package com.example.lotterysystemproject.Controllers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.databinding.AdminDashboardBinding;


/**
 * AdminDashboard serves as the main control panel for administrators,
 * providing navigation access to various management sections of the app.
 *
 * From this dashboard, an admin can:
 * - Browse/manage user profiles
 * - Browse/manage Images
 * - Browse/manage events
 *
 *
 * This fragment acts as a central panel for admin operations and uses Android Navigation
 * to move between different administrative sections.
 *
 *
 */
public class AdminDashboard extends Fragment {

    private AdminDashboardBinding binding;

    /**
     * Called to inflate the fragment layout and initialize the Firebase manager.
     *
     * @param inflater  The LayoutInflater used to inflate views in the fragment.
     * @param container The parent ViewGroup into which the fragment's UI should be attached.
     * @param savedInstanceState If not null, the fragment is being re-created from a previous state.
     * @return The root view of the inflated layout.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AdminDashboardBinding.inflate(inflater, container, false);

        return binding.getRoot();


    }

    /**
     * Called once the fragment's view has been created.
     *
     * This method initializes click listeners for navigation buttons
     * and handles back navigation to the previous fragment or activity.
     *
     * @param view The created view.
     * @param savedInstanceState The saved state of the fragment, if any.
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle back navigation
        binding.backArrow.setOnClickListener(v -> {
            if (!NavHostFragment.findNavController(AdminDashboard.this).navigateUp()) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Navigate to Browse Profiles section
        binding.btnBrowseProfiles.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminDashboard.this)
                        .navigate(R.id.action_adminDashboard_to_adminBrowseProfiles)
        );

        // Navigate to Browse Events section
        binding.btnBrowseEvents.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminDashboard.this)
                    .navigate(R.id.action_adminDashboard_to_adminBrowseEvents);

        });

        // Navigate to Browse Images section
        binding.btnBrowseImages.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminDashboard.this)
                    .navigate(R.id.action_adminDashboard_to_adminBrowseImages);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
