package com.example.venueease;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserBookingsFragment extends Fragment implements UserBookingsAdapter.OnPayNowClickListener {

    private DatabaseHelper dbHelper;
    private RecyclerView rvUserBookings;
    private UserBookingsAdapter userBookingsAdapter;
    private List<Booking> bookingList;

    private TextView tvBookingCountHeader;
    private TextView tvEmptyUserBookings;

    // We need the user's email to filter bookings
    private String currentUserEmail;

    // Required empty public constructor
    public UserBookingsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());

        // Get current user's email from session
        SharedPreferences sessionPrefs = getActivity().getSharedPreferences(LoginActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        currentUserEmail = sessionPrefs.getString(LoginActivity.KEY_EMAIL, null);

        // Find views
        rvUserBookings = view.findViewById(R.id.rv_user_bookings);
        tvBookingCountHeader = view.findViewById(R.id.tv_booking_count_header);
        tvEmptyUserBookings = view.findViewById(R.id.tv_empty_user_bookings);

        // Setup
        setupRecyclerView();
        loadUserBookings();
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        userBookingsAdapter = new UserBookingsAdapter(getContext(), bookingList, this); // 'this' is the listener
        rvUserBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUserBookings.setAdapter(userBookingsAdapter);
    }

    private void loadUserBookings() {
        if (currentUserEmail == null) {
            // Handle case where user email is not found (shouldn't happen if logged in)
            tvEmptyUserBookings.setText("Error: Could not find user information.");
            tvEmptyUserBookings.setVisibility(View.VISIBLE);
            rvUserBookings.setVisibility(View.GONE);
            tvBookingCountHeader.setText("0 bookings");
            return;
        }

        // TODO: Update DatabaseHelper to fetch bookings by user email
        // For now, we'll fetch ALL bookings as a placeholder
        List<Booking> userBookings = dbHelper.getBookings(null, null); // Fetch all bookings for now

        // --- TEMPORARY FILTERING (REMOVE WHEN DB IS UPDATED) ---
        List<Booking> filteredBookings = new ArrayList<>();
        for (Booking booking : userBookings) {
            if (currentUserEmail.equals(booking.getUserEmail())) {
                filteredBookings.add(booking);
            }
        }
        // --- END OF TEMPORARY FILTERING ---

        // Update adapter
        userBookingsAdapter.updateUserBookings(filteredBookings);

        // Update header count
        int count = filteredBookings.size();
        tvBookingCountHeader.setText(String.format(Locale.getDefault(), "%d booking%s", count, count == 1 ? "" : "s"));

        // Show/Hide empty view
        if (filteredBookings.isEmpty()) {
            rvUserBookings.setVisibility(View.GONE);
            tvEmptyUserBookings.setVisibility(View.VISIBLE);
            tvEmptyUserBookings.setText("You haven't made any bookings yet."); // Set default message
        } else {
            rvUserBookings.setVisibility(View.VISIBLE);
            tvEmptyUserBookings.setVisibility(View.GONE);
        }
    }

    /**
     * Callback for the "Pay Now" button click from the adapter.
     */
    @Override
    public void onPayNowClicked(Booking booking) {
        // TODO: Implement navigation to the Payment Screen
        Toast.makeText(getContext(), "Pay Now clicked for Booking #" + booking.getBookingId(), Toast.LENGTH_SHORT).show();
    }

    // --- IMPORTANT: Database Update Needed ---
    // You will need to modify `DatabaseHelper.getBookings()` to accept a userEmail
    // parameter and add a WHERE clause like:
    // `query += " AND b." + KEY_USER_EMAIL + " = ?";`
    // `selectionArgs.add(userEmail);`
    // Then remove the temporary filtering code in `loadUserBookings()`.
}