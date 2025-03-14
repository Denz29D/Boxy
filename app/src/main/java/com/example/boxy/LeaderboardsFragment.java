package com.example.boxy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.boxy.adapters.LeaderboardsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LeaderboardsFragment extends Fragment {

    private static final String TAG = "LeaderboardsFragment";

    private RecyclerView rvLeaderboards;
    private ImageButton btnBack;
    private FloatingActionButton fabFilter;
    private SwipeRefreshLayout swipeRefresh;
    private TabLayout tabLayout;
    private TextView tvLeaderboardMetricHeader;

    private LeaderboardsAdapter leaderboardAdapter;
    private List<LeaderboardUser> leaderboardList = new ArrayList<>();

    // Track how many users we need to load, and how many we've loaded so far.
    private int totalUsersToLoad = 0;
    private int loadedUsersCount = 0;

    // Time filter options: "ALL_TIME" or "LAST_7_DAYS"
    private String currentTimeFilter = "ALL_TIME";

    // Model to hold user stats
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

        // Bind views
        rvLeaderboards = view.findViewById(R.id.rv_leaderboards);
        btnBack = view.findViewById(R.id.btn_back);
        fabFilter = view.findViewById(R.id.fab_filter);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        // TabLayout and the header text for the metric column
        tabLayout = view.findViewById(R.id.tab_layout);
        tvLeaderboardMetricHeader = view.findViewById(R.id.tv_leaderboard_metric);

        // Set up RecyclerView
        rvLeaderboards.setLayoutManager(new LinearLayoutManager(getContext()));
        leaderboardAdapter = new LeaderboardsAdapter(getContext(), leaderboardList);
        rvLeaderboards.setAdapter(leaderboardAdapter);

        // Back button functionality
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // Listen for tab selections
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 0 = Overall, 1 = Push-ups, 2 = Workouts
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        // Overall
                        leaderboardAdapter.setMetricType(0);
                        tvLeaderboardMetricHeader.setText("Score");
                        break;
                    case 1:
                        // Push-ups
                        leaderboardAdapter.setMetricType(1);
                        tvLeaderboardMetricHeader.setText("Push-ups");
                        break;
                    case 2:
                        // Workouts
                        leaderboardAdapter.setMetricType(2);
                        tvLeaderboardMetricHeader.setText("Workouts");
                        break;
                }
                // Reload user data (push-up records, workouts) so the new metric can be displayed
                loadUsers();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not used
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not used
            }
        });

        // FAB to toggle time filter between All Time and Last 7 Days
        fabFilter.setOnClickListener(v -> {
            if (currentTimeFilter.equals("ALL_TIME")) {
                currentTimeFilter = "LAST_7_DAYS";
                Toast.makeText(getContext(), "Filtering: Last 7 Days", Toast.LENGTH_SHORT).show();
            } else {
                currentTimeFilter = "ALL_TIME";
                Toast.makeText(getContext(), "Filtering: All Time", Toast.LENGTH_SHORT).show();
            }
            loadUsers();
        });

        // Swipe-to-refresh functionality
        swipeRefresh.setOnRefreshListener(this::loadUsers);

        // Initial load of leaderboard data
        loadUsers();

        return view;
    }

    // Computes the cutoff date based on currentTimeFilter.
    private Date getCutoffDate() {
        Calendar cal = Calendar.getInstance();
        if (currentTimeFilter.equals("LAST_7_DAYS")) {
            cal.add(Calendar.DAY_OF_YEAR, -7);
        }
        return cal.getTime();
    }

    // Loads all users from the "users" collection, then for each user, load stats with filtering.
    private void loadUsers() {
        swipeRefresh.setRefreshing(true);

        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    leaderboardList.clear();
                    loadedUsersCount = 0; // reset for the new load
                    totalUsersToLoad = queryDocumentSnapshots.size();

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

                    // We sort/notify after each user is loaded (in loadUserStats)
                    // once loadedUsersCount == totalUsersToLoad
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading users", e);
                    swipeRefresh.setRefreshing(false);
                });
    }

    // Loads a user's stats (pushup record and workouts completed) with filtering applied.
    private void loadUserStats(LeaderboardUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query for pushup records
        Query pushupQuery = db.collection("users").document(user.userId)
                .collection("pushup_records");
        // Query for completed workouts
        Query workoutsQuery = db.collection("users").document(user.userId)
                .collection("completedWorkouts");

        if (!currentTimeFilter.equals("ALL_TIME")) {
            Date cutoff = getCutoffDate();
            pushupQuery = pushupQuery.whereGreaterThanOrEqualTo("timestamp", cutoff);
            workoutsQuery = workoutsQuery.whereGreaterThanOrEqualTo("timestamp", cutoff);
        }

        Query finalWorkoutsQuery = workoutsQuery;
        pushupQuery.get()
                .addOnSuccessListener(pushupSnapshots -> {
                    // Find the max pushUpCount
                    int maxRecord = 0;
                    for (DocumentSnapshot doc : pushupSnapshots) {
                        Long count = doc.getLong("pushUpCount");
                        if (count != null && count.intValue() > maxRecord) {
                            maxRecord = count.intValue();
                        }
                    }
                    user.pushupRecord = maxRecord;

                    // Now load workouts
                    finalWorkoutsQuery.get()
                            .addOnSuccessListener(workoutSnapshots -> {
                                user.workoutsCompleted = workoutSnapshots.size();

                                // One user's data is now fully loaded
                                loadedUsersCount++;

                                // If all users are loaded, sort once and update UI
                                if (loadedUsersCount == totalUsersToLoad) {
                                    sortLeaderboard();
                                    leaderboardAdapter.notifyDataSetChanged();
                                    swipeRefresh.setRefreshing(false);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading workouts for user " + user.userId, e);
                                loadedUsersCount++;
                                // If all users are loaded, sort once and update UI
                                if (loadedUsersCount == totalUsersToLoad) {
                                    sortLeaderboard();
                                    leaderboardAdapter.notifyDataSetChanged();
                                    swipeRefresh.setRefreshing(false);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pushup records for user " + user.userId, e);
                    loadedUsersCount++;
                    // If all users are loaded, sort once and update UI
                    if (loadedUsersCount == totalUsersToLoad) {
                        sortLeaderboard();
                        leaderboardAdapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    // Sort the leaderboardList in descending order based on the current metric type:
    //   0 = Overall (pushupRecord + workoutsCompleted)
    //   1 = Push-ups (pushupRecord)
    //   2 = Workouts (workoutsCompleted)
    private void sortLeaderboard() {
        switch (leaderboardAdapter.getMetricType()) {
            case 0: // Overall
                Collections.sort(leaderboardList, (u1, u2) -> {
                    int score1 = u1.pushupRecord + u1.workoutsCompleted;
                    int score2 = u2.pushupRecord + u2.workoutsCompleted;
                    // Sort descending
                    return Integer.compare(score2, score1);
                });
                break;
            case 1: // Push-ups
                Collections.sort(leaderboardList, (u1, u2) ->
                        Integer.compare(u2.pushupRecord, u1.pushupRecord)
                );
                break;
            case 2: // Workouts
                Collections.sort(leaderboardList, (u1, u2) ->
                        Integer.compare(u2.workoutsCompleted, u1.workoutsCompleted)
                );
                break;
        }
    }
}
