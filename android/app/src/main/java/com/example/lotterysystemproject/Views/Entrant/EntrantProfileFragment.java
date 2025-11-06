package com.example.lotterysystemproject.Views.Entrant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterysystemproject.R;

public class EntrantProfileFragment extends Fragment {

    public EntrantProfileFragment() {}

    // ActivityResultLauncher to handle result from DeleteProfileActivity
    private final ActivityResultLauncher<Intent> deleteProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && getView() != null) {
                    // Clear the UI or show a toast
                    TextView name  = getView().findViewById(R.id.profile_name);
                    TextView email = getView().findViewById(R.id.profile_email);
                    if (name != null)  name.setText("—");
                    if (email != null) email.setText("—");
                    Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        Button settings = v.findViewById(R.id.btn_settings);
        settings.setOnClickListener(btn ->
                androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_profile_to_settings)
        );

        // ✅ NEW: Edit Profile button
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
            startActivity(i);
        });

        bindProfileToViews(v);   // <- load saved data to UI
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v != null) bindProfileToViews(v);  // refresh if you returned from edits
    }

    private void bindProfileToViews(@NonNull View root) {
        android.content.SharedPreferences prefs =
                requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);

        String name  = prefs.getString("userName",  "—");
        String email = prefs.getString("userEmail", "—");
        String phone = prefs.getString("userPhone", "—"); // show if you have a phone TextView

        android.widget.TextView tvName  = root.findViewById(R.id.profile_name);
        android.widget.TextView tvEmail = root.findViewById(R.id.profile_email);
        android.widget.TextView tvPhone = root.findViewById(R.id.profile_phone);

        if (tvName  != null)  tvName.setText(name == null || name.isEmpty() ? "—" : name);
        if (tvEmail != null) tvEmail.setText(email == null || email.isEmpty() ? "—" : email);
        if (tvPhone != null) tvPhone.setText(phone == null || phone.isEmpty() ? "—" : phone);
    }

    private String resolveUserId() {
        android.content.SharedPreferences prefs =
                requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
        return prefs.getString("userId", "unknown");
    }
}
