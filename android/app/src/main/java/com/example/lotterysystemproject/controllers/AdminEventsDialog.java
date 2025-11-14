package com.example.lotterysystemproject.controllers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lotterysystemproject.firebasemanager.AdminRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.R;

import java.text.DateFormat;
import java.util.Locale;

public class AdminEventsDialog extends DialogFragment {
    private final Event event;
    private AdminRepository adminRepository;

    public AdminEventsDialog(Event event) {
        this.event = event;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminRepository = RepositoryProvider.getAdminRepository();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_event_details, null);

        TextView eventNameText = view.findViewById(R.id.dialog_event_name);
        TextView eventDateText = view.findViewById(R.id.dialog_event_date);
        //TextView eventOrganizerNameText = view.findViewById(R.id.dialog_organizer_name);
        TextView eventTimeText = view.findViewById(R.id.dialog_event_time);
        TextView eventLocationText = view.findViewById(R.id.dialog_event_location);
        TextView eventDescriptionText = view.findViewById(R.id.dialog_event_description);
        Button removeEventButton = view.findViewById(R.id.dialog_event_remove_button);
        Button closeEventButton = view.findViewById(R.id.dialog_event_close_button);

        // Set event details
        eventNameText.setText(event.getName());
        String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
                .format(event.getEventDate());
        eventDateText.setText(formattedDate);
        eventTimeText.setText(event.getEventTime());
        eventLocationText.setText(event.getLocation());
        eventDescriptionText.setText(event.getDescription());

        // Setup button listeners
        setupRemoveButton(removeEventButton);
        setupCloseButton(closeEventButton);

        // Build and return the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();
    }

    private void setupRemoveButton(Button removeEventButton) {
        removeEventButton.setOnClickListener(v -> {
            adminRepository.deleteEvent(event.getId(),
                    new AdminRepository.AdminCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(requireContext(),
                                    "Event removed successfully", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(requireContext(),
                                    "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void setupCloseButton(Button closeEventButton) {
        closeEventButton.setOnClickListener(v -> dismiss());
    }
}