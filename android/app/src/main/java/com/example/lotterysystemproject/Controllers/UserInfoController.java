package com.example.lotterysystemproject.Controllers;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.example.lotterysystemproject.Models.UserModel;
import com.example.lotterysystemproject.Views.Entrant.EntrantMainActivity;
import com.example.lotterysystemproject.databinding.UserInfoBinding;
import com.example.lotterysystemproject.Views.Admin.AdminLoginActivity;
public class UserInfoController {
    public UserModel collectUserInfo(UserInfoBinding binding) {
        String name = binding.userName.getText() != null ? binding.userName.getText().toString().trim() : "";
        String email = binding.userEmail.getText() != null ? binding.userEmail.getText().toString().trim() : "";
        String phone = binding.userPhone.getText() != null ? binding.userPhone.getText().toString().trim() : "";
        return new UserModel(name, email, phone);
    }

    public boolean validate(UserModel model) {
        // Phone is optional; only require name and email
        return !isNullOrBlank(model.getUserName())
                && !isNullOrBlank(model.getUserEmail());
    }

    private boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public void showValidationError(UserInfoBinding binding) {
        binding.errorMessage.setVisibility(View.VISIBLE);
    }

    public void hideValidationError(UserInfoBinding binding) {
        binding.errorMessage.setVisibility(View.GONE);
    }

    public void persistInMemory(Context context, UserModel model) {
        // Placeholder for persistence (e.g., SharedPreferences/DB)
    }

    public void navigateToEntrantHome(Fragment fragment) {
        if (fragment.getActivity() == null) return;
        Intent intent = new Intent(fragment.getActivity(), EntrantMainActivity.class);
        fragment.startActivity(intent);
    }

    public void navigateToAdminLogin(Fragment fragment) {
        if (fragment.getActivity() == null) return;
        Intent intent = new Intent(fragment.getActivity(), AdminLoginActivity.class);
        fragment.startActivity(intent);
    }

    public void handleContinue(Fragment fragment, UserInfoBinding binding) {
        hideValidationError(binding);
        UserModel model = collectUserInfo(binding);
        if (!validate(model)) {
            showValidationError(binding);
            return;
        }
        model.setDetailsProvided(true);
        persistInMemory(fragment.getContext(), model);
        navigateToEntrantHome(fragment);
    }

    public void handleSkip(Fragment fragment, UserInfoBinding binding) {
        hideValidationError(binding);
        UserModel model = collectUserInfo(binding);
        model.setDetailsProvided(false);
        persistInMemory(fragment.getContext(), model);
        navigateToEntrantHome(fragment);
    }
}