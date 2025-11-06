package com.example.lotterysystemproject.Views.fragments.organizer;

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
import com.example.lotterysystemproject.Models.Entrant;
import com.example.lotterysystemproject.repositories.EntrantRepository;
import com.example.lotterysystemproject.viewmodels.EntrantViewModel;
import java.util.List;

public class DrawLotteryDialogFragment extends DialogFragment {

    private EntrantViewModel entrantViewModel;
    private TextView waitingCountText, availableSpotsText;
    private EditText numberInput;
    private Button cancelButton, drawButton;

    private int waitingListSize;
    private int availableSpots;

    public static DrawLotteryDialogFragment newInstance(int waitingListSize, int availableSpots) {
        DrawLotteryDialogFragment fragment = new DrawLotteryDialogFragment();
        Bundle args = new Bundle();
        args.putInt("waitingListSize", waitingListSize);
        args.putInt("availableSpots", availableSpots);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_draw_lottery, null);

        // Get arguments
        if (getArguments() != null) {
            waitingListSize = getArguments().getInt("waitingListSize");
            availableSpots = getArguments().getInt("availableSpots");
        }

        // Get ViewModel
        entrantViewModel = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        // Initialize views
        waitingCountText = view.findViewById(R.id.waiting_list_count);
        availableSpotsText = view.findViewById(R.id.available_spots);
        numberInput = view.findViewById(R.id.number_input);
        cancelButton = view.findViewById(R.id.cancel_button);
        drawButton = view.findViewById(R.id.draw_button);

        // Set values
        waitingCountText.setText("Waiting List: " + waitingListSize);
        availableSpotsText.setText("Available Spots: " + availableSpots);
        numberInput.setHint(String.valueOf(Math.min(availableSpots, waitingListSize)));

        // Cancel button
        cancelButton.setOnClickListener(v -> dismiss());

        // Draw button (US 02.05.02)
        drawButton.setOnClickListener(v -> drawLottery());

        builder.setView(view);
        return builder.create();
    }

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

    private void showSuccessDialog(List<Entrant> winners) {
        com.example.lotterysystemproject.Views.fragments.organizer.LotterySuccessDialogFragment successDialog =
                com.example.lotterysystemproject.Views.fragments.organizer.LotterySuccessDialogFragment.newInstance(winners);
        successDialog.show(getParentFragmentManager(), "lottery_success");
    }
}