package com.example.boxy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/*
 * MainActivity hosts the primary fragments of the app.
 * The app uses a BottomNavigationView to switch between Home, Workouts, Profile, and Stats fragments.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the main activity layout.
        setContentView(R.layout.activity_main);

        // Bind the BottomNavigationView from the layout.
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // Set up the listener to handle navigation item selection.
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_workouts) {
                replaceFragment(new WorkoutsFragment());
            } else if (id == R.id.nav_profile) {
                replaceFragment(new Profile());
            } else if (id == R.id.nav_stats) {
                replaceFragment(new StatsFragment());
            }
            return true;
        });

        // Set the default fragment to display.
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    // Replaces the current fragment in the container with the specified fragment.
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }
}
