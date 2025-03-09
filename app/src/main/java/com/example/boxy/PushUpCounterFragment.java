package com.example.boxy;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
    private ImageButton btnBack; // Updated to ImageButton to match the XML

    public PushUpCounterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_push_up_counter, container, false);
        tvPushUpCount = view.findViewById(R.id.tv_pushup_count);
        tvPushUpCount.setText("Push-ups: " + pushUpCount);

        // Initialize and set up the back button
        btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

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
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Use the z-axis for vertical movement detection.
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
    public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
        // No action needed for this example.
    }
}
