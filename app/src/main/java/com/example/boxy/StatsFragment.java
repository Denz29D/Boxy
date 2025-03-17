/****************************************************************************
 * StatsFragment.java
 * This fragment displays overall statistics, push-up records, and workout data.
 ****************************************************************************/
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class StatsFragment extends Fragment {

    private static final String TAG = "StatsFragment";

    // UI references for statistics display.
    private TextView tvTotalPushups, tvPushupRecord, tvTotalWorkouts;
    private ImageButton btnBack;
    private MaterialButton btnTimeFilter;
    private TabLayout tabLayout;

    public StatsFragment() {
        // Empty public constructor.
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout (assumed to be fragment_stats.xml).
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Bind UI components.
        btnBack = view.findViewById(R.id.btn_back);
        tvTotalPushups = view.findViewById(R.id.tv_total_pushups);
        tvPushupRecord = view.findViewById(R.id.tv_pushup_record);
        tvTotalWorkouts = view.findViewById(R.id.tv_total_workouts);
        btnTimeFilter = view.findViewById(R.id.btn_time_filter);
        tabLayout = view.findViewById(R.id.tab_layout);

        // Set the back button functionality.
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // Time filter button (optional).
        btnTimeFilter.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Time filter clicked", Toast.LENGTH_SHORT).show()
        );

        // Respond to tab selection changes.
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d(TAG, "Selected tab position: " + position);
                // Update UI based on selected tab if needed.
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No action required.
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No action required.
            }
        });

        // Load relevant user statistics from Firebase.
        loadPushupStats();
        loadPushupRecord();
        loadWorkoutStats();

        return view;
    }

    /*
     * Retrieves the sum of all push-ups from the user's pushup_records subcollection.
     */
    private void loadPushupStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvTotalPushups.setText("0");
            Toast.makeText(requireContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("pushup_records")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalPushups = 0;
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        Long count = doc.getLong("pushUpCount");
                        if (count != null) {
                            totalPushups += count.intValue();
                        }
                    }
                    tvTotalPushups.setText(String.valueOf(totalPushups));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to load pushup stats: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading pushup stats", e);
                });
    }

    /*
     * Retrieves the highest recorded push-up count from the pushup_records subcollection.
     */
    private void loadPushupRecord() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvPushupRecord.setText("0");
            Toast.makeText(requireContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("pushup_records")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int maxPushups = 0;
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        Long count = doc.getLong("pushUpCount");
                        if (count != null && count.intValue() > maxPushups) {
                            maxPushups = count.intValue();
                        }
                    }
                    tvPushupRecord.setText(String.valueOf(maxPushups));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to load pushup record: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading pushup record", e);
                });
    }

    /*
     * Retrieves the count of completed workouts from the user's completedWorkouts subcollection.
     */
    private void loadWorkoutStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvTotalWorkouts.setText("0");
            Toast.makeText(requireContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("completedWorkouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalWorkouts = queryDocumentSnapshots.size();
                    tvTotalWorkouts.setText(String.valueOf(totalWorkouts));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to load workout stats: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading workout stats", e);
                });
    }
}
