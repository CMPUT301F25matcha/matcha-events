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

/**
 * DialogFragment displayed after a successful lottery draw.
 * <p>
 * Shows the number of entrants selected, displays a preview list of their names,
 * and provides an option for the organizer to view the selected entrants tab.
 * </p>
 */
public class LotterySuccessDialogFragment extends DialogFragment {

    /** List of entrants selected as lottery winners. */
    private List<Entrant> winners;

    /**
     * Creates a new instance of this dialog fragment with the given list of winners.
     *
     * @param winners List of selected entrants
     * @return A configured {@link LotterySuccessDialogFragment} instance
     */
    public static LotterySuccessDialogFragment newInstance(List<Entrant> winners) {
        LotterySuccessDialogFragment fragment = new LotterySuccessDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("winners", new ArrayList<>(winners));
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates and returns the dialog that displays the lottery success message and winner list.
     *
     * @param savedInstanceState The previously saved state, if available
     * @return The constructed {@link Dialog} for displaying success details
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_lottery_success, null);

        // Retrieve winners from arguments
        if (getArguments() != null) {
            winners = (List<Entrant>) getArguments().getSerializable("winners");
        }

        // Initialize views
        TextView successMessage = view.findViewById(R.id.success_message);
        TextView winnersList = view.findViewById(R.id.winners_list);
        Button viewSelectedButton = view.findViewById(R.id.view_selected_button);

        // Display total number of winners
        successMessage.setText(winners.size() + " entrants selected and notified!");

        // Build a short preview list of winners (limit to 3 names)
        StringBuilder winnersText = new StringBuilder();
        int displayCount = Math.min(3, winners.size());
        for (int i = 0; i < displayCount; i++) {
            winnersText.append("• ").append(winners.get(i).getName()).append("\n");
        }

        // Indicate if there are more winners not shown
        if (winners.size() > 3) {
            winnersText.append("... ").append(winners.size() - 3).append(" more");
        }

        winnersList.setText(winnersText.toString());

        // Switch to "Selected" tab when the user clicks the button
        viewSelectedButton.setOnClickListener(v -> {
            dismiss();
            if (getParentFragment() instanceof EventManagementFragment) {
                ((EventManagementFragment) getParentFragment()).switchToSelectedTab();
            }
        });

        builder.setView(view);
        setCancelable(false); // Force user interaction to close dialog
        return builder.create();
    }
}
