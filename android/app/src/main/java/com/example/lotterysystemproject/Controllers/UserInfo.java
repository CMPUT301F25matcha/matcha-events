package com.example.lotterysystemproject.Controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.lotterysystemproject.Models.User;
import com.example.lotterysystemproject.Views.Admin.AdminLoginActivity;
import com.example.lotterysystemproject.Views.Entrant.EntrantMainActivity;
import com.example.lotterysystemproject.databinding.UserInfoBinding;

public class UserInfo {
    public User collectUserInfo(UserInfoBinding binding) {
        String name = binding.userName.getText() != null ? binding.userName.getText().toString().trim() : "";
        String email = binding.userEmail.getText() != null ? binding.userEmail.getText().toString().trim() : "";
        String phone = binding.userPhone.getText() != null ? binding.userPhone.getText().toString().trim() : "";
        return new User("", name, email, phone);
    }

    public boolean validate(User model) {
        // Phone is optional; only require name and email
        return !isNullOrBlank(model.getName())
                && !isNullOrBlank(model.getEmail());
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

    public void persistInMemory(Context context, User model) {
        // Placeholder for persistence (e.g., SharedPreferences/DB)
    }

    public void navigateToEntrantHome(Activity activity) {
        Intent intent = new Intent(activity, EntrantMainActivity.class);
        activity.startActivity(intent);
    }

    public void navigateToAdminLogin(Activity activity) {
        Intent intent = new Intent(activity, AdminLoginActivity.class);
        activity.startActivity(intent);
    }

    public void handleContinue(Activity activity, UserInfoBinding binding) {
        hideValidationError(binding);
        User model = collectUserInfo(binding);
        if (!validate(model)) {
            showValidationError(binding);
            return;
        }
        model.setSignedUp(true);
        persistInMemory(activity, model);
        navigateToEntrantHome(activity);
    }

    public void handleSkip(Activity activity, UserInfoBinding binding) {
        hideValidationError(binding);
        User model = collectUserInfo(binding);
        model.setSignedUp(false);
        persistInMemory(activity, model);
        navigateToEntrantHome(activity);
    }
}