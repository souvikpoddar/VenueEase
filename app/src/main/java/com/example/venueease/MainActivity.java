package com.example.venueease;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Setup the Filter Icon Click Listener (Using modern Lambda syntax)
        ImageView filterIcon = findViewById(R.id.btn_filter_icon);
        filterIcon.setOnClickListener(v -> {
            // This code launches the VenueFilterBottomSheet Fragment
            VenueFilterBottomSheet filterBottomSheet = new VenueFilterBottomSheet();
            filterBottomSheet.show(getSupportFragmentManager(), filterBottomSheet.getTag());
        });

        // 2. Setup the Bottom Navigation Bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_venues) {
                // Testing: Launch details when 'Venues' is tapped
                // launchDetailsScreen();
                Toast.makeText(MainActivity.this, "Venues tab tapped", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_bookings) {
                Toast.makeText(MainActivity.this, "Bookings tab tapped", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_notifications) {
                Toast.makeText(MainActivity.this, "Notifications tab tapped", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(MainActivity.this, "Profile tab tapped", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_venues);

        // Optional testing line (currently commented out)
        // launchDetailsScreen();
    }

    // Function to launch the Venue Details Screen
    private void launchDetailsScreen() {
        Intent intent = new Intent(this, VenueDetailsActivity.class);
        startActivity(intent);
    }
}