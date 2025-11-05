package com.example.lotterysystemproject.Controllers;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.lotterysystemproject.Models.Event;
import com.example.lotterysystemproject.R;

public class EventsDialog extends DialogFragment {

    private final Event event;

    public EventsDialog(Event event) {this.event = event;}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_event_details, null);

        TextView eventNameText = view.findViewById(R.id.dialog_event_name);
        TextView eventDateText = view.findViewById(R.id.dialog_event_date);
        TextView eventOrganizerNameText = view.findViewById(R.id.dialog_organizer_name);
        TextView eventDescriptionText = view.findViewById(R.id.dialog_event_description);
        Button removeEventButton = view.findViewById(R.id.dialog_event_remove_button);
        Button closeEventButton = view.findViewById(R.id.dialog_event_close_button);

        eventNameText.setText(event.getName());

    }


}