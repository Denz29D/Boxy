package com.example.boxy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boxy.LeaderboardsFragment.LeaderboardUser;
import com.example.boxy.R;

import java.util.List;

/*
 * Displays a leaderboard of users.
 * Supports three metric types:
 *   0 = Overall (push-ups + workouts)
 *   1 = Push-ups only
 *   2 = Workouts only
 */
public class LeaderboardsAdapter extends RecyclerView.Adapter<LeaderboardsAdapter.ViewHolder> {

    // Context for inflating layouts.
    private final Context context;
    // List of users to be displayed on the leaderboard.
    private final List<LeaderboardUser> leaderboardList;
    // Default metric type is Overall.
    private int metricType = 0;

    // Constructor to initialise context and leaderboard list.
    public LeaderboardsAdapter(Context context, List<LeaderboardUser> leaderboardList) {
        this.context = context;
        this.leaderboardList = leaderboardList;
    }

    /**
     * Updates the metric type and refreshes the leaderboard.
     * @param metricType 0 = Overall, 1 = Push-ups, 2 = Workouts
     */
    public void setMetricType(int metricType) {
        this.metricType = metricType;
        notifyDataSetChanged();
    }

    public int getMetricType() {
        return metricType;
    }

    @NonNull
    @Override
    public LeaderboardsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the leaderboard item layout.
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardsAdapter.ViewHolder holder, int position) {
        LeaderboardUser user = leaderboardList.get(position);

        // Rank is based on the sorted position (1-indexed).
        holder.tvRank.setText(String.valueOf(position + 1));

        // Display medal icons for the top three positions.
        if (position == 0) {
            holder.ivMedal.setVisibility(View.VISIBLE);
            // Optionally have a gold medal bage for the top user.
        } else if (position == 1) {
            holder.ivMedal.setVisibility(View.VISIBLE);
            // Optionally: have a silver medal bage for the second user.
        } else if (position == 2) {
            holder.ivMedal.setVisibility(View.VISIBLE);
            // Optionally: have a bronze medal bage for the third user.
        } else {
            holder.ivMedal.setVisibility(View.GONE);
        }

        // Set the full name of the user.
        holder.tvFullName.setText(user.fullName);
        // Display a static user level (update if dynamic level data is available).
        holder.tvUserLevel.setText("Level 5");

        // Calculate and display the score based on the selected metric.
        int score;
        if (metricType == 0) { // Overall
            score = user.pushupRecord + user.workoutsCompleted;
        } else if (metricType == 1) { // Push-ups only
            score = user.pushupRecord;
        } else { // Workouts only (metricType == 2)
            score = user.workoutsCompleted;
        }
        holder.tvScore.setText(String.valueOf(score));
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    // ViewHolder class to hold references to UI components for each leaderboard item.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvFullName, tvUserLevel, tvScore;
        ImageView ivMedal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvUserLevel = itemView.findViewById(R.id.tv_user_level);
            tvScore = itemView.findViewById(R.id.tv_score);
            ivMedal = itemView.findViewById(R.id.iv_medal);
        }
    }
}
