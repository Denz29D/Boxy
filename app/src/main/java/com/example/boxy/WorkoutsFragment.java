package com.example.boxy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boxy.adapters.WorkoutAdapter;
import com.example.boxy.models.Workout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkoutsFragment extends Fragment implements WorkoutAdapter.OnWorkoutActionListener {

    private RecyclerView rvWorkouts;
    private WorkoutAdapter adapter;
    private List<Workout> workoutList = new ArrayList<>();

    public WorkoutsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workouts, container, false);

        rvWorkouts = view.findViewById(R.id.rv_workouts);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pass "this" for the OnWorkoutActionListener
        adapter = new WorkoutAdapter(requireContext(), workoutList, this);
        rvWorkouts.setAdapter(adapter);

        loadWorkouts();
        return view;
    }
    //load workouts from database
    private void loadWorkouts() {
        FirebaseFirestore.getInstance().collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    workoutList.clear();
                    queryDocumentSnapshots.forEach(doc -> {
                        // Convert the Firestore doc into a Workout object
                        Workout workout = doc.toObject(Workout.class);
                        if (workout != null) {
                            //  set the doc ID as the workoutId
                            // so it's never null when we navigate to details.
                            workout.setWorkoutId(doc.getId());
                            workoutList.add(workout);
                        }
                    });
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load workouts: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // 1) Called when user taps "Mark Complete" button, saves to Firestore ina subcollection
    @Override
    public void onMarkWorkoutComplete(Workout workout) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("completedWorkouts")
                .document(workout.getWorkoutId())
                .set(Map.of(
                        "workoutId", workout.getWorkoutId(),
                        "completedAt", FieldValue.serverTimestamp()
                ))
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Workout marked complete", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to mark complete: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // 2) Called when user taps "Start" then navigate to WorkoutDetails manually
    @Override
    public void onWorkoutClicked(Workout workout) {
        // Create the details fragment and pass the doc ID via arguments
        WorkoutDetails detailsFragment = new WorkoutDetails();
        Bundle bundle = new Bundle();
        bundle.putString("workoutId", workout.getWorkoutId());
        detailsFragment.setArguments(bundle);

        // Replace the current fragment with WorkoutDetails and add to back stack
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, detailsFragment)
                .addToBackStack(null)
                .commit();
    }
}
