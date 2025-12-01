package com.example.lotterysystemproject.views.entrant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterysystemproject.firebasemanager.EventRepository;
import com.example.lotterysystemproject.firebasemanager.RepositoryProvider;
import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.views.organizer.OrganizerMainActivity;

/**
 * Represents entrant’s profile screen displayed within the profile navigation section of the app.
 * Allows users to view their personal information (name, email, phone), access account settings,
 * and delete their profile.
 */
public class EntrantProfileFragment extends Fragment {

    /** Default constructor required for Fragment instantiation. */
    public EntrantProfileFragment() {}

    // ActivityResultLauncher to handle result from DeleteProfileActivity
    private final ActivityResultLauncher<Intent> deleteProfileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent i = new Intent(requireActivity(), UserInfoView.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    }
            );

    /**
     * Inflates the fragment’s layout.
     * @param inflater  LayoutInflater used to inflate the layout XML
     * @param container Parent container that holds this fragment
     * @param savedInstanceState Previous instance state if re-created
     */
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_profile, container, false);
    }

    /**
     * Called after the fragment’s view hierarchy is created.
     * Wires the button listeners for navigation and deletion actions and loads profile data into the screen views.
     * @param v Root view of fragment
     * @param savedInstanceState Previous instance state if re-created
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        Button editBtn = v.findViewById(R.id.btn_edit_profile);
        editBtn.setOnClickListener(click -> {
            Intent i = new Intent(requireContext(), EditProfileActivity.class);
            i.putExtra(EditProfileActivity.EXTRA_USER_ID, resolveUserId());
            startActivity(i);
        });

        Button deleteBtn = v.findViewById(R.id.btn_delete_profile);
        deleteBtn.setOnClickListener(click -> {
            Intent i = new Intent(requireContext(), DeleteProfileActivity.class);
            i.putExtra(DeleteProfileActivity.EXTRA_USER_ID, resolveUserId());
            deleteProfileLauncher.launch(i); // or use startActivityForResult pattern below if callback wanted
        });

        Button organizerSignUpBtn = v.findViewById(R.id.btn_organizer_signup);
        organizerSignUpBtn.setOnClickListener(click -> {
            String userId = resolveUserId();

            RepositoryProvider.getEventRepository().updateUserRoleToOrganizer(
                    userId,
                    new EventRepository.RepositoryCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(requireContext(),
                                    "Successfully updated your role to an organizer. Swipe right to return.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("UserRole", "Successfully updated user role to organizer");
                            Intent i = new Intent(requireContext(), OrganizerMainActivity.class);
                            i.putExtra(DeleteProfileActivity.EXTRA_USER_ID, userId);
                            startActivity(i);
                        }

                        @Override
                        public void onError(Exception error) {
                            Log.e("UserRole", "Failed to update role: " + error.getMessage());
                            Toast.makeText(requireContext(),
                                    "Failed to update role to organizer",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        bindProfileToViews(v);   // load saved data to UI
        View historyBtn = v.findViewById(R.id.btn_events_history);
        if (historyBtn != null) {
            historyBtn.setOnClickListener(click ->
                    androidx.navigation.Navigation.findNavController(v)
                            .navigate(R.id.action_profile_to_eventHistory)
            );
        }
    }

    /**
     * Refreshes profile data whenever fragment becomes visible again.
     * Ensures updates are reflected when returning from settings screen.
     */
    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v != null) bindProfileToViews(v);  // refresh if returned from edits
    }

    /**
     * Binds saved user profile information to corresponding text views.
     * Reads stored values from and updates the profile UI fields.
     * @param root The fragment’s root used to locate text views
     */
    private void bindProfileToViews(@NonNull View root) {
        android.content.SharedPreferences prefs =
                requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);

        String name  = prefs.getString("userName",  "—");
        String email = prefs.getString("userEmail", "—");
        String phone = prefs.getString("userPhone", "—"); // show if optional phone TextView present

        android.widget.TextView tvName  = root.findViewById(R.id.profile_name);
        android.widget.TextView tvEmail = root.findViewById(R.id.profile_email);
        android.widget.TextView tvPhone = root.findViewById(R.id.profile_phone);

        if (tvName  != null)  tvName.setText(name == null || name.isEmpty() ? "—" : name);
        if (tvEmail != null) tvEmail.setText(email == null || email.isEmpty() ? "—" : email);
        if (tvPhone != null) tvPhone.setText(phone == null || phone.isEmpty() ? "—" : phone);
    }

    /**
     * Retrieves the current user’s unique identifier.
     * Uses a mock device ID for local testing purposes.
     */
    private String resolveUserId() {
        android.content.SharedPreferences prefs =
                requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
        return prefs.getString("userId", "unknown");
    }
}

