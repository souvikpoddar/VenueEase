package com.example.venueease;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import android.widget.ImageButton; // Import this
import androidx.appcompat.widget.SearchView; // Import this
import com.google.android.material.chip.Chip; // Import this
import com.google.android.material.chip.ChipGroup; // Import this
import androidx.annotation.NonNull;
import java.util.List;
import android.widget.TextView;

// 1. Implement BOTH interfaces
public class AdminDashboardActivity extends AppCompatActivity
        implements AddVenueFragment.OnVenueDataChangedListener,
        VenueAdapter.OnVenueActionListener, FilterVenuesFragment.FilterListener {

    private MaterialButton btnAddVenue;
    private RecyclerView rvVenues;
    private VenueAdapter venueAdapter;
    private List<Venue> venueList;
    private SearchView searchView;
    private ImageButton btnFilter;
    private ChipGroup chipGroupLocations;
    private DatabaseHelper dbHelper;
    private TextView tvEmptyView;
    private String mCurrentQuery = "";
    private FilterCriteria mCurrentCriteria = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new DatabaseHelper(this);
        btnAddVenue = findViewById(R.id.btn_add_venue);
        rvVenues = findViewById(R.id.rv_venues);
        searchView = findViewById(R.id.search_view);
        btnFilter = findViewById(R.id.btn_filter);
        chipGroupLocations = findViewById(R.id.chip_group_locations);
        tvEmptyView = findViewById(R.id.tv_empty_view);

        setupRecyclerView();

        // --- Setup Listeners ---
        btnAddVenue.setOnClickListener(v -> {
            AddVenueFragment addVenueFragment = new AddVenueFragment();
            addVenueFragment.show(getSupportFragmentManager(), addVenueFragment.getTag());
        });

        // 2. Filter Button Listener
        btnFilter.setOnClickListener(v -> {
            FilterVenuesFragment filterFragment = new FilterVenuesFragment();
            filterFragment.show(getSupportFragmentManager(), filterFragment.getTag());
        });

        // 3. Search View Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCurrentQuery = query;
                loadVenuesFromDb();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    // If user starts typing, clear the chip selection
                    chipGroupLocations.clearCheck();
                }
                mCurrentQuery = newText;
                loadVenuesFromDb();
                return true;
            }
        });

        // 4. Add the new ChipGroup Listener
        chipGroupLocations.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if (checkedIds.isEmpty()) {
                    // No chip selected, do nothing
                    // (This might happen when searchView clears it)
                } else {
                    // Get the selected chip
                    int selectedChipId = checkedIds.get(0); // We're in singleSelection mode
                    Chip selectedChip = group.findViewById(selectedChipId);

                    if (selectedChip != null) {
                        // Set the search bar text to the chip's text and submit
                        String chipText = selectedChip.getText().toString();
                        searchView.setQuery(chipText, true); // true = submit the query
                    }
                }
            }
        });

        // Load initial data
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
        // Get the list from the database using current state
        List<Venue> newVenues = dbHelper.getFilteredVenues(mCurrentQuery, mCurrentCriteria);

        venueAdapter.updateVenues(newVenues);

        if (newVenues.isEmpty()) {
            // List is empty, show the empty view
            rvVenues.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);

            // Set context-aware text
            if (!mCurrentQuery.isEmpty() || mCurrentCriteria != null) {
                tvEmptyView.setText(R.string.empty_venues_admin_filtered);
            } else {
                tvEmptyView.setText(R.string.empty_venues_admin_default);
            }
        } else {
            // List has items, show the RecyclerView
            rvVenues.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * 5. Callback from FilterVenuesFragment
     */
    @Override
    public void onFiltersApplied(FilterCriteria criteria) {
        mCurrentCriteria = criteria; // Save the new criteria
        loadVenuesFromDb(); // Refresh the list
    }

    @Override
    public void onDataChanged() {
        Toast.makeText(this, "Dashboard refreshing...", Toast.LENGTH_SHORT).show();
        loadVenuesFromDb(); // Refresh list while keeping filters
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
        // 1. Build the confirmation dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Venue")
                .setMessage("Are you sure you want to delete \"" + venue.getName() + "\"?\nThis action cannot be undone.")
                .setCancelable(false) // User must choose an option

                // 2. The "Delete" button (Positive)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Call the database helper to delete
                        int rowsAffected = dbHelper.deleteVenue(venue.getId());

                        if (rowsAffected > 0) {
                            Toast.makeText(AdminDashboardActivity.this, "Venue deleted", Toast.LENGTH_SHORT).show();
                            // Refresh the list
                            loadVenuesFromDb();
                        } else {
                            Toast.makeText(AdminDashboardActivity.this, "Error deleting venue", Toast.LENGTH_SHORT).show();
                        }
                    }
                })

                // 3. The "Cancel" button (Negative)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Just close the dialog
                    }
                })

                // 4. Create the dialog
                .create();

        // 4. Show the dialog
        dialog.show();

        // 5. Get the button AFTER showing the dialog and set its color
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
    }
}