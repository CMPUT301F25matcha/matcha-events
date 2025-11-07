package com.example.lotterysystemproject.Views.fragments.organizer;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Models.Entrant;
import java.util.ArrayList;
import java.util.List;

public class LotterySuccessDialogFragment extends DialogFragment {

    private List<Entrant> winners;

    public static LotterySuccessDialogFragment newInstance(List<Entrant> winners) {
        LotterySuccessDialogFragment fragment = new LotterySuccessDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("winners", new ArrayList<>(winners));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_lottery_success, null);

        // Get winners from arguments
        if (getArguments() != null) {
            winners = (List<Entrant>) getArguments().getSerializable("winners");
        }

        // Initialize views
        TextView successMessage = view.findViewById(R.id.success_message);
        TextView winnersList = view.findViewById(R.id.winners_list);
        Button viewSelectedButton = view.findViewById(R.id.view_selected_button);

        // Set message
        successMessage.setText(winners.size() + " entrants selected and notified!");

        // Build winners list text
        StringBuilder winnersText = new StringBuilder();
        int displayCount = Math.min(3, winners.size());

        for (int i = 0; i < displayCount; i++) {
            winnersText.append("• ").append(winners.get(i).getName()).append("\n");
        }

        if (winners.size() > 3) {
            winnersText.append("... ").append(winners.size() - 3).append(" more");
        }

        winnersList.setText(winnersText.toString());

        // View Selected button - switches to Selected tab
        viewSelectedButton.setOnClickListener(v -> {
            dismiss();
            // Parent fragment will handle switching to Selected tab
            if (getParentFragment() instanceof EventManagementFragment) {
                ((EventManagementFragment) getParentFragment()).switchToSelectedTab();
            }
        });

        builder.setView(view);
        setCancelable(false); // Must click button to close
        return builder.create();
    }
}