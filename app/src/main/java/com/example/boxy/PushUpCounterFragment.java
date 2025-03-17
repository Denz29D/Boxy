package com.example.boxy;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

/*
 * Fragment that implements a push-up counter using the device's accelerometer.
 * The app listens for vertical motion to detect push-up repetitions, manages a session timer,
 * and saves the session data to Firestore when requested.
 */
public class PushUpCounterFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    // Variables for counting push-ups.
    private float previousZ = 0;
    private int pushUpCount = 0;
    private boolean isGoingDown = false;
    private static final float PUSH_UP_THRESHOLD = 2.0f; // Threshold value for detecting push-up motion.
    private static final long MIN_TIME_BETWEEN_PUSHUPS = 1000; // Minimum time (in ms) between counted push-ups.
    private long lastPushUpTime = 0;

    // UI elements for displaying count and session time.
    private TextView tvPushUpCount;
    private TextView tvSessionTime;
    private ImageButton btnBack;
    private Button btnStart, btnStop, btnReset, btnSave;

    // Timer variables.
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            tvSessionTime.setText(String.format("Time: %02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    // Flag to indicate if counting is active.
    private boolean isCounting = false;

    public PushUpCounterFragment() {
        // Required empty public constructor.
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_push_up_counter, container, false);

        // Bind and initialise UI elements.
        tvPushUpCount = view.findViewById(R.id.tv_pushup_count);
        tvPushUpCount.setText("Push-ups: " + pushUpCount);

        tvSessionTime = view.findViewById(R.id.tv_session_time);
        tvSessionTime.setText("Time: 00:00");

        btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnStart = view.findViewById(R.id.btn_start);
        btnStop = view.findViewById(R.id.btn_stop);
        btnReset = view.findViewById(R.id.btn_reset);
        btnSave = view.findViewById(R.id.btn_save);

        // Set click listeners for control buttons.
        btnStart.setOnClickListener(v -> startCounting());
        btnStop.setOnClickListener(v -> stopCounting());
        btnReset.setOnClickListener(v -> resetCounter());
        btnSave.setOnClickListener(v -> savePushupData());

        // Initialise SensorManager and obtain the accelerometer sensor.
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
        // Register sensor listener only if counting is active.
        if (isCounting && sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister sensor listener and stop the timer to conserve resources.
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Process sensor data only when counting is active.
        if (!isCounting) return;

        float z = event.values[2];
        long currentTime = System.currentTimeMillis();

        // Enforce debounce by checking time difference between push-ups.
        if (currentTime - lastPushUpTime > MIN_TIME_BETWEEN_PUSHUPS) {
            // Detect downward movement by comparing the current and previous Z values.
            if (!isGoingDown && (previousZ - z) > PUSH_UP_THRESHOLD) {
                isGoingDown = true;
            }
            // Detect upward movement after a downward phase to complete a push-up.
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
    public void onAccuracyChanged(@NonNull Sensor sensor, int accuracy) {
        // No additional action needed for accuracy changes in this implementation.
    }

    // Starts the push-up counting session.
    private void startCounting() {
        if (!isCounting) {
            isCounting = true;
            if (sensorManager != null && accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
            startTimer();
            Toast.makeText(requireContext(), "Push-up counter started", Toast.LENGTH_SHORT).show();
        }
    }

    // Stops the push-up counting session.
    private void stopCounting() {
        if (isCounting) {
            isCounting = false;
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
            stopTimer();
            Toast.makeText(requireContext(), "Push-up counter stopped", Toast.LENGTH_SHORT).show();
        }
    }

    // Resets the push-up counter and session timer.
    private void resetCounter() {
        pushUpCount = 0;
        tvPushUpCount.setText("Push-ups: " + pushUpCount);
        tvSessionTime.setText("Time: 00:00");
        Toast.makeText(requireContext(), "Push-up counter reset", Toast.LENGTH_SHORT).show();
    }

    // Saves the current push-up session data to Firestore.
    private void savePushupData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Prepare a map with the push-up count and timestamp.
            Map<String, Object> data = new HashMap<>();
            data.put("pushUpCount", pushUpCount);
            data.put("date", new Timestamp(new Date()));

            // Save the push-up record under the user's document.
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

    // Starts the session timer.
    private void startTimer() {
        startTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);
    }

    // Stops the session timer.
    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }
}
