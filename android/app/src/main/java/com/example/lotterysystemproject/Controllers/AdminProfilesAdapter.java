package com.example.lotterysystemproject.Controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterysystemproject.R;
import com.example.lotterysystemproject.Models.User;


/**
 * AdminProfilesAdapter is adapter responsible for displaying
 * a list of  User profiles in the admin dashboard interface.
 *
 * Each profile card displays the user's name, email, and profile image,
 * and includes a button for viewing detailed user information.
 *
 * - Displays user profiles in a scrollable list.
 * - Provides an "View Details" button for user.
 *
 */
public class AdminProfilesAdapter extends RecyclerView.Adapter<AdminProfilesAdapter.ProfileViewHolder> {

    /** The context used for inflating views and managing fragment. */
    private final Context context;

    /** The list of User profiles displayed in the RecyclerView. */
    private List<User> users;

    /**
     * Constructs a new AdminProfilesAdapter.
     *
     * @param context The context used for inflating layouts and creating dialogs.
     * @param users The list of User objects to display.
     */
    public AdminProfilesAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }


    /**
     * ProfileViewHolder represents a single profile card within the RecyclerView.
     *
     * It holds references to the profile's image, name, email, and view details button.
     *
     */
    public class ProfileViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView name, email;
        Button viewDetails;


        /**
         * Constructs a new ProfileViewHolder and binds its layout views.
         *
         * @param itemView The inflated layout representing a single user profile card.
         */
        public ProfileViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            //role = itemView.findViewById(R.id.dialog_role);
            viewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }


    /**
     * Inflates the layout for an individual profile item and creates a new ProfileViewHolder.
     *
     * @param viewGroup The parent ViewGroup into which the view will be added.
     * @param viewType  The type of view (not used here, as there is only one type).
     * @return A new ProfileViewHolder for the inflated view.
     */
    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_profile, viewGroup, false);

        return new ProfileViewHolder(view);

    }

    /**
     * Binds the User data to the views within a ProfileViewHolder.
     *
     * Displays the user's name and email, and attaches a click listener to the
     * "View Details" button, which opens an AdminUserProfileDialog
     * showing detailed user information.
     *
     *
     * @param viewHolder The holder containing the views to bind data to.
     * @param position The position of the user in the dataset.
     */
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder viewHolder, final int position) {
        User user = users.get(position);

        // Bind user data to text fields
        viewHolder.name.setText(user.getName());
        viewHolder.email.setText(user.getEmail());
        //viewHolder.role.setText(user.getRole());

        // Handle "View Details" button click
        viewHolder.viewDetails.setOnClickListener(v -> {
            // Show UserProfileDialog
            AdminUserProfileDialog dialog = new AdminUserProfileDialog(user);
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "UserProfileDialog");
        });

    }

    /**
     * Returns the total number of User profiles in the adapter's dataset.
     *
     * @return The number of profiles to display in the RecyclerView.
     */
    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

}