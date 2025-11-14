package com.example.lotterysystemproject.views.entrant;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.controllers.UserInfo;
import com.example.lotterysystemproject.databinding.UserInfoBinding;

/**
 * View class for the user information sign-up screen.
 * This activity allows entrants to provide their personal information
 * including name, email, and optional phone number.
 *
 * Part of the MVC pattern where this class handles UI inflation and
 * delegates user interactions to the UserInfo controller.
 *
 * Related User Stories:
 * - US 01.02.01: As an entrant, I want to provide my personal information
 * - US 01.07.01: As an entrant, I want to be identified by my device
 *
 * @see UserInfo
 * @see com.example.lotterysystemproject.models.User
 */
public class UserInfoView extends AppCompatActivity {

    private UserInfoBinding binding;
    private final UserInfo controller = new UserInfo();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = UserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonContinue.setOnClickListener(v -> controller.handleContinue(this, binding));
        binding.buttonSkip.setOnClickListener(v -> controller.handleSkip(this, binding));
        binding.adminLogin.setOnClickListener(v -> controller.navigateToAdminLogin(this));
        binding.buttonOrganizer.setOnClickListener(v -> controller.navigateToOrganizerHome(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}