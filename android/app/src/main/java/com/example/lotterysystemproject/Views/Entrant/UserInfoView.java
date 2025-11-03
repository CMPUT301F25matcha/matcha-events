package com.example.lotterysystemproject.Views.Entrant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterysystemproject.Controllers.UserInfoController;
import com.example.lotterysystemproject.databinding.UserInfoBinding;

public class UserInfoView extends Fragment {

    private UserInfoBinding binding;
    private final UserInfoController controller = new UserInfoController();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = UserInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (binding != null) {
            binding.buttonContinue.setOnClickListener(v -> controller.handleContinue(this, binding));
            binding.buttonSkip.setOnClickListener(v -> controller.handleSkip(this, binding));
            binding.adminLogin.setOnClickListener(v -> controller.navigateToAdminLogin(this));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}