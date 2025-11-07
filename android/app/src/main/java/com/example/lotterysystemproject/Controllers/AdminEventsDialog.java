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

/**
 * AdminEventsDialog is a dialog fragment that displays detailed information
 * about a specific EventAdmin instance for administrators.
 *
 * This dialog provides an interactive view that shows the event’s:
 * - Name
 * - Date and time
 * - Location
 * - Description of event
 *
 * Administrators can also delete the event directly from the dialog using the
 * "Remove Event" button, which communicates FirebaseManager to remove
 * the event from Firestore.
 *
 */
public class AdminEventsDialog extends DialogFragment {

    /** The EventAdmin object representing the event to display. */
    private final EventAdmin eventAdmin;

    /**
     * Constructs a new AdminEventsDialog to display the details of a specific event.
     *
     * @param eventAdmin The EventAdmin object whose details will be shown in the dialog.
     */
    public AdminEventsDialog(EventAdmin eventAdmin) {this.eventAdmin = eventAdmin;}

    /**
     * Creates and returns the dialog that displays detailed information about the selected event.
     *
     * This method inflates the dialog layout, populates the event data fields,
     * and sets up listeners for "Remove" and "Close" buttons.
     *
     *
     * @param savedInstanceState The previously saved instance state, if any.
     * @return A fully configured Dialog instance that is ready for display.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_event_details, null);


        // Initialize UI components
        TextView eventNameText = view.findViewById(R.id.dialog_event_name);
        TextView eventDateText = view.findViewById(R.id.dialog_event_date);
        //TextView eventOrganizerNameText = view.findViewById(R.id.dialog_organizer_name);
        TextView eventTimeText = view.findViewById(R.id.dialog_event_time);
        TextView eventLocationText = view.findViewById(R.id.dialog_event_location);
        TextView eventDescriptionText = view.findViewById(R.id.dialog_event_description);
        Button removeEventButton = view.findViewById(R.id.dialog_event_remove_button);
        Button closeEventButton = view.findViewById(R.id.dialog_event_close_button);

        // Populate event details
        eventNameText.setText(eventAdmin.getName());
        String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
                        .format(eventAdmin.getEventDate());
        eventDateText.setText(formattedDate);
        eventTimeText.setText(eventAdmin.getTime());
        eventLocationText.setText(eventAdmin.getLocation());
        eventDescriptionText.setText(eventAdmin.getDescription());

        // Handle "Remove Event" button click
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

        // Handle "Close" button click to dismiss the dialog
        closeEventButton.setOnClickListener(v -> dismiss());

        // Build and return dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();
    }


}