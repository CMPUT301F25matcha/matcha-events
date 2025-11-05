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

public class AdminDashboard extends Fragment {

    private AdminDashboardBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AdminDashboardBinding.inflate(inflater, container, false);






        return binding.getRoot();


    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.backArrow.setOnClickListener(v -> {
            if (!NavHostFragment.findNavController(AdminDashboard.this).navigateUp()) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        binding.btnBrowseProfiles.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminDashboard.this)
                        .navigate(R.id.action_adminDashboard_to_adminBrowseProfiles)
        );
        binding.btnBrowseEvents.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminDashboard.this)
                    .navigate(R.id.action_adminDashboard_to_adminBrowseEvents);

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
