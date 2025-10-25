package com.example.venueease;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Import this
import androidx.recyclerview.widget.RecyclerView; // Import this

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList; // Import this
import java.util.List; // Import this

public class AdminDashboardActivity extends AppCompatActivity implements AddVenueFragment.VenueAddListener {

    private MaterialButton btnAddVenue;

    // 1. Declare RecyclerView, Adapter, List, and DB Helper
    private RecyclerView rvVenues;
    private VenueAdapter venueAdapter;
    private List<Venue> venueList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // 2. Initialize DB Helper
        dbHelper = new DatabaseHelper(this);

        // Find views
        btnAddVenue = findViewById(R.id.btn_add_venue);
        rvVenues = findViewById(R.id.rv_venues);

        // 3. Setup RecyclerView
        setupRecyclerView();

        // Set click listener for Add Venue button
        btnAddVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddVenueFragment addVenueFragment = new AddVenueFragment();
                addVenueFragment.show(getSupportFragmentManager(), addVenueFragment.getTag());
            }
        });

        // 4. Load the initial list of venues
        loadVenuesFromDb();
    }

    private void setupRecyclerView() {
        // Initialize the list
        venueList = new ArrayList<>();

        // Create the adapter
        venueAdapter = new VenueAdapter(this, venueList);

        // Set layout manager and adapter
        rvVenues.setLayoutManager(new LinearLayoutManager(this));
        rvVenues.setAdapter(venueAdapter);
    }

    /**
     * Fetches all venues from the database and updates the RecyclerView.
     */
    private void loadVenuesFromDb() {
        // 5. Get the list from the database
        List<Venue> newVenues = dbHelper.getAllVenuesList();

        // 6. Update the adapter's data
        venueAdapter.updateVenues(newVenues);

        // TODO: Add a check here to show/hide a "No venues found" message
    }

    /**
     * This is the callback method from AddVenueFragment.VenueAddListener
     * It's called after a new venue is successfully added to the DB.
     */
    @Override
    public void onVenueAdded() {
        Toast.makeText(this, "Dashboard refreshing...", Toast.LENGTH_SHORT).show();

        // 7. Reload the venues from the database
        loadVenuesFromDb();
    }
}