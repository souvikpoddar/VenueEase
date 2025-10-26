package com.example.venueease;

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

    // Define our fragments
    private VenuesFragment venuesFragment;
    private BookingsFragment bookingsFragment;
    // We can add these later
    // private NotificationsFragment notificationsFragment;
    // private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize our fragments
        venuesFragment = new VenuesFragment();
        bookingsFragment = new BookingsFragment(); // We'll create this class next

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
                    // selectedFragment = notificationsFragment; // Coming soon
                    Toast.makeText(AdminDashboardActivity.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
                    return false; // Return false to not select the item yet
                } else if (itemId == R.id.nav_profile) {
                    // selectedFragment = profileFragment; // Coming soon
                    Toast.makeText(AdminDashboardActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                    return false; // Return false to not select the item yet
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
}