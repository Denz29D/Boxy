package com.example.boxy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Removed toolbar setup since the toolbar has been removed from the layout.
        // Toolbar toolbar = findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_workouts) {
                replaceFragment(new WorkoutsFragment());
            } else if (id == R.id.nav_profile) {
                replaceFragment(new Profile());
            }
            else if (id == R.id.nav_stats) {
                replaceFragment(new StatsFragment());
            }
            return true;
        });

        // Set the default fragment.
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
