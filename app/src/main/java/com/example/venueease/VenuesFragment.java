package com.example.venueease;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

// 1. This Fragment now implements all the listeners
public class VenuesFragment extends Fragment
        implements AddVenueFragment.OnVenueDataChangedListener,
        VenueAdapter.OnVenueActionListener,
        FilterVenuesFragment.FilterListener {

    // 2. All views and variables are moved here
    private RecyclerView rvVenues;
    private VenueAdapter venueAdapter;
    private List<Venue> venueList;
    private DatabaseHelper dbHelper;

    private MaterialButton btnAddVenue;
    private SearchView searchView;
    private ImageButton btnFilter;
    private ChipGroup chipGroupLocations;
    private TextView tvEmptyView;

    private String mCurrentQuery = "";
    private FilterCriteria mCurrentCriteria = null;

    // Required empty public constructor
    public VenuesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_venues, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 3. Initialize DB Helper (using getContext())
        dbHelper = new DatabaseHelper(getContext());

        // 4. Find all views using the fragment's 'view'
        rvVenues = view.findViewById(R.id.rv_venues);
        btnAddVenue = view.findViewById(R.id.btn_add_venue);
        searchView = view.findViewById(R.id.search_view);
        btnFilter = view.findViewById(R.id.btn_filter);
        chipGroupLocations = view.findViewById(R.id.chip_group_locations);
        tvEmptyView = view.findViewById(R.id.tv_empty_view);

        // 5. Setup all logic
        setupRecyclerView();
        setupListeners();
        loadVenuesFromDb();
    }

    private void setupRecyclerView() {
        venueList = new ArrayList<>();
        // 'this' refers to the Fragment itself, which implements the listener
        venueAdapter = new VenueAdapter(getContext(), venueList, this);
        rvVenues.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVenues.setAdapter(venueAdapter);
    }

    private void setupListeners() {
        // Add Venue Button
        btnAddVenue.setOnClickListener(v -> {
            AddVenueFragment addVenueFragment = new AddVenueFragment();

            // Tell the dialog that 'this' (VenuesFragment) is its target/listener
            addVenueFragment.setTargetFragment(VenuesFragment.this, 0);

            addVenueFragment.show(getParentFragmentManager(), addVenueFragment.getTag());
        });

        // Filter Button
        btnFilter.setOnClickListener(v -> {
            FilterVenuesFragment filterFragment = new FilterVenuesFragment();
            // Use getParentFragmentManager()
            filterFragment.show(getParentFragmentManager(), filterFragment.getTag());
        });

        // Search View
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
                    chipGroupLocations.clearCheck();
                }
                mCurrentQuery = newText;
                loadVenuesFromDb();
                return true;
            }
        });

        // Location Chips
        chipGroupLocations.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                searchView.setQuery("", true);
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    searchView.setQuery(selectedChip.getText().toString(), true);
                }
            }
        });
    }

    private void loadVenuesFromDb() {
        List<Venue> newVenues = dbHelper.getFilteredVenues(mCurrentQuery, mCurrentCriteria);
        venueAdapter.updateVenues(newVenues);

        if (newVenues.isEmpty()) {
            rvVenues.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
            if (!mCurrentQuery.isEmpty() || mCurrentCriteria != null) {
                tvEmptyView.setText(R.string.empty_venues_admin_filtered);
            } else {
                tvEmptyView.setText(R.string.empty_venues_admin_default);
            }
        } else {
            rvVenues.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }
    }

    // --- 6. All Listener Callback Methods are now part of the Fragment ---

    @Override
    public void onFiltersApplied(FilterCriteria criteria) {
        mCurrentCriteria = criteria;
        loadVenuesFromDb();
    }

    @Override
    public void onDataChanged() {
        Toast.makeText(getContext(), "Dashboard refreshing...", Toast.LENGTH_SHORT).show();
        loadVenuesFromDb();
    }

    @Override
    public void onEditClicked(Venue venue) {
        AddVenueFragment editVenueFragment = new AddVenueFragment();
        Bundle args = new Bundle();
        args.putSerializable("venue_to_edit", venue);
        editVenueFragment.setArguments(args);

        editVenueFragment.setTargetFragment(VenuesFragment.this, 0);

        editVenueFragment.show(getParentFragmentManager(), editVenueFragment.getTag());
    }

    @Override
    public void onDeleteClicked(Venue venue) {
        // Use getContext() for the AlertDialog Builder
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Delete Venue")
                .setMessage("Are you sure you want to delete \"" + venue.getName() + "\"?\nThis action cannot be undone.")
                .setCancelable(false)
                .setPositiveButton("Delete", (d, which) -> {
                    int rowsAffected = dbHelper.deleteVenue(venue.getId());
                    if (rowsAffected > 0) {
                        Toast.makeText(getContext(), "Venue deleted", Toast.LENGTH_SHORT).show();
                        loadVenuesFromDb();
                    } else {
                        Toast.makeText(getContext(), "Error deleting venue", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
    }
}