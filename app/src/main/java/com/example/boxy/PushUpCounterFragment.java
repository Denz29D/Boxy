package com.example.boxy;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PushUpCounterFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    // Variables for push-up counting
    private float previousZ = 0;
    private int pushUpCount = 0;
    private boolean isGoingDown = false;
    private static final float PUSH_UP_THRESHOLD = 2.0f; // May need calibration per device
    private static final long MIN_TIME_BETWEEN_PUSHUPS = 1000; // 1 second debounce
    private long lastPushUpTime = 0;

    // UI references
    private TextView tvPushUpCount;
    private ImageButton btnBack;
    private Button btnStart, btnStop, btnReset, btnSave;

    // State flag for counting
    private boolean isCounting = false;

    public PushUpCounterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_push_up_counter, container, false);
        tvPushUpCount = view.findViewById(R.id.tv_pushup_count);
        tvPushUpCount.setText("Push-ups: " + pushUpCount);

        // Initialize back button
        btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Initialize control buttons
        btnStart = view.findViewById(R.id.btn_start);
        btnStop = view.findViewById(R.id.btn_stop);
        btnReset = view.findViewById(R.id.btn_reset);
        btnSave = view.findViewById(R.id.btn_save);

        btnStart.setOnClickListener(v -> startCounting());
        btnStop.setOnClickListener(v -> stopCounting());
        btnReset.setOnClickListener(v -> resetCounter());
        btnSave.setOnClickListener(v -> savePushupData());

        sensorManager = (SensorManager) requireActivity().getSystemService(android.content.Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Toast.makeText(requireContext(), "Sensor Manager not available", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only register sensor if counting is active.
        if (isCounting && sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister sensor when the fragment is paused
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Only process sensor data if counting is active.
        if (!isCounting) return;

        float z = event.values[2];
        long currentTime = System.currentTimeMillis();

        // Only count if enough time has passed (debounce)
        if (currentTime - lastPushUpTime > MIN_TIME_BETWEEN_PUSHUPS) {
            // Detect downward motion.
            if (!isGoingDown && (previousZ - z) > PUSH_UP_THRESHOLD) {
                isGoingDown = true;
            }
            // Detect upward motion after downward motion to complete a push-up.
            if (isGoingDown && (z - previousZ) > PUSH_UP_THRESHOLD) {
                pushUpCount++;
                isGoingDown = false;
                lastPushUpTime = currentTime;
                tvPushUpCount.setText("Push-ups: " + pushUpCount);
                Toast.makeText(requireContext(), "Push-up count: " + pushUpCount, Toast.LENGTH_SHORT).show();
            }
        }
        previousZ = z;
    }

    @Override
    public void onAccuracyChanged(@NonNull android.hardware.Sensor sensor, int accuracy) {
        // No action needed for this example.
    }

    private void startCounting() {
        if (!isCounting) {
            isCounting = true;
            // Register sensor listener if not already registered.
            if (sensorManager != null && accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
            Toast.makeText(requireContext(), "Push-up counter started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopCounting() {
        if (isCounting) {
            isCounting = false;
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
            Toast.makeText(requireContext(), "Push-up counter stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetCounter() {
        pushUpCount = 0;
        tvPushUpCount.setText("Push-ups: " + pushUpCount);
        Toast.makeText(requireContext(), "Push-up counter reset", Toast.LENGTH_SHORT).show();
    }

    private void savePushupData() {
        // Get the currently signed-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Create a map with the data you want to save
            Map<String, Object> data = new HashMap<>();
            data.put("pushUpCount", pushUpCount);
            data.put("date", new Timestamp(new Date()));

            // Save data in a subcollection (e.g., "pushup_records") under the user's document
            db.collection("users")
                    .document(user.getUid())
                    .collection("pushup_records")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(requireContext(), "Push-up data saved successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "No signed-in user", Toast.LENGTH_SHORT).show();
        }
    }
}
