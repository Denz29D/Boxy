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
import com.bumptech.glide.Glide;  // Make sure you have Glide added as a dependency
import com.example.boxy.R;
import com.example.boxy.models.Workout;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private Context context;
    private List<Workout> workoutList;
    private OnWorkoutActionListener listener;

    // Interface for handling user actions
    public interface OnWorkoutActionListener {
        void onMarkWorkoutComplete(Workout workout);
        void onWorkoutClicked(Workout workout);
    }

    public WorkoutAdapter(Context context, List<Workout> workoutList, OnWorkoutActionListener listener) {
        this.context = context;
        this.workoutList = workoutList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);

        // Use Glide to load and downsample the image resource
        String rawImageUrl = workout.getImageUrl(); // e.g. "hiit_workout.jpg"
        if (rawImageUrl != null && !rawImageUrl.isEmpty()) {
            int dotIndex = rawImageUrl.lastIndexOf('.');
            String resourceName = (dotIndex > 0) ? rawImageUrl.substring(0, dotIndex) : rawImageUrl;

            // Find the drawable resource identifier
            int resId = context.getResources().getIdentifier(
                    resourceName,  // e.g., "hiit_workout"
                    "drawable",
                    context.getPackageName()
            );

            if (resId != 0) {
                Glide.with(context)
                        .load(resId)
                        // Optionally set an override to a reasonable size; adjust dimensions as needed.
                        .override(800, 800)
                        .centerCrop()
                        .into(holder.ivWorkout);
            }
        }

        // Set the workout title
        holder.tvWorkoutName.setText(workout.getTitle());

        // Set additional info text
        String infoText = workout.getDuration() + " • "
                + workout.getDifficulty() + " • "
                + workout.getCalories() + " calories";
        holder.tvWorkoutDescription.setText(infoText);

        // "Start" button click listener
        holder.btnStart.setOnClickListener(v -> {
            Toast.makeText(context, "Navigating to workout details", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onWorkoutClicked(workout);
            }
        });

        // "Mark Complete" button click listener, if applicable
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
