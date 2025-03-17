package com.example.boxy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/*
 * Fragment for displaying the current user's profile information.
 * Retrieves user details from Firebase Authentication and Firestore,
 * and loads the profile image using Glide.
 */
public class Profile extends Fragment {

    private ImageView ivProfile;
    private TextView tvName, tvEmail;
    private Button btnEditProfile, btnLogout;

    public Profile() {
        // Required empty public constructor.
    }

    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Bind UI elements from the layout.
        ivProfile = view.findViewById(R.id.iv_profile);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Retrieve the currently signed-in user.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Set email from FirebaseAuth.
            String authEmail = currentUser.getEmail();
            tvEmail.setText(authEmail);

            // Retrieve additional user details from Firestore.
            String userId = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Display full name if available.
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                tvName.setText(fullName);
                            } else {
                                tvName.setText("User");
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to load user details: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

            // Load profile image if a photo URL exists.
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(requireContext())
                        .load(currentUser.getPhotoUrl())
                        .into(ivProfile);
            }
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Set up click listener for the Edit Profile button.
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit profile clicked", Toast.LENGTH_SHORT).show();
            // Navigation to an EditProfile screen can be added here.
        });

        // Set up the Logout button to sign out the user and navigate to the login screen.
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
