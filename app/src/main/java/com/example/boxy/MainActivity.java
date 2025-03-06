package com.example.boxy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the Toolbar as the ActionBar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize bottom navigation.
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Get the NavHostFragment and its NavController.
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            throw new IllegalStateException("NavHostFragment not found");
        }

        // Define top-level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_workouts,
                R.id.nav_discover,
                R.id.nav_stats,
                R.id.nav_profile
        ).build();

        // Set up the ActionBar and BottomNavigationView with the NavController.
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Handle optional navigation extra.
        String navigateTo = getIntent().getStringExtra("navigateTo");
        if (navigateTo != null) {
            if (navigateTo.equalsIgnoreCase("home")) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            } else if (navigateTo.equalsIgnoreCase("workouts")) {
                bottomNavigationView.setSelectedItemId(R.id.nav_workouts);
            } else if (navigateTo.equalsIgnoreCase("discover")) {
                bottomNavigationView.setSelectedItemId(R.id.nav_discover);
            } else if (navigateTo.equalsIgnoreCase("stats")) {
                bottomNavigationView.setSelectedItemId(R.id.nav_stats);
            } else if (navigateTo.equalsIgnoreCase("profile")) {
                bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            } else {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
