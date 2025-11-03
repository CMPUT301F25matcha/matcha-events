package com.example.lotterysystemproject.Controllers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.Models.User;

public class UserProfileDialog extends DialogFragment {

    private final User user;

    public UserProfileDialog(User user) {
        this.user = user;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_user_profile, null);

        // Find your layout views
        TextView nameText = view.findViewById(R.id.dialog_name);
        TextView emailText = view.findViewById(R.id.dialog_email);
        TextView phoneText = view.findViewById(R.id.dialog_phone);
        TextView roleText = view.findViewById(R.id.dialog_role);
        Button removeButton = view.findViewById(R.id.dialog_remove_button);
        Button closeButton = view.findViewById(R.id.dialog_close_button);

        // Populate the fields
        nameText.setText(user.getName());
        emailText.setText(user.getEmail());
        phoneText.setText(user.getPhone());
        roleText.setText(user.getRole());


        // Handle Remove User
        removeButton.setOnClickListener(v -> {
            FirebaseManager.getInstance().deleteUser(user.getId(), new FirebaseManager.FirebaseCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), "User removed successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(requireContext(), "Failed to remove user: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                }

            });

        });

        // Handle Close button
        closeButton.setOnClickListener(v -> dismiss());

        // Build and return dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();

    }
}
