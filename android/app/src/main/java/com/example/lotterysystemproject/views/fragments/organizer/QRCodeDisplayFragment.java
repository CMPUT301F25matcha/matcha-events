package com.example.lotterysystemproject.views.fragments.organizer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterysystemproject.models.EventAdmin;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.utils.QRCodeGenerator;
import com.example.lotterysystemproject.viewmodels.EventViewModel;

/**
 * Fragment responsible for displaying promotional and check-in QR codes
 * associated with a specific event.
 * <p>
 * The fragment retrieves the selected event's information via {@link EventViewModel}
 * and uses {@link QRCodeGenerator} to generate and display two QR codes:
 * one for event promotion and another for participant check-in.
 * </p>
 */
public class QRCodeDisplayFragment extends Fragment {

    /** Shared ViewModel for accessing event data. */
    private EventViewModel eventViewModel;

    /** ID of the event whose QR codes are being displayed. */
    private String eventId;

    /** The currently selected event. */
    private EventAdmin currentEvent;

    /** Displays the event’s name as a title. */
    private TextView eventNameTitle;

    /** ImageView for displaying the promotional QR code. */
    private ImageView promoQrImage;

    /** ImageView for displaying the check-in QR code. */
    private ImageView checkinQrImage;

    /** Button for navigating back to the previous screen. */
    private Button backButton;

    /**
     * Inflates the QR code display layout and initializes UI components and ViewModel.
     * Retrieves the event ID passed as an argument and triggers QR code generation.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout
     * @param container Parent view that the fragment will attach to
     * @param savedInstanceState Saved instance state, if available
     * @return The root view of the inflated layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_code_display, container, false);

        // Retrieve event ID from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        // Initialize ViewModel
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Initialize views
        backButton = view.findViewById(R.id.back_button);
        eventNameTitle = view.findViewById(R.id.event_name_title);
        promoQrImage = view.findViewById(R.id.promo_qr_image);
        checkinQrImage = view.findViewById(R.id.checkin_qr_image);

        // Load event and generate its QR codes
        loadEventAndGenerateQRCodes();

        // Handle back navigation
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    /**
     * Loads the list of events from the ViewModel and finds the event
     * matching the provided event ID. Once found, it updates the title
     * and triggers QR code generation.
     */
    private void loadEventAndGenerateQRCodes() {
        eventViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                for (EventAdmin event : events) {
                    if (event.getId().equals(eventId)) {
                        currentEvent = event;
                        eventNameTitle.setText(event.getName());
                        generateAndDisplayQRCodes(event);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Generates and displays both promotional and check-in QR codes for the given event.
     * <p>
     * The promotional QR code is used for sharing or marketing purposes,
     * while the check-in QR code is used by participants for event entry verification.
     * </p>
     *
     * @param event The {@link EventAdmin} object for which to generate QR codes
     */
    private void generateAndDisplayQRCodes(EventAdmin event) {
        // Generate promotional QR code
        String promoData = QRCodeGenerator.generatePromoData(event.getId(), event.getName());
        Bitmap promoQrBitmap = QRCodeGenerator.generateQRCode(promoData, 500, 500);
        if (promoQrBitmap != null) {
            promoQrImage.setImageBitmap(promoQrBitmap);
        }

        // Generate check-in QR code
        String checkinData = QRCodeGenerator.generateCheckinData(event.getId(), event.getName());
        Bitmap checkinQrBitmap = QRCodeGenerator.generateQRCode(checkinData, 500, 500);
        if (checkinQrBitmap != null) {
            checkinQrImage.setImageBitmap(checkinQrBitmap);
        }
    }
}
