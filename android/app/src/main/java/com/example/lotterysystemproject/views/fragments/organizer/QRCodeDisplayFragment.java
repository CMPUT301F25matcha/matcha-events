package com.example.lotterysystemproject.Views.fragments.organizer;

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
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Models.Event;
import com.example.lotterysystemproject.utils.QRCodeGenerator;
import com.example.lotterysystemproject.viewmodels.EventViewModel;

public class QRCodeDisplayFragment extends Fragment {

    private EventViewModel eventViewModel;
    private String eventId;
    private Event currentEvent;

    private TextView eventNameTitle;
    private ImageView promoQrImage, checkinQrImage;
    private Button backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_code_display, container, false);

        // Get event ID from arguments
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

        // Load event and generate QR codes
        loadEventAndGenerateQRCodes();

        // Back button
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void loadEventAndGenerateQRCodes() {
        eventViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                for (Event event : events) {
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

    private void generateAndDisplayQRCodes(Event event) {
        // Generate promotional QR code (US 02.01.01)
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