package com.example.lotterysystemproject.Views.Entrant;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterysystemproject.Controllers.UserInfo;
import com.example.lotterysystemproject.databinding.UserInfoBinding;

public class UserInfoView extends AppCompatActivity {

    private UserInfoBinding binding;
    private final UserInfo controller = new UserInfo();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = UserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Existing buttons
        binding.buttonContinue.setOnClickListener(v -> controller.handleContinue(this, binding));
        binding.buttonSkip.setOnClickListener(v -> controller.handleSkip(this, binding));
        binding.adminLogin.setOnClickListener(v -> controller.navigateToAdminLogin(this));

        // 🆕 Organizer button — connects to your new handler
        if (binding.buttonOrganizer != null) {
            binding.buttonOrganizer.setOnClickListener(v -> controller.handleOrganizer(this, binding));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
