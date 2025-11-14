package com.example.lotterysystemproject.views.fragments.organizer;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.Entrant;
import com.example.lotterysystemproject.firebasemanager.EntrantRepository;
import com.example.lotterysystemproject.viewmodels.EntrantViewModel;
import java.util.List;

/**
 * A dialog fragment that allows the organizer to perform a lottery draw for an event.
 * <p>
 * Displays the current waiting list size and available spots, and lets the organizer
 * input the number of winners to select. Handles input validation and performs
 * the draw through the {@link EntrantViewModel}.
 * </p>
 */
public class DrawLotteryDialogFragment extends DialogFragment {

    private EntrantViewModel entrantViewModel;
    private TextView waitingCountText, availableSpotsText;
    private EditText numberInput;
    private Button cancelButton, drawButton;

    private int waitingListSize;
    private int availableSpots;

    /**
     * Creates a new instance of the dialog with waiting list and available spot information.
     *
     * @param waitingListSize The number of entrants currently on the waiting list.
     * @param availableSpots  The number of available spots for the event.
     * @return A configured instance of {@link DrawLotteryDialogFragment}.
     */
    public static DrawLotteryDialogFragment newInstance(int waitingListSize, int availableSpots) {
        DrawLotteryDialogFragment fragment = new DrawLotteryDialogFragment();
        Bundle args = new Bundle();
        args.putInt("waitingListSize", waitingListSize);
        args.putInt("availableSpots", availableSpots);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates and returns the dialog for performing a lottery draw.
     *
     * @param savedInstanceState The saved instance state, if available.
     * @return A configured {@link Dialog} instance for the lottery draw.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_draw_lottery, null);

        // Retrieve arguments
        if (getArguments() != null) {
            waitingListSize = getArguments().getInt("waitingListSize");
            availableSpots = getArguments().getInt("availableSpots");
        }

        // Get ViewModel
        entrantViewModel = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        // Initialize UI components
        waitingCountText = view.findViewById(R.id.waiting_list_count);
        availableSpotsText = view.findViewById(R.id.available_spots);
        numberInput = view.findViewById(R.id.number_input);
        cancelButton = view.findViewById(R.id.cancel_button);
        drawButton = view.findViewById(R.id.draw_button);

        // Display current stats
        waitingCountText.setText("Waiting List: " + waitingListSize);
        availableSpotsText.setText("Available Spots: " + availableSpots);
        numberInput.setHint(String.valueOf(Math.min(availableSpots, waitingListSize)));

        // Set up button listeners
        cancelButton.setOnClickListener(v -> dismiss());
        drawButton.setOnClickListener(v -> drawLottery());

        builder.setView(view);
        return builder.create();
    }

    /**
     * Handles validation and triggers the lottery draw through the ViewModel.
     * <p>
     * Validates the user input to ensure that the entered number of winners
     * is within allowed limits, and then performs the draw.
     * </p>
     */
    private void drawLottery() {
        String input = numberInput.getText().toString().trim();

        // Validate input
        if (input.isEmpty()) {
            numberInput.setError("Enter a number");
            return;
        }

        int count;
        try {
            count = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            numberInput.setError("Invalid number");
            return;
        }

        if (count <= 0) {
            numberInput.setError("Must be greater than 0");
            return;
        }

        if (count > waitingListSize) {
            numberInput.setError("Only " + waitingListSize + " in waiting list");
            return;
        }

        if (count > availableSpots) {
            numberInput.setError("Only " + availableSpots + " spots available");
            return;
        }

        // Perform lottery draw
        entrantViewModel.drawLottery(count, new EntrantRepository.OnLotteryCompleteListener() {
            @Override
            public void onComplete(List<Entrant> winners) {
                dismiss();
                showSuccessDialog(winners);
            }

            @Override
            public void onFailure(String error) {
                numberInput.setError(error);
            }
        });
    }

    /**
     * Displays a success dialog with a list of selected winners.
     *
     * @param winners The list of entrants who were selected in the lottery.
     */
    private void showSuccessDialog(List<Entrant> winners) {
        LotterySuccessDialogFragment successDialog =
                LotterySuccessDialogFragment.newInstance(winners);
        successDialog.show(getParentFragmentManager(), "lottery_success");
    }
}
