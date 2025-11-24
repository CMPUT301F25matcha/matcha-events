package com.example.lotterysystemproject.controllers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import com.example.lotterysystemproject.models.User;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.firebasemanager.AdminRepository;

public class AdminOrganizerDialog extends DialogFragment {

    private final User user;

    public AdminOrganizerDialog(User user) {
        this.user = user;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_organizer, null);

        // Bind Views
        TextView organizerName = view.findViewById(R.id.dialog_organizer_name);
        TextView organizerEmail = view.findViewById(R.id.dialog_organizer_email);
        TextView organizerPhone = view.findViewById(R.id.dialog_organizer_phone);
        Button removeOrganizer = view.findViewById(R.id.dialog_remove_organizer_button);
        Button closeButton = view.findViewById(R.id.dialog_close_button);

        // Fill with organizer data
        organizerName.setText(user.getName());
        organizerEmail.setText(user.getEmail());
        organizerPhone.setText(user.getPhone());


        // Close Buttong
        closeButton.setOnClickListener(v -> dismiss());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();


    }



}