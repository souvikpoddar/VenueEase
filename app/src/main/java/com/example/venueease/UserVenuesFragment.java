package com.example.venueease;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserVenuesFragment extends Fragment implements VenueUserAdapter.OnVenueUserActionListener, FilterVenuesFragment.FilterListener {

    // Database & RecyclerView
    private DatabaseHelper dbHelper;
    private RecyclerView rvUserVenues;
    private VenueUserAdapter venueUserAdapter;
    private List<Venue> venueList;

    // UI Views
    private TextView tvWelcomeUser, tvAvailableCount, tvUserEmptyView;
    private SearchView userSearchView;
    private ImageButton userBtnFilter;
    private ChipGroup userChipGroupLocations;

    // Filter State
    private String mCurrentQuery = "";
    private FilterCriteria mCurrentCriteria = null;

    private ActivityResultLauncher<Intent> bookVenueLauncher;

    // Required empty public constructor
    public UserVenuesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_venues, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the launcher in onCreate
        bookVenueLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Show confirmation Snackbar
                        if (getView() != null) { // Ensure fragment view is available
                            Snackbar snackbar = Snackbar.make(getView(), "Booking request submitted successfully!", Snackbar.LENGTH_LONG);
                            // Anchor it to the bottom nav bar
                            View anchorView = getActivity().findViewById(R.id.user_bottom_navigation);
                            if (anchorView != null) {
                                snackbar.setAnchorView(anchorView);
                            }
                            snackbar.setBackgroundTint(ContextCompat.getColor(getContext(), R.color.text_color_approved)); // Green color
                            snackbar.show();
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());

        // Find all views
        rvUserVenues = view.findViewById(R.id.rv_user_venues);
        tvWelcomeUser = view.findViewById(R.id.tv_welcome_user);
        tvAvailableCount = view.findViewById(R.id.tv_available_count);
        tvUserEmptyView = view.findViewById(R.id.tv_user_empty_view);
        userSearchView = view.findViewById(R.id.user_search_view);
        userBtnFilter = view.findViewById(R.id.user_btn_filter);
        userChipGroupLocations = view.findViewById(R.id.user_chip_group_locations);

        // Setup
        setWelcomeMessage();
        setupRecyclerView();
        setupListeners();
        loadVenuesFromDb();
    }

    private void setWelcomeMessage() {
        // Get user's name from SharedPreferences
        SharedPreferences sessionPrefs = getActivity().getSharedPreferences(LoginActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        String email = sessionPrefs.getString(LoginActivity.KEY_EMAIL, "User");

        SharedPreferences userAccountsPrefs = getActivity().getSharedPreferences(LoginActivity.USER_ACCOUNTS_PREFS, Context.MODE_PRIVATE);
        String fullName = userAccountsPrefs.getString(email + "_fullname", "User");

        // Set the welcome message
        tvWelcomeUser.setText(String.format(getString(R.string.user_welcome), fullName));
    }

    private void setupRecyclerView() {
        venueList = new ArrayList<>();
        venueUserAdapter = new VenueUserAdapter(getContext(), venueList, this);
        rvUserVenues.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUserVenues.setAdapter(venueUserAdapter);
    }

    private void setupListeners() {
        // Filter Button
        userBtnFilter.setOnClickListener(v -> {
            FilterVenuesFragment filterFragment = new FilterVenuesFragment();

            // Set 'this' (UserVenuesFragment) as the target to receive the results
            filterFragment.setTargetFragment(UserVenuesFragment.this, 0);

            filterFragment.show(getParentFragmentManager(), filterFragment.getTag());
        });

        // Search View
        userSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCurrentQuery = query;
                loadVenuesFromDb();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                boolean isFromChip = false;
                for (int i = 0; i < userChipGroupLocations.getChildCount(); i++) {
                    Chip chip = (Chip) userChipGroupLocations.getChildAt(i);
                    if (chip.getText().toString().equals(newText)) {
                        isFromChip = true;
                        break;
                    }
                }
                if (!isFromChip && !newText.isEmpty()) {
                    userChipGroupLocations.clearCheck();
                }
                mCurrentQuery = newText;
                loadVenuesFromDb();
                return true;
            }
        });

        // Location Chips
        userChipGroupLocations.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                userSearchView.setQuery("", true);
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    userSearchView.setQuery(selectedChip.getText().toString(), true);
                }
            }
        });
    }

    private void loadVenuesFromDb() {
        List<Venue> newVenues = dbHelper.getFilteredVenues(mCurrentQuery, mCurrentCriteria);
        venueUserAdapter.updateVenues(newVenues);

        // Update count
        tvAvailableCount.setText(String.format(Locale.getDefault(), getString(R.string.available_venues_count), newVenues.size()));

        // Handle empty view
        if (newVenues.isEmpty()) {
            rvUserVenues.setVisibility(View.GONE);
            tvUserEmptyView.setVisibility(View.VISIBLE);
        } else {
            rvUserVenues.setVisibility(View.VISIBLE);
            tvUserEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFiltersApplied(FilterCriteria criteria) {
        mCurrentCriteria = criteria; // Save the new criteria
        loadVenuesFromDb(); // Refresh the list with the new filters
    }

    @Override
    public void onViewDetailsClicked(Venue venue) {
        // Create an Intent to start VenueDetailsActivity
        Intent intent = new Intent(getContext(), VenueDetailsActivity.class);

        // Pass the selected Venue object to the activity
        intent.putExtra("VENUE_DATA", venue); // Venue class must implement Serializable

        // Start the activity
        startActivity(intent);
    }

    @Override
    public void onBookNowClicked(Venue venue) {
        // Create an Intent to start BookVenueActivity
        Intent intent = new Intent(getContext(), BookVenueActivity.class);

        // Pass the selected Venue object
        intent.putExtra("VENUE_TO_BOOK", venue); // Venue class must be Serializable

        // Start the activity
        bookVenueLauncher.launch(intent);
    }
}