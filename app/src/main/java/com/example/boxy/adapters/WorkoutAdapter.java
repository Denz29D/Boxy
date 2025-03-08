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
import com.bumptech.glide.Glide;
import com.example.boxy.R;
import com.example.boxy.models.Workout;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private Context context;
    private List<Workout> workoutList;
    private OnWorkoutActionListener listener;

    // Interface for handling workout actions (navigation & marking as complete)
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
        holder.tvWorkoutName.setText(workout.getTitle());
        holder.tvWorkoutDescription.setText(
                workout.getDuration() + " • " + workout.getDifficulty() + " • " + workout.getCalories() + " calories"
        );

        Glide.with(context).load(workout.getImageUrl()).into(holder.ivWorkout);

        // Start button -> Navigate to Workout Details (Handled in WorkoutsFragment)
        holder.btnStart.setOnClickListener(v -> {
            Toast.makeText(context, "Navigating to workout details", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onWorkoutClicked(workout);
            }
        });

        // Mark Complete button (optional: make sure it exists in item_workout.xml)
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
