package com.example.lotterysystemproject.views.fragments.organizer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterysystemproject.adapters.PlaceAutoSuggestAdapter;
import com.example.lotterysystemproject.firebasemanager.EntrantRepository;
import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryCallback;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.firebasemanager.UserRepository;
import com.example.lotterysystemproject.models.DeviceIdentityManager;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.models.User;
import com.example.lotterysystemproject.viewmodels.EventViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    private EditText eventNameInput, descriptionInput, capacityInput, priceInput, maxWaitingListInput;
    private Button dateButton, timeButton, regStartButton, regEndButton;
    private Button uploadPosterButton, createEventButton, backButton;

    /** ViewModel used to manage event data */
    private EventViewModel eventViewModel;

    /** Calendar objects for date/time management */
    private Calendar eventDateTime, regStartDate, regEndDate;

    /** Date/time formats for displaying user-selected values */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    private Uri selectedImageUri = null;
    private ImageView eventPosterPreview;
    private ActivityResultLauncher<String> pickImageLauncher;

    //google maps
    private double selectedLatitude;
    private double selectedLongitude;
    private String selectedAddress;
    private AutoCompleteTextView locationInput;
    private PlaceAutoSuggestAdapter adapter;
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    /**
     * Inflates the layout and initializes the fragment components.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        // Initialize Google Places SDK (Use your actual API Key)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "YOUR_GOOGLE_CLOUD_API_KEY_HERE");
        }

        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        eventDateTime = Calendar.getInstance();
        regStartDate = Calendar.getInstance();
        regEndDate = Calendar.getInstance();

        initializeViews(view);
        pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        eventPosterPreview.setImageURI(uri);
                        validateForm();
                    }
                });
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

        // 1. Initialize Places Client
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyAh33R6SyKaCWWfNaMwv6crlBPdJyxINxE");
        }
        placesClient = Places.createClient(requireContext());

        sessionToken = AutocompleteSessionToken.newInstance();

        // 2. Set up the Adapter
        adapter = new PlaceAutoSuggestAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, placesClient);
        locationInput.setAdapter(adapter);

        // 3. Add TextWatcher to trigger API calls as user types
        locationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) { // Only search after 2 chars to save API quota
                    fetchSuggestions(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 4. Handle the user clicking a suggestion
        locationInput.setOnItemClickListener((parent, v, position, id) -> {
            // The user clicked a specific address in the dropdown
            String placeId = adapter.getPlaceId(position);

            // Now we must do a specific call to get the Lat/Lng (Coordinates)
            // because the prediction list only gives us the Name and ID.
            fetchPlaceDetails(placeId);
        });

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

        eventPosterPreview = view.findViewById(R.id.event_poster_preview);

        updateDateTimeButtons();
    }

    private void fetchSuggestions(String query) {
        // 3. Pass the session token here
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(sessionToken)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            adapter.setData(response.getAutocompletePredictions());
        }).addOnFailureListener((exception) -> {
            exception.printStackTrace();
        });
    }

    private void fetchPlaceDetails(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // 4. Pass the SAME session token here to "close" the session
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(sessionToken)
                .build();

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            if (place.getLatLng() != null) {
                // Save your data
                double lat = place.getLatLng().latitude;
                double lng = place.getLatLng().longitude;
                locationInput.setText(place.getAddress());
                locationInput.dismissDropDown(); // Close the list
            }

            // 5. CRITICAL: Create a new token for the next search.
            // A token is invalid after it has been used in a FetchPlaceRequest.
            sessionToken = AutocompleteSessionToken.newInstance();

        }).addOnFailureListener((exception) -> {
            exception.printStackTrace();
            // Even on failure, it is safer to reset the token to avoid invalid token errors
            sessionToken = AutocompleteSessionToken.newInstance();
        });
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
                pickImageLauncher.launch("image/*")
        );

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

        int maxWaitingList = 100;
        try {
            maxWaitingList = Integer.parseInt(maxWaitingListStr);
        } catch (NumberFormatException ignored) {}

        String deviceId = DeviceIdentityManager.getUserId(getContext());

        // Get the repository and fetch user info
        UserRepository repository = RepositoryProvider.getUserRepository();
        int finalMaxWaitingList = maxWaitingList;
        int finalCapacity = capacity;

        repository.getUserById(deviceId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                // Check if user is an organizer
                if (!"organizer".equals(result.getRole())) {
                    Toast.makeText(getContext(), "Only organizers can create events", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Generate event ID locally (works in both mock and Firebase modes)
                String eventId = "event_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
                //TODO: Make sure to gather when the event is, as this is for display for Entrants, hard coded for now
                Date date = regEndDate.getTime();
                String eventTime = "5:00PM";
                EventRepository eventRepository = RepositoryProvider.getEventRepository();
                Event newEvent = new Event(eventId, name, description, result.getName(), result.getEmail(), date, eventTime, location, finalCapacity);

                newEvent.setRegistrationStart(regStartDate.getTime());
                newEvent.setRegistrationEnd(regEndDate.getTime());
                newEvent.setMaxWaitingListSize(finalMaxWaitingList);

                // Generate QR codes
                String promoQR = generateQRCode(name, "PROMO");
                String checkinQR = generateQRCode(name, "CHECKIN");

                newEvent.setPromotionalQrCode(promoQR);
                newEvent.setCheckInQrCode(checkinQR);

                // Save the event via ViewModel which saves to firebase
                if (selectedImageUri != null) {
                    uploadPosterAndCreateEvent(newEvent);
                } else {
                    eventViewModel.createEvent(newEvent);
                    Toast.makeText(getContext(),
                            "✓ Event created with QR codes!",
                            Toast.LENGTH_LONG).show();
                    requireActivity().onBackPressed();

                }



            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventCreation", "Failed to fetch organizer info: " + e);
                Toast.makeText(getContext(),
                        "Error: Could not verify organizer. " + e,
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

    private void uploadPosterAndCreateEvent(Event event) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("event_posters/" + event.getId() + ".jpg");

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(t -> {
                    ref.getDownloadUrl().addOnSuccessListener(url -> {
                        event.setPosterImageUrl(url.toString());
                        eventViewModel.createEvent(event);

                        Toast.makeText(getContext(), "Event created with poster!", Toast.LENGTH_LONG).show();
                        requireActivity().onBackPressed();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Poster upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
