package com.example.venueease;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

// 1. Implement BOTH interfaces
public class AdminDashboardActivity extends AppCompatActivity
        implements AddVenueFragment.OnVenueDataChangedListener,
        VenueAdapter.OnVenueActionListener {

    private MaterialButton btnAddVenue;
    private RecyclerView rvVenues;
    private VenueAdapter venueAdapter;
    private List<Venue> venueList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new DatabaseHelper(this);
        btnAddVenue = findViewById(R.id.btn_add_venue);
        rvVenues = findViewById(R.id.rv_venues);

        setupRecyclerView();

        // "Add Venue" button (opens fragment in "Add Mode")
        btnAddVenue.setOnClickListener(v -> {
            AddVenueFragment addVenueFragment = new AddVenueFragment();
            // No arguments means "Add Mode"
            addVenueFragment.show(getSupportFragmentManager(), addVenueFragment.getTag());
        });

        loadVenuesFromDb();
    }

    private void setupRecyclerView() {
        venueList = new ArrayList<>();
        // 2. Pass 'this' as the listener
        venueAdapter = new VenueAdapter(this, venueList, this);
        rvVenues.setLayoutManager(new LinearLayoutManager(this));
        rvVenues.setAdapter(venueAdapter);
    }

    private void loadVenuesFromDb() {
        List<Venue> newVenues = dbHelper.getAllVenuesList();
        venueAdapter.updateVenues(newVenues);
    }

    // 3. This is the callback from the AddVenueFragment
    @Override
    public void onDataChanged() {
        Toast.makeText(this, "Dashboard refreshing...", Toast.LENGTH_SHORT).show();
        loadVenuesFromDb();
    }

    // 4. This is the new callback from the VenueAdapter
    @Override
    public void onEditClicked(Venue venue) {
        // Open the fragment in "Edit Mode"
        AddVenueFragment editVenueFragment = new AddVenueFragment();

        // 5. Create a bundle and pass the venue data
        Bundle args = new Bundle();
        args.putSerializable("venue_to_edit", venue);
        editVenueFragment.setArguments(args);

        editVenueFragment.show(getSupportFragmentManager(), editVenueFragment.getTag());
    }

    // 6. This is also from the VenueAdapter (for later)
    @Override
    public void onDeleteClicked(Venue venue) {
        Toast.makeText(this, "Delete clicked for " + venue.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Show confirmation dialog, then call dbHelper.deleteVenue(venue.getId());
    }
}