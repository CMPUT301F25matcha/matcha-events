package com.example.lotterysystemproject.views.fragments.organizer;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.lotterysystemproject.models.Event;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.adapters.TabsPagerAdapter;
import com.example.lotterysystemproject.viewmodels.EntrantViewModel;
import com.example.lotterysystemproject.viewmodels.EventViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Fragment that manages an event's details, including its waiting list and selected entrants.
 * <p>
 * Provides a tabbed interface for organizers to view event participants,
 * navigate between waiting and selected entrants, and display a QR code for event registration.
 */
public class EventManagementFragment extends Fragment {

    private EventViewModel eventViewModel;
    private EntrantViewModel entrantViewModel;

    private TextView eventNameHeader, eventDate, eventLocation, eventEnrollment;
    private Button backButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button qrCodeButton;

    private String eventId;

    private ImageView eventPoster;
    private Button editPosterButton;
    private AlertDialog loadingDialog;
    private String currentPosterUrl;


    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    updateEventPoster(uri);
                }
            });
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US);

    /**
     * Inflates the layout for the fragment, initializes view models and UI elements,
     * and loads event details and entrant data.
     *
     * @param inflater  LayoutInflater for inflating the view
     * @param container Parent view group
     * @param savedInstanceState Previous saved state, if available
     * @return The root view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_management, container, false);

        // Get event ID from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        // Initialize ViewModels
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        entrantViewModel = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        // Load entrants for this event
        entrantViewModel.loadEntrants(eventId);

        // Initialize UI components
        backButton = view.findViewById(R.id.back_button);
        eventNameHeader = view.findViewById(R.id.event_name_header);
        eventDate = view.findViewById(R.id.event_date);
        eventLocation = view.findViewById(R.id.event_location);
        eventEnrollment = view.findViewById(R.id.event_enrollment);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        qrCodeButton = view.findViewById(R.id.qr_code_button);
        eventPoster = view.findViewById(R.id.event_poster);
        editPosterButton = view.findViewById(R.id.edit_poster_button);

        // Configure tab layout and view pager
        setupTabs();

        // Load and display event details
        loadEventDetails();

        // Navigate back
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Navigate to QR code display
        qrCodeButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            Navigation.findNavController(v).navigate(
                    R.id.action_eventManagement_to_qrCodeDisplay,
                    args
            );
        });

        // Edit poster
        editPosterButton.setOnClickListener(v -> showPosterUpdateDialog());

        return view;
    }

    /**
     * Configures the tab layout and attaches it to the ViewPager.
     * Displays two tabs: "Waiting" and "Selected".
     */
    private void setupTabs() {
        TabsPagerAdapter adapter = new TabsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Waiting");
                    break;
                case 1:
                    tab.setText("Selected");
                    break;
            }
        }).attach();
    }

    /**
     * Observes the event list and loads the details for the current event.
     */
    private void loadEventDetails() {
        eventViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                for (Event event : events) {
                    if (event.getId().equals(eventId)) {
                        displayEventDetails(event);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Displays the details of the specified event, including name, date, location, and enrollment count.
     *
     * @param event The event whose details will be shown
     */
    private void displayEventDetails(Event event) {

        currentPosterUrl = event.getPosterImageUrl();

        // Load poster
        if (event.getPosterImageUrl() != null && !event.getPosterImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(event.getPosterImageUrl())
                    .into(eventPoster);
        }

        eventNameHeader.setText(event.getName());
        eventDate.setText("📅 " + dateFormat.format(event.getEventDate()));
        eventLocation.setText("📍 " + event.getLocation());
        eventEnrollment.setText("👥 " + event.getCurrentEnrolled() + "/" + event.getMaxCapacity() + " enrolled");
    }

    /**
     * Switches the ViewPager to the "Selected" tab.
     * Used when an entrant is drawn or selected from the waiting list.
     */
    public void switchToSelectedTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1); // 1 = Selected tab
        }
    }

    /**
     * Shows dialog to confirm poster update
     */
    private void showPosterUpdateDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Update Event Poster")
                .setMessage("Choose a new poster image for this event")
                .setPositiveButton("Choose Image", (dialog, which) ->
                        pickImageLauncher.launch("image/*"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Uploads new poster image and updates event
     */
    private void updateEventPoster(Uri imageUri) {

        // Show loading dialog
        showLoadingDialog();
        // Upload to Firebase storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference posterRef = storageReference.child("event_posters/" + eventId + "_" + System.currentTimeMillis() + ".jpg");


        posterRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    posterRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        updatePosterUrlInFirestore(downloadUrl.toString());
                    }).addOnFailureListener(e -> {
                        dismissLoadingDialog();
                        Toast.makeText(requireContext(), "Failed to download url: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    });
                })
                .addOnFailureListener(e -> {
                    dismissLoadingDialog();
                    Toast.makeText(requireContext(), "Failed to update poster: " + e.getMessage(), Toast.LENGTH_LONG).show();

                });

    }

    private void updatePosterUrlInFirestore(String newPosterUrl)  {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .update("posterImageUrl", newPosterUrl)
                .addOnSuccessListener(aVoid -> {
                    dismissLoadingDialog();

                    // Update UI with new image
                    Glide.with(this)
                            .load(newPosterUrl)
                            .into(eventPoster);

                    // Delete old poster from storage
                    deleteOldPoster();

                    // Update current poster
                    currentPosterUrl = newPosterUrl;

                    Toast.makeText(requireContext(), "Poster updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    dismissLoadingDialog();
                    Toast.makeText(requireContext(), "Failed to update poster in database: " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
    }

    /**
     * Shows loading dialog
     */
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);
            builder.setView(dialogView);
            builder.setCancelable(false);
            loadingDialog = builder.create();

        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }


    /**
     * Dismisses the loading dialog
     */
    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * Deletes the old poster from Firebase Storage
     */
    private void deleteOldPoster() {
        if (currentPosterUrl != null && !currentPosterUrl.isEmpty()) {
            try {
                StorageReference oldPosterRef  = FirebaseStorage.getInstance().getReferenceFromUrl(currentPosterUrl);
                oldPosterRef.delete()
                        .addOnSuccessListener(aVoid ->
                                Log.d("EventManagement", "Old poster deleted successfully"))
                        .addOnFailureListener(e ->
                                Log.e("EventManagement", "Failed to delete old poster", e));
            } catch (Exception e) {
                Log.e("EventManagement", "Error parsing old poster URL", e);
            }

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissLoadingDialog();
    }

}
