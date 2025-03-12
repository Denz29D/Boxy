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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class StatsFragment extends Fragment {

    private static final String TAG = "StatsFragment";
    private TextView tvTotalPushups, tvPushupRecord, tvTotalWorkouts;
    private ImageButton btnBack;

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        tvTotalPushups = view.findViewById(R.id.tv_total_pushups);
        tvPushupRecord = view.findViewById(R.id.tv_pushup_record);
        tvTotalWorkouts = view.findViewById(R.id.tv_total_workouts);
        btnBack = view.findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        loadPushupStats();
        loadPushupRecord();
        loadWorkoutStats();

        return view;
    }

    // Sums up all push-ups in the pushup_records subcollection
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
                    Toast.makeText(requireContext(), "Failed to load pushup stats: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading pushup stats", e);
                });
    }

    // Finds the maximum pushUpCount value from pushup_records
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
                    Toast.makeText(requireContext(), "Failed to load pushup record: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading pushup record", e);
                });
    }

    // Loads the count of completed workouts
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
                    Toast.makeText(requireContext(), "Failed to load workout stats: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading workout stats", e);
                });
    }
}
