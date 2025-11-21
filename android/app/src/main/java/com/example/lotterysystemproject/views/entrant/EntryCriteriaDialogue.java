package com.example.lotterysystemproject.views.entrant;

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
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.User;

public class EntryCriteriaDialogue extends DialogFragment {


    public EntryCriteriaDialogue() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_entry_criteria, null);

        Button closeButton = view.findViewById(R.id.dialog_close_button);

        // Handle Close button
        closeButton.setOnClickListener(v -> dismiss());

        // Build and return dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();

    }
}
