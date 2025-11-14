package com.example.lotterysystemproject.views.fragments.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.example.lotterysystemproject.firebasemanager.EntrantRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.models.DeviceIdentityManager;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.viewmodels.EventViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * Fragment that allows organizers to create new events.
 * <p>
 * Provides input fields for event details such as name, description,
 * location, capacity, and price. Also handles date/time pickers,
 * registration period selection, and event validation before submission.
 * </p>
 */
public class CreateEventFragment extends Fragment {

    /** UI components for event details */
    private EditText eventNameInput, descriptionInput, locationInput, capacityInput, priceInput, maxWaitingListInput;
    private Button dateButton, timeButton, regStartButton, regEndButton;
    private Button uploadPosterButton, createEventButton, backButton;

    /** ViewModel used to manage event data */
    private EventViewModel eventViewModel;

    /** Calendar objects for date/time management */
    private Calendar eventDateTime, regStartDate, regEndDate;

    /** Date/time formats for displaying user-selected values */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    /**
     * Inflates the layout and initializes the fragment components.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        eventDateTime = Calendar.getInstance();
        regStartDate = Calendar.getInstance();
        regEndDate = Calendar.getInstance();

        initializeViews(view);
        setupListeners();
        setupValidation();

        return view;
    }

    /**
     * Initializes all UI views from the layout.
     *
     * @param view The inflated fragment view.
     */
    private void initializeViews(View view) {
        eventNameInput = view.findViewById(R.id.event_name_input);
        descriptionInput = view.findViewById(R.id.description_input);
        locationInput = view.findViewById(R.id.location_input);
        capacityInput = view.findViewById(R.id.capacity_input);
        maxWaitingListInput = view.findViewById(R.id.max_waiting_list_input);
        priceInput = view.findViewById(R.id.price_input);

        dateButton = view.findViewById(R.id.date_button);
        timeButton = view.findViewById(R.id.time_button);
        regStartButton = view.findViewById(R.id.reg_start_button);
        regEndButton = view.findViewById(R.id.reg_end_button);

        uploadPosterButton = view.findViewById(R.id.upload_poster_button);
        createEventButton = view.findViewById(R.id.create_event_button);
        backButton = view.findViewById(R.id.back_button);

        updateDateTimeButtons();
    }

    /**
     * Sets up listeners for all interactive elements, such as buttons and pickers.
     */
    private void setupListeners() {
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        dateButton.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        eventDateTime.set(year, month, dayOfMonth);
                        updateDateTimeButtons();
                        validateForm();
                    },
                    eventDateTime.get(Calendar.YEAR),
                    eventDateTime.get(Calendar.MONTH),
                    eventDateTime.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });

        timeButton.setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minute) -> {
                        eventDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        eventDateTime.set(Calendar.MINUTE, minute);
                        updateDateTimeButtons();
                        validateForm();
                    },
                    eventDateTime.get(Calendar.HOUR_OF_DAY),
                    eventDateTime.get(Calendar.MINUTE),
                    false
            );
            picker.show();
        });

        regStartButton.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        regStartDate.set(year, month, dayOfMonth);
                        updateDateTimeButtons();
                        validateForm();
                    },
                    regStartDate.get(Calendar.YEAR),
                    regStartDate.get(Calendar.MONTH),
                    regStartDate.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });

        regEndButton.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        regEndDate.set(year, month, dayOfMonth);
                        updateDateTimeButtons();
                        validateForm();
                    },
                    regEndDate.get(Calendar.YEAR),
                    regEndDate.get(Calendar.MONTH),
                    regEndDate.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });

        uploadPosterButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Upload Poster - Coming soon!", Toast.LENGTH_SHORT).show());

        createEventButton.setOnClickListener(v -> createEvent());
    }

    /**
     * Sets up basic input validation to ensure required fields are filled.
     */
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

    /**
     * Validates form inputs and toggles the create event button accordingly.
     */
    private void validateForm() {
        boolean isValid = !eventNameInput.getText().toString().trim().isEmpty() &&
                !capacityInput.getText().toString().trim().isEmpty();

        createEventButton.setEnabled(isValid);
        createEventButton.setAlpha(isValid ? 1.0f : 0.5f);
    }

    /**
     * Updates date and time button labels with the currently selected values.
     */
    private void updateDateTimeButtons() {
        dateButton.setText("📅 " + dateFormat.format(eventDateTime.getTime()));
        timeButton.setText("🕐 " + timeFormat.format(eventDateTime.getTime()));
        regStartButton.setText("📅 " + dateFormat.format(regStartDate.getTime()));
        regEndButton.setText("📅 " + dateFormat.format(regEndDate.getTime()));
    }

    /**
     * Creates a new event using input data and saves it via the ViewModel.
     */
    private void createEvent() {
        String name = eventNameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String capacityStr = capacityInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String maxWaitingListStr = maxWaitingListInput.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || description.isEmpty() || location.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid capacity", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = 0.0;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException ignored) {}

        int maxWaitingList = 100;
        try {
            maxWaitingList = Integer.parseInt(maxWaitingListStr);
        } catch (NumberFormatException ignored) {}

        String deviceId = DeviceIdentityManager.getUserId(getContext());

        // Get the repository and fetch user info
        EntrantRepository repository = RepositoryProvider.getEntrantRepository();
        int finalMaxWaitingList = maxWaitingList;
        double finalPrice = price;
        int finalCapacity = capacity;

        repository.getCurrentUserInfo(deviceId, new EntrantRepository.OnUserInfoListener() {
            @Override
            public void onSuccess(String hostId, String hostName, String role) {
                // Check if user is an organizer
                if (!"organizer".equals(role)) {
                    Toast.makeText(getContext(), "Only organizers can create events", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Generate event ID locally (works in both mock and Firebase modes)
                String eventId = "event_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);

                Event newEvent = new Event(
                        eventId,
                        name,
                        description,
                        hostName,
                        hostId,
                        eventDateTime.getTime(),
                        timeFormat.format(eventDateTime.getTime()),
                        location,
                        finalCapacity
                );

                newEvent.setRegistrationStart(regStartDate.getTime());
                newEvent.setRegistrationEnd(regEndDate.getTime());
                newEvent.setMaxWaitingListSize(finalMaxWaitingList);

                // Generate QR codes
                String promoQR = generateQRCode(name, "PROMO");
                String checkinQR = generateQRCode(name, "CHECKIN");

                newEvent.setPromotionalQrCode(promoQR);
                newEvent.setCheckInQrCode(checkinQR);

                // Save the event via ViewModel
                eventViewModel.createEvent(newEvent);

                Toast.makeText(getContext(),
                        "✓ Event created with QR codes!",
                        Toast.LENGTH_LONG).show();

                requireActivity().onBackPressed();
            }

            @Override
            public void onFailure(String error) {
                Log.e("EventCreation", "Failed to fetch organizer info: " + error);
                Toast.makeText(getContext(),
                        "Error: Could not verify organizer. " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Generates a mock QR code string for testing purposes.
     *
     * @param eventName The name of the event.
     * @param type The QR code type (e.g., "PROMO", "CHECKIN").
     * @return The generated QR code string.
     */
    private String generateQRCode(String eventName, String type) {
        return type + "_" + eventName.replaceAll(" ", "_") + "_" + System.currentTimeMillis();
    }
}
