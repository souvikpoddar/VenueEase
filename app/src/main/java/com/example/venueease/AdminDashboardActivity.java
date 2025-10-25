package com.example.venueease;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

// Make sure your class extends AppCompatActivity
public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialButton btnAddVenue;
    // ... other view declarations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Find the button
        btnAddVenue = findViewById(R.id.btn_add_venue);

        // Set the click listener to show the bottom sheet
        btnAddVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an instance of the fragment
                AddVenueFragment addVenueFragment = new AddVenueFragment();

                // Show the fragment
                addVenueFragment.show(getSupportFragmentManager(), addVenueFragment.getTag());
            }
        });

        // ... rest of your dashboard setup (RecyclerView, etc.)
    }
}