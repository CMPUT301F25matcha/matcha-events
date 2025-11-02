package com.example.lotterysystemproject.Views.Entrant;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.databinding.UserInfoBinding;

public class UserInfoView extends Fragment {

    private static final String TAG = "UserInfoView";
    private UserInfoBinding binding;

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
            binding.buttonFirst.setOnClickListener(v -> {
                if (getActivity() == null) {
                    Log.e(TAG, "Cannot start activity: Fragment not attached to activity.");
                    return;
                }

                // Create the Intent
                Intent intent = new Intent(getActivity(), EntrantMainActivity.class);

                // Use the flags to clear the back stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                try {
                    startActivity(intent);

                    // Finish the hosting activity ONLY if the startActivity succeeded
                    getActivity().finish();

                } catch (ActivityNotFoundException e) {
                    // This is the error we are diagnosing!
                    Log.e(TAG, "CRASH DIAGNOSIS: EntrantMainActivity not found in Manifest!", e);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}