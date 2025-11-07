package com.example.lotterysystemproject.Views.Entrant;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.Controllers.UserInfo;
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
 * @see com.example.lotterysystemproject.Models.User
 */
public class UserInfoView extends AppCompatActivity {

    private UserInfoBinding binding;
    private final UserInfo controller = new UserInfo();

    /**
     * Called when the activity is first created.
     * Checks if the user has already completed sign-up. If so, skips this screen
     * and navigates directly to the main activity. Otherwise, inflates the layout
     * and sets up click listeners for the continue, skip, and admin login buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this contains the data
     *                          it most recently supplied. Otherwise null.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user has already signed up
        if (controller.isUserAlreadySignedUp(this)) {
            // User exists, skip sign-up and go to main activity
            controller.navigateToEntrantHome(this);
            return;
        }

        // First time user, show sign-up screen
        binding = UserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up button click listeners, delegating to controller
        // Existing buttons
        binding.buttonContinue.setOnClickListener(v -> controller.handleContinue(this, binding));
        binding.buttonSkip.setOnClickListener(v -> controller.handleSkip(this, binding));
//        binding.adminLogin.setOnClickListener(v -> controller.navigateToAdminLogin(this));
        binding.adminLogin.setOnClickListener(v -> controller.navigateToAdminLogin(this));

        // 🆕 Organizer button — connects to your new handler
        if (binding.buttonOrganizer != null) {
            binding.buttonOrganizer.setOnClickListener(v -> controller.handleOrganizer(this, binding));
        }
    }

    /**
     * Called before the activity is destroyed.
     * Cleans up the view binding reference to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}