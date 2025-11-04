package com.example.lotterysystemproject.views.fragments.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.viewmodels.EventViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateEventFragment extends Fragment {

    // UI Components
    private EditText eventNameInput, descriptionInput, locationInput, capacityInput, priceInput;
    private Button dateButton, timeButton, regStartButton, regEndButton;
    private Button uploadPosterButton, createEventButton, backButton;

    // Data
    private EventViewModel eventViewModel;
    private Calendar eventDateTime, regStartDate, regEndDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        // Initialize ViewModel
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Initialize calendars
        eventDateTime = Calendar.getInstance();
        regStartDate = Calendar.getInstance();
        regEndDate = Calendar.getInstance();

        // Initialize views
        initializeViews(view);

        // Setup listeners
        setupListeners();

        // Setup validation
        setupValidation();

        return view;
    }

    private void initializeViews(View view) {
        // Text inputs
        eventNameInput = view.findViewById(R.id.event_name_input);
        descriptionInput = view.findViewById(R.id.description_input);
        locationInput = view.findViewById(R.id.location_input);
        capacityInput = view.findViewById(R.id.capacity_input);
        priceInput = view.findViewById(R.id.price_input);

        // Date/Time buttons
        dateButton = view.findViewById(R.id.date_button);
        timeButton = view.findViewById(R.id.time_button);
        regStartButton = view.findViewById(R.id.reg_start_button);
        regEndButton = view.findViewById(R.id.reg_end_button);

        // Action buttons
        uploadPosterButton = view.findViewById(R.id.upload_poster_button);
        createEventButton = view.findViewById(R.id.create_event_button);
        backButton = view.findViewById(R.id.back_button);

        // Set initial button texts
        updateDateTimeButtons();
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Event Date picker
        dateButton.setOnClickListener(v -> showDatePicker(eventDateTime, (year, month, day) -> {
            eventDateTime.set(year, month, day);
            updateDateTimeButtons();
            validateForm();
        }));

        // Event Time picker
        timeButton.setOnClickListener(v -> showTimePicker(eventDateTime, (hour, minute) -> {
            eventDateTime.set(Calendar.HOUR_OF_DAY, hour);
            eventDateTime.set(Calendar.MINUTE, minute);
            updateDateTimeButtons();
            validateForm();
        }));

        // Registration Start Date picker
        regStartButton.setOnClickListener(v -> showDatePicker(regStartDate, (year, month, day) -> {
            regStartDate.set(year, month, day);
            updateDateTimeButtons();
            validateForm();
        }));

        // Registration End Date picker
        regEndButton.setOnClickListener(v -> showDatePicker(regEndDate, (year, month, day) -> {
            regEndDate.set(year, month, day);
            updateDateTimeButtons();
            validateForm();
        }));

        // Upload Poster button (US 02.04.01 - will implement later)
        uploadPosterButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Upload Poster - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Create Event button (US 02.01.01)
        createEventButton.setOnClickListener(v -> createEvent());
    }

    private void setupValidation() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        eventNameInput.addTextChangedListener(validationWatcher);
        capacityInput.addTextChangedListener(validationWatcher);
    }

    private void validateForm() {
        boolean isValid = true;

        // Check required fields
        if (eventNameInput.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

        if (capacityInput.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

        // Enable/disable create button
        createEventButton.setEnabled(isValid);
        createEventButton.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void showDatePicker(Calendar calendar, DateSetListener listener) {
        DatePickerDialog picker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> listener.onDateSet(year, month, dayOfMonth),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void showTimePicker(Calendar calendar, TimeSetListener listener) {
        TimePickerDialog picker = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> listener.onTimeSet(hourOfDay, minute),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        picker.show();
    }

    private void updateDateTimeButtons() {
        dateButton.setText("📅 " + dateFormat.format(eventDateTime.getTime()));
        timeButton.setText("🕐 " + timeFormat.format(eventDateTime.getTime()));
        regStartButton.setText("📅 " + dateFormat.format(regStartDate.getTime()));
        regEndButton.setText("📅 " + dateFormat.format(regEndDate.getTime()));
    }

    private void createEvent() {
        // US 02.01.01: Create event and generate QR code

        // Collect form data
        String name = eventNameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String capacityStr = capacityInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();

        // Validate capacity
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid capacity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Event object
        Event newEvent = new Event(
                name,
                eventDateTime.getTime(),
                timeFormat.format(eventDateTime.getTime()),
                location,
                capacity
        );
        newEvent.setDescription(description);
        newEvent.setRegistrationStart(regStartDate.getTime());
        newEvent.setRegistrationEnd(regEndDate.getTime());

        // Parse price if provided
        if (!priceStr.isEmpty()) {
            try {
                double price = Double.parseDouble(priceStr);
                newEvent.setPrice(price);
            } catch (NumberFormatException e) {
                // Ignore invalid price
            }
        }

        // Generate QR codes (US 02.01.01)
        String promoQR = generateQRCode(name, "PROMO");
        String checkinQR = generateQRCode(name, "CHECKIN");
        newEvent.setQrCodePromo(promoQR);
        newEvent.setQrCodeCheckin(checkinQR);

        // Save to repository via ViewModel
        eventViewModel.createEvent(newEvent);

        // Show success message
        Toast.makeText(getContext(),
                "✓ Event created with QR codes!",
                Toast.LENGTH_LONG).show();

        // Navigate back to dashboard
        requireActivity().onBackPressed();
    }

    private String generateQRCode(String eventName, String type) {
        // Simple QR code data format (real implementation would generate actual QR image)
        return type + "_" + eventName.replaceAll(" ", "_") + "_" + System.currentTimeMillis();
    }

    // Listener interfaces
    private interface DateSetListener {
        void onDateSet(int year, int month, int day);
    }

    private interface TimeSetListener {
        void onTimeSet(int hour, int minute);
    }
}