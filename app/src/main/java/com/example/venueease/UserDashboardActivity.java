package com.example.venueease;

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

public class UserDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    // Define our user fragments
    private UserVenuesFragment userVenuesFragment;
    // We can add these later
    // private UserBookingsFragment userBookingsFragment;
    // private UserNotificationsFragment userNotificationsFragment;
    // private UserProfileFragment userProfileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize our fragments
        userVenuesFragment = new UserVenuesFragment();
        // userBookingsFragment = new UserBookingsFragment(); // (coming soon)

        bottomNavigationView = findViewById(R.id.user_bottom_navigation);

        // Load the default fragment (Venues)
        if (savedInstanceState == null) {
            loadFragment(userVenuesFragment);
        }

        // Set the listener for the bottom navigation
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.user_nav_venues) {
                    selectedFragment = userVenuesFragment;
                } else if (itemId == R.id.user_nav_bookings) {
                    // selectedFragment = userBookingsFragment; // (coming soon)
                    Toast.makeText(UserDashboardActivity.this, "My Bookings clicked", Toast.LENGTH_SHORT).show();
                    return false; // Return false to not select the item yet
                } else if (itemId == R.id.user_nav_notifications) {
                    // selectedFragment = userNotificationsFragment; // (coming soon)
                    Toast.makeText(UserDashboardActivity.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (itemId == R.id.user_nav_profile) {
                    // selectedFragment = userProfileFragment; // (coming soon)
                    Toast.makeText(UserDashboardActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                    return false;
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
        transaction.replace(R.id.user_fragment_container, fragment);
        transaction.commit();
    }
}