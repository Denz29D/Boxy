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

/**
 * Displays a leaderboard of users. Supports three metric types:
 *  0 = Overall (push-ups + workouts)
 *  1 = Push-ups only
 *  2 = Workouts only
 */
public class LeaderboardsAdapter extends RecyclerView.Adapter<LeaderboardsAdapter.ViewHolder> {

    private final Context context;
    private final List<LeaderboardUser> leaderboardList;
    // Default metric type is Overall
    private int metricType = 0;

    public LeaderboardsAdapter(Context context, List<LeaderboardUser> leaderboardList) {
        this.context = context;
        this.leaderboardList = leaderboardList;
    }

    /**
     * Updates the metric type and refreshes the list.
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardsAdapter.ViewHolder holder, int position) {
        LeaderboardUser user = leaderboardList.get(position);

        // Rank is based on the sorted position (1-indexed)
        holder.tvRank.setText(String.valueOf(position + 1));

        // Show medal icons for the top three positions (customize icons as needed)
        if (position == 0) {
            holder.ivMedal.setVisibility(View.VISIBLE);
            // Optionally: holder.ivMedal.setImageResource(R.drawable.ic_gold_medal);
        } else if (position == 1) {
            holder.ivMedal.setVisibility(View.VISIBLE);
            // Optionally: holder.ivMedal.setImageResource(R.drawable.ic_silver_medal);
        } else if (position == 2) {
            holder.ivMedal.setVisibility(View.VISIBLE);
            // Optionally: holder.ivMedal.setImageResource(R.drawable.ic_bronze_medal);
        } else {
            holder.ivMedal.setVisibility(View.GONE);
        }

        // Set user info
        holder.tvFullName.setText(user.fullName);
        // Example static user level; update if you have dynamic level data
        holder.tvUserLevel.setText("Level 5");

        // Calculate and display the score based on metric type
        int score;
        if (metricType == 0) { // Overall
            score = user.pushupRecord + user.workoutsCompleted;
        } else if (metricType == 1) { // Push-ups
            score = user.pushupRecord;
        } else { // Workouts (metricType == 2)
            score = user.workoutsCompleted;
        }
        holder.tvScore.setText(String.valueOf(score));
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

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
