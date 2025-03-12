package com.example.boxy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boxy.adapters.LeaderboardsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardsFragment extends Fragment {

    private static final String TAG = "LeaderboardsFragment";
    private RecyclerView rvLeaderboards;
    private ImageButton btnBack;
    private LeaderboardsAdapter leaderboardAdapter;
    private List<LeaderboardUser> leaderboardList = new ArrayList<>();

    // Simple model to hold user stats
    public static class LeaderboardUser {
        public String userId;
        public String fullName;
        public int pushupRecord;
        public int workoutsCompleted;

        public LeaderboardUser(String userId, String fullName, int pushupRecord, int workoutsCompleted) {
            this.userId = userId;
            this.fullName = fullName;
            this.pushupRecord = pushupRecord;
            this.workoutsCompleted = workoutsCompleted;
        }
    }

    public LeaderboardsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboards, container, false);
        rvLeaderboards = view.findViewById(R.id.rv_leaderboards);
        btnBack = view.findViewById(R.id.btn_back);

        rvLeaderboards.setLayoutManager(new LinearLayoutManager(getContext()));
        leaderboardAdapter = new LeaderboardsAdapter(getContext(), leaderboardList);
        rvLeaderboards.setAdapter(leaderboardAdapter);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        loadUsers();
        return view;
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    leaderboardList.clear();
                    // For each user document, create a LeaderboardUser object and load stats.
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String userId = doc.getId();
                        String fullName = doc.getString("fullName");
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = "Anonymous";
                        }
                        LeaderboardUser user = new LeaderboardUser(userId, fullName, 0, 0);
                        leaderboardList.add(user);
                        loadUserStats(user);
                    }
                    leaderboardAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading users", e);
                });
    }

    // Load a user's pushup record and workout count.
    private void loadUserStats(LeaderboardUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Load pushup record: maximum pushUpCount value from the pushup_records subcollection
        db.collection("users").document(user.userId)
                .collection("pushup_records")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int maxRecord = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Long count = doc.getLong("pushUpCount");
                        if (count != null && count.intValue() > maxRecord) {
                            maxRecord = count.intValue();
                        }
                    }
                    user.pushupRecord = maxRecord;
                    leaderboardAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading pushup records for user " + user.userId, e));

        // Load workouts completed: count the documents in the completedWorkouts subcollection
        db.collection("users").document(user.userId)
                .collection("completedWorkouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    user.workoutsCompleted = queryDocumentSnapshots.size();
                    leaderboardAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading workouts for user " + user.userId, e));
    }
}
