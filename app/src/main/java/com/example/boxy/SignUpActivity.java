package com.example.boxy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * Activity for signing up new users.
 * The app uses Firebase Authentication to create an account and Firestore to store user details.
 * Edge-to-edge display is enabled by applying window insets.
 */
public class SignUpActivity extends AppCompatActivity {

    // FirebaseAuth instance for user sign-up operations.
    private FirebaseAuth mAuth;
    // FirebaseFirestore instance for storing user details.
    private FirebaseFirestore db;
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for the sign-up activity.
        setContentView(R.layout.activity_sign_up);
        // Apply window insets to enable edge-to-edge display.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Authentication and Firestore instances.
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the sign-up button click listener.
        Button signUpButton = findViewById(R.id.btn_signup);
        signUpButton.setOnClickListener(v -> signupButtonClicked());

        // Set up the login link to navigate to LoginActivity.
        TextView loginLink = findViewById(R.id.tv_login);
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        });
    }

    // Triggered when the sign-up button is clicked.
    private void signupButtonClicked() {
        // Retrieve user input values.
        EditText nameEditText = findViewById(R.id.et_name);
        EditText emailEditText = findViewById(R.id.et_email);
        EditText passwordEditText = findViewById(R.id.et_password);
        EditText confirmPasswordEditText = findViewById(R.id.et_confirm_password);

        String fullName = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate that all fields are filled.
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure the password and confirmation match.
        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed with sign-up using Firebase Authentication.
        signup(email, password, fullName);
    }

    // Signs up a new user and stores their data in Firestore.
    private void signup(String email, String password, String fullName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignUpActivity.this, "Sign up successful. Redirecting...", Toast.LENGTH_SHORT).show();

                        // Prepare a map of user data to store.
                        String uid = user.getUid();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", uid);
                        userData.put("fullName", fullName);
                        userData.put("email", email);
                        userData.put("createdAt", FieldValue.serverTimestamp());
                        userData.put("updatedAt", FieldValue.serverTimestamp());
                        userData.put("favoriteWorkouts", new ArrayList<String>());

                        // Store user data in the "users" collection under the user's UID.
                        db.collection("users").document(uid)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User document added successfully");
                                    navigateToHome();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error adding user document", e);
                                    Toast.makeText(SignUpActivity.this, "Failed to store user data.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Sign up failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Navigates to MainActivity after successful sign-up.
    private void navigateToHome() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish SignUpActivity to prevent returning to it.
    }
}
