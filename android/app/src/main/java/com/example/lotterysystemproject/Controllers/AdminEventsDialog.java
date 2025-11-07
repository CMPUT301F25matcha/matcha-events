package com.example.lotterysystemproject.Controllers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.lotterysystemproject.Models.EventAdmin;
import com.example.lotterysystemproject.Models.FirebaseManager;
import com.example.lotterysystemproject.R;

import java.text.DateFormat;
import java.util.Locale;

public class AdminEventsDialog extends DialogFragment {

    private final EventAdmin eventAdmin;

    public AdminEventsDialog(EventAdmin eventAdmin) {this.eventAdmin = eventAdmin;}

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

        eventNameText.setText(eventAdmin.getName());
        String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
                        .format(eventAdmin.getEventDate());
        eventDateText.setText(formattedDate);
        eventTimeText.setText(eventAdmin.getTime());
        eventLocationText.setText(eventAdmin.getLocation());
        eventDescriptionText.setText(eventAdmin.getDescription());

        // Handle Remove Event
        removeEventButton.setOnClickListener(v -> {
            FirebaseManager.getInstance().deleteEvent(eventAdmin.getId(), new FirebaseManager.FirebaseCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), "Event removed sucessfullly", Toast.LENGTH_SHORT).show();
                    Log.d("FirebaseManager", "Delete success");
                    dismiss();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(requireContext(), "Failed to remove user: " + e.getMessage(), Toast.LENGTH_SHORT).show();


                }
            });
        });

        // Handle Close Button
        closeEventButton.setOnClickListener(v -> dismiss());

        // Build and return dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();
    }


}