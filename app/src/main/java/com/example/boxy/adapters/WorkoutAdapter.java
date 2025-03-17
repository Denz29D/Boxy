package com.example.boxy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Glide is used for image loading and caching.
import com.example.boxy.R;
import com.example.boxy.models.Workout;

import java.util.List;

/*
 * Adapter that binds a list of workouts to a RecyclerView.
 * The app displays workout details and provides actions to start or mark a workout complete.
 */
public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private Context context;
    private List<Workout> workoutList;
    private OnWorkoutActionListener listener;

    // Interface for handling workout actions.
    public interface OnWorkoutActionListener {
        void onMarkWorkoutComplete(Workout workout);
        void onWorkoutClicked(Workout workout);
    }

    // Constructor to initialise the adapter with context, workout list, and an action listener.
    public WorkoutAdapter(Context context, List<Workout> workoutList, OnWorkoutActionListener listener) {
        this.context = context;
        this.workoutList = workoutList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the workout item layout.
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        // Retrieve the workout object for the current position.
        Workout workout = workoutList.get(position);

        // Load and downsample the image using Glide.
        String rawImageUrl = workout.getImageUrl(); // Example: "hiit_workout.jpg"
        if (rawImageUrl != null && !rawImageUrl.isEmpty()) {
            // Remove file extension from the image name.
            int dotIndex = rawImageUrl.lastIndexOf('.');
            String resourceName = (dotIndex > 0) ? rawImageUrl.substring(0, dotIndex) : rawImageUrl;

            // Find the drawable resource identifier using the resource name.
            int resId = context.getResources().getIdentifier(
                    resourceName,  // For instance, "hiit_workout"
                    "drawable",
                    context.getPackageName()
            );

            if (resId != 0) {
                // Load the image into the ImageView using Glide.
                Glide.with(context)
                        .load(resId)
                        .override(800, 800) // Optionally override dimensions for performance.
                        .centerCrop()
                        .into(holder.ivWorkout);
            }
        }

        // Set the workout title.
        holder.tvWorkoutName.setText(workout.getTitle());

        // Create a string combining duration, difficulty, and calories.
        String infoText = workout.getDuration() + " • "
                + workout.getDifficulty() + " • "
                + workout.getCalories() + " calories";
        holder.tvWorkoutDescription.setText(infoText);

        // Set up the "Start" button click listener.
        holder.btnStart.setOnClickListener(v -> {
            Toast.makeText(context, "Navigating to workout details", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onWorkoutClicked(workout);
            }
        });

        // Set up the "Mark Complete" button click listener if the button is available.
        if (holder.btnMarkComplete != null) {
            holder.btnMarkComplete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMarkWorkoutComplete(workout);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    // ViewHolder class holds references to UI components for each workout item.
    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWorkout;
        TextView tvWorkoutName, tvWorkoutDescription;
        Button btnStart, btnMarkComplete;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWorkout = itemView.findViewById(R.id.iv_workout);
            tvWorkoutName = itemView.findViewById(R.id.tv_workout_name);
            tvWorkoutDescription = itemView.findViewById(R.id.tv_workout_description);
            btnStart = itemView.findViewById(R.id.btn_start);

        }
    }
}
