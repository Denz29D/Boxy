package com.example.boxy;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.boxy.models.Workout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.HashMap;
import java.util.Map;

public class WorkoutDetails extends Fragment {

    private String workoutId;
    private Workout currentWorkout;

    // UI references
    private TextView tvWorkoutTitle, tvWorkoutDescription, tvDuration, tvDifficulty, tvCalories, tvFullDescription;
    private ImageView ivWorkout;
    private Button btnStartWorkout;
    private ImageButton btnBack; // ✅ FIX: Change Button to ImageButton
    private YouTubePlayerView youtubePlayerView;

    public WorkoutDetails() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        btnBack = view.findViewById(R.id.btn_back); // ✅ FIX: Correctly reference the ImageButton
        youtubePlayerView = view.findViewById(R.id.youtube_player_view);

        getLifecycle().addObserver(youtubePlayerView);

        loadWorkoutDetails();

        btnStartWorkout.setOnClickListener(v -> markWorkoutComplete());

        // Back button navigation fix
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void loadWorkoutDetails() {
        if (TextUtils.isEmpty(workoutId)) return;

        FirebaseFirestore.getInstance().collection("workouts")
                .document(workoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentWorkout = documentSnapshot.toObject(Workout.class);
                        if (currentWorkout != null) {
                            populateUI(currentWorkout);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Failed to load workout.", Snackbar.LENGTH_SHORT).show();
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

        Glide.with(this).load(workout.getImageUrl()).into(ivWorkout);

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
        if (currentWorkout == null) return;
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("workoutId", currentWorkout.getWorkoutId());
        data.put("completedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("completedWorkouts")
                .document(currentWorkout.getWorkoutId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(requireView(), "Workout marked complete!", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Failed to mark complete: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });
    }
}
