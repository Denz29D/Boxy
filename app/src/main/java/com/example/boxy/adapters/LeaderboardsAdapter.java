package com.example.boxy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.boxy.LeaderboardsFragment.LeaderboardUser;
import com.example.boxy.R;
import java.util.List;

public class LeaderboardsAdapter extends RecyclerView.Adapter<LeaderboardsAdapter.ViewHolder> {

    private Context context;
    private List<LeaderboardUser> leaderboardList;

    public LeaderboardsAdapter(Context context, List<LeaderboardUser> leaderboardList) {
        this.context = context;
        this.leaderboardList = leaderboardList;
    }

    public void setData(List<LeaderboardUser> data) {
        this.leaderboardList = data;
        notifyDataSetChanged();
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
        holder.tvFullName.setText(user.fullName);
        holder.tvPushupRecord.setText(String.valueOf(user.pushupRecord));
        holder.tvWorkoutsCompleted.setText(String.valueOf(user.workoutsCompleted));
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvPushupRecord, tvWorkoutsCompleted;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvPushupRecord = itemView.findViewById(R.id.tv_pushup_record);
            tvWorkoutsCompleted = itemView.findViewById(R.id.tv_workouts_completed);
        }
    }
}
