package com.example.venueease;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AdminDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    // Define fragments
    private VenuesFragment venuesFragment;
    private BookingsFragment bookingsFragment;
    private NotificationsFragment notificationsFragment;
    private AdminProfileFragment adminProfileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize fragments
        venuesFragment = new VenuesFragment();
        bookingsFragment = new BookingsFragment();
        notificationsFragment = new NotificationsFragment();
        adminProfileFragment = new AdminProfileFragment();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load the default fragment (Venues)
        if (savedInstanceState == null) {
            loadFragment(venuesFragment);
        }

        // Set the listener for the bottom navigation
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_venues) {
                    selectedFragment = venuesFragment;
                } else if (itemId == R.id.nav_bookings) {
                    selectedFragment = bookingsFragment;
                } else if (itemId == R.id.nav_notifications) {
                    selectedFragment = notificationsFragment;
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = adminProfileFragment;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }

                return false;
            }
        });
    }

    /**
     * Helper method to replace the fragment in the container
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void logoutAdmin() {
        // Get the session SharedPreferences
        SharedPreferences sessionPrefs = getSharedPreferences(LoginActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);

        // Clear all data from this file
        SharedPreferences.Editor editor = sessionPrefs.edit();
        editor.clear();
        editor.apply();

        // Navigate back to LoginActivity
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}