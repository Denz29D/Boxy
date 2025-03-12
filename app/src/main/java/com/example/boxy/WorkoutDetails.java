package com.example.boxy;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.boxy.models.Workout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.HashMap;
import java.util.Map;

public class WorkoutDetails extends Fragment {

    private static final String TAG = "WorkoutDetails";

    private String workoutId;
    private Workout currentWorkout;

    // UI references
    private TextView tvWorkoutTitle, tvWorkoutDescription, tvDuration, tvDifficulty, tvCalories, tvFullDescription;
    private ImageView ivWorkout;
    private Button btnStartWorkout;
    private ImageButton btnBack;
    private YouTubePlayerView youtubePlayerView;

    public WorkoutDetails() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_workout_details, container, false);

        if (getArguments() != null) {
            workoutId = getArguments().getString("workoutId", "");
        }

        // Initialize UI elements
        tvWorkoutTitle = view.findViewById(R.id.tv_workout_title);
        tvWorkoutDescription = view.findViewById(R.id.tv_workout_description);
        tvDuration = view.findViewById(R.id.tv_duration);
        tvDifficulty = view.findViewById(R.id.tv_difficulty);
        tvCalories = view.findViewById(R.id.tv_calories);
        tvFullDescription = view.findViewById(R.id.tv_workout_full_description);
        ivWorkout = view.findViewById(R.id.iv_workout);
        btnStartWorkout = view.findViewById(R.id.btn_start_workout);
        btnBack = view.findViewById(R.id.btn_back);
        youtubePlayerView = view.findViewById(R.id.youtube_player_view);

        // Ensure the YouTube player lifecycle is observed
        getLifecycle().addObserver(youtubePlayerView);

        // Load workout details from Firestore
        loadWorkoutDetails();

        // Mark workout complete
        btnStartWorkout.setOnClickListener(v -> markWorkoutComplete());

        // Back button navigation
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void loadWorkoutDetails() {
        if (TextUtils.isEmpty(workoutId)) {
            Toast.makeText(requireContext(), "No workout ID provided", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("workouts")
                .document(workoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentWorkout = documentSnapshot.toObject(Workout.class);
                        if (currentWorkout != null) {
                            // Optionally set the ID again to keep it consistent:
                            currentWorkout.setWorkoutId(documentSnapshot.getId());
                            populateUI(currentWorkout);
                            Toast.makeText(requireContext(), "Workout details loaded", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "No workout found with id: " + workoutId);
                        Toast.makeText(requireContext(), "No workout found with this ID", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Failed to load workout.", Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(requireContext(), "Error loading workout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading workout details", e);
                });
    }

    private void populateUI(Workout workout) {
        tvWorkoutTitle.setText(workout.getTitle());
        String shortDesc = workout.getDuration() + " • " + workout.getDifficulty() + " • " + workout.getCalories() + " calories";
        tvWorkoutDescription.setText(shortDesc);

        tvDuration.setText(workout.getDuration());
        tvDifficulty.setText(workout.getDifficulty());
        tvCalories.setText(String.valueOf(workout.getCalories()));
        tvFullDescription.setText(workout.getDescription());

        // Load the image using Glide. If imageUrl is a local drawable name (e.g., "hiit_workout.jpg"),
        // strip the extension and find the resource ID. Then resize with .override() to avoid large images.
        String imageUrl = workout.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (!imageUrl.startsWith("http")) {
                // e.g. "hiit_workout.jpg" -> resourceName = "hiit_workout"
                int dotIndex = imageUrl.lastIndexOf('.');
                String resourceName = (dotIndex > 0) ? imageUrl.substring(0, dotIndex) : imageUrl;
                int resId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());
                if (resId != 0) {
                    Glide.with(this)
                            .load(resId)
                            .override(600, 600)  // Resize the image
                            .centerCrop()        // Crop to fill
                            .into(ivWorkout);
                } else {
                    Log.d(TAG, "Drawable resource not found for: " + resourceName);
                    Toast.makeText(requireContext(), "Image resource not found for " + resourceName, Toast.LENGTH_SHORT).show();
                }
            } else {
                // Image URL is an actual URL, so load it directly (also resizing).
                Glide.with(this)
                        .load(imageUrl)
                        .override(600, 600)
                        .centerCrop()
                        .into(ivWorkout);
            }
        }

        // Setup YouTube video if available
        if (!TextUtils.isEmpty(workout.getVideoId())) {
            youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(workout.getVideoId(), 0f);
                }
            });
        } else {
            youtubePlayerView.setVisibility(View.GONE);
        }
    }

    private void markWorkoutComplete() {
        if (currentWorkout == null) {
            Log.d(TAG, "Current workout is null, cannot mark complete.");
            Toast.makeText(requireContext(), "No workout loaded to mark complete.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Log.d(TAG, "User not signed in.");
            Toast.makeText(requireContext(), "You must be signed in to mark workouts complete.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Ensure the user document exists by merging an empty map.
        db.collection("users").document(userId).set(new HashMap<>(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document ensured.");
                    Toast.makeText(requireContext(), "User doc ensured, now marking workout complete...", Toast.LENGTH_SHORT).show();

                    // Now add the subcollection document.
                    Map<String, Object> data = new HashMap<>();
                    data.put("workoutId", currentWorkout.getWorkoutId());
                    data.put("completedAt", FieldValue.serverTimestamp());

                    db.collection("users")
                            .document(userId)
                            .collection("completedWorkouts")
                            .document(currentWorkout.getWorkoutId())
                            .set(data)
                            .addOnSuccessListener(voida -> {
                                Snackbar.make(requireView(), "Workout marked complete!", Snackbar.LENGTH_SHORT).show();
                                Toast.makeText(requireContext(), "Workout marked complete!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Workout marked complete in subcollection.");
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(requireView(), "Failed to mark complete: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                Toast.makeText(requireContext(), "Error marking workout complete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error marking workout complete", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Failed to ensure user document: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(requireContext(), "Error ensuring user doc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error ensuring user document", e);
                });
    }
}
