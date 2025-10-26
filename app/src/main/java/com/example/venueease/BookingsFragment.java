package com.example.venueease;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class BookingsFragment extends Fragment implements BookingsAdapter.OnBookingActionListener {

    private DatabaseHelper dbHelper;
    private RecyclerView rvBookings;
    private BookingsAdapter bookingsAdapter;
    private List<Booking> bookingList;

    // UI Elements
    private TextView tvTotalRequests, tvPendingRequests, tvApprovedRequests, tvConfirmedRequests;
    private TextView tvEmptyBookings, tvFilterDateValue;
    private ChipGroup chipGroupStatus;
    private RelativeLayout rlFilterDate;

    // Filter State
    private String mCurrentStatusFilter = BookingsAdapter.STATUS_PENDING;
    private String mCurrentDateFilter = null;
    private String currentUserEmail;

    // Required empty public constructor
    public BookingsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());


        SharedPreferences sessionPrefs = getActivity().getSharedPreferences(LoginActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        currentUserEmail = sessionPrefs.getString(LoginActivity.KEY_EMAIL, null);

        // Find all views
        rvBookings = view.findViewById(R.id.rv_bookings);
        tvTotalRequests = view.findViewById(R.id.tv_total_requests);
        tvPendingRequests = view.findViewById(R.id.tv_pending_requests);
        tvApprovedRequests = view.findViewById(R.id.tv_approved_requests);
        tvConfirmedRequests = view.findViewById(R.id.tv_confirmed_requests);
        tvEmptyBookings = view.findViewById(R.id.tv_empty_bookings);
        tvFilterDateValue = view.findViewById(R.id.tv_filter_date_value);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        rlFilterDate = view.findViewById(R.id.rl_filter_date);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup Listeners
        setupListeners();

        // Load initial data
        updateSummaryCards();
        loadBookings();
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        bookingsAdapter = new BookingsAdapter(getContext(), bookingList, this);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBookings.setAdapter(bookingsAdapter);
    }

    private void setupListeners() {
        // Chip group for filtering by status
        chipGroupStatus.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                // The checkedIds parameter is a List, not an int.
                // Since we are in singleSelection mode, we just need the first item.

                if (checkedIds.isEmpty()) {
                    // This shouldn't happen if selectionRequired=true, but good to check.
                    return;
                }

                // 1. Get the single selected ID from the list
                int selectedChipId = checkedIds.get(0);

                // 2. Now, compare the int ID
                if (selectedChipId == R.id.chip_pending) {
                    mCurrentStatusFilter = BookingsAdapter.STATUS_PENDING;
                } else if (selectedChipId == R.id.chip_approved) {
                    mCurrentStatusFilter = BookingsAdapter.STATUS_APPROVED;
                } else if (selectedChipId == R.id.chip_confirmed) {
                    mCurrentStatusFilter = BookingsAdapter.STATUS_CONFIRMED;
                } else if (selectedChipId == R.id.chip_rejected) {
                    mCurrentStatusFilter = BookingsAdapter.STATUS_REJECTED;
                }

                // Refresh the list with the new filter
                loadBookings();
            }
        });

        // Date filter
        rlFilterDate.setOnClickListener(v -> showDatePicker());
    }

    private void loadBookings() {
        if (currentUserEmail == null) {
            tvEmptyBookings.setText("Error: Could not find user information.");
            return;
        }
        // Fetch bookings from DB based on current filters
        List<Booking> newBookings = dbHelper.getBookings(null, mCurrentStatusFilter, mCurrentDateFilter);
        bookingsAdapter.updateBookings(newBookings);

        // Show/Hide empty view
        if (newBookings.isEmpty()) {
            rvBookings.setVisibility(View.GONE);
            tvEmptyBookings.setVisibility(View.VISIBLE);
        } else {
            rvBookings.setVisibility(View.VISIBLE);
            tvEmptyBookings.setVisibility(View.GONE);
        }
    }

    private void updateSummaryCards() {
        // Get counts from DB
        int total = dbHelper.getBookingCount(null);
        int pending = dbHelper.getBookingCount(BookingsAdapter.STATUS_PENDING);
        int approved = dbHelper.getBookingCount(BookingsAdapter.STATUS_APPROVED);
        int confirmed = dbHelper.getBookingCount(BookingsAdapter.STATUS_CONFIRMED);
        int rejected = dbHelper.getBookingCount(BookingsAdapter.STATUS_REJECTED);

        // Update TextViews
        tvTotalRequests.setText(String.valueOf(total));
        tvPendingRequests.setText(String.valueOf(pending));
        tvApprovedRequests.setText(String.valueOf(approved));
        tvConfirmedRequests.setText(String.valueOf(confirmed));

        // Update Chip text to include counts
        ((Chip) chipGroupStatus.findViewById(R.id.chip_pending)).setText(String.format(Locale.getDefault(), "Pending (%d)", pending));
        ((Chip) chipGroupStatus.findViewById(R.id.chip_approved)).setText(String.format(Locale.getDefault(), "Approved (%d)", approved));
        ((Chip) chipGroupStatus.findViewById(R.id.chip_confirmed)).setText(String.format(Locale.getDefault(), "Confirmed (%d)", confirmed));
        ((Chip) chipGroupStatus.findViewById(R.id.chip_rejected)).setText(String.format(Locale.getDefault(), "Rejected (%d)", rejected));
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (datePicker, y, m, d) -> {
                    // --- THIS IS THE UPDATED LOGIC ---

                    // 1. Create a Calendar object for the selected date
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(y, m, d);

                    // 2. Format the date for the UI ("26 / 10 / 2025")
                    String uiDateString = String.format(Locale.getDefault(), "%02d / %02d / %04d", d, m + 1, y);

                    // 3. Format the date for the DB query ("Sun, Oct 26, 2025")
                    // This MUST match the format in your addTestData() method
                    SimpleDateFormat dbFormat = new SimpleDateFormat("E, MMM d, yyyy", Locale.getDefault());
                    String dbDateString = dbFormat.format(selectedCalendar.getTime());

                    // 4. Set the UI text and the filter value
                    tvFilterDateValue.setText(uiDateString);
                    tvFilterDateValue.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

                    mCurrentDateFilter = dbDateString; // Set the filter to the DB format

                    loadBookings(); // Refresh the list

                    // --- END OF UPDATED LOGIC ---
                },
                year, month, day
        );

        // Add the "Clear Filter" button
        datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Clear Filter", (dialog, which) -> {
            clearDateFilter();
        });

        // Add the "Cancel" button
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        datePickerDialog.show();
    }

    /**
     * Clears the date filter and reloads the bookings.
     */
    private void clearDateFilter() {
        mCurrentDateFilter = null; // Clear the filter state
        tvFilterDateValue.setText("dd / mm / yyyy"); // Reset the label
        tvFilterDateValue.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        loadBookings(); // Refresh the list
    }

    // --- Adapter Action Callbacks ---

    @Override
    public void onApprove(Booking booking) {
        boolean success = dbHelper.updateBookingStatus(booking.getBookingId(), BookingsAdapter.STATUS_APPROVED);
        if (success) {
            // --- ADD NOTIFICATION FOR USER ---
            String userNotifTitle = "Booking Approved! ✅";
            String userNotifMsg = String.format(Locale.getDefault(),
                    "Great news! Your booking for %s on %s has been approved by the admin. You can now proceed with online payment to confirm your booking. Multiple payment options are available including UPI, Credit/Debit Card, and Net Banking.",
                    booking.getVenue().getName(), // Assuming venue details are loaded
                    booking.getEventDate());
            dbHelper.addNotification(booking.getUserEmail(), "BOOKING_APPROVED", userNotifTitle, userNotifMsg, booking.getBookingId(), booking.getVenueId());
            // --- END NOTIFICATION ---
            // --- New Snackbar Logic ---

            // 1. Get the BottomNavigationView from the activity to use as an anchor
            View anchorView = getActivity().findViewById(R.id.bottom_navigation);

            // 2. Create the dynamic message
            String message = String.format("%s's booking for %s has been approved.",
                    booking.getUserName(),
                    booking.getVenue().getName());

            // 3. Create and customize the Snackbar
            Snackbar snackbar = Snackbar.make(anchorView, message, Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(anchorView); // This makes it appear ABOVE the nav bar

            // Set the green "Approved" color
            snackbar.setBackgroundTint(ContextCompat.getColor(getContext(), R.color.text_color_approved));
            snackbar.show();

            // --- End of New Logic ---

            // Refresh the UI
            loadBookings();
            updateSummaryCards();
        } else {
            Toast.makeText(getContext(), "Failed to approve", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReject(Booking booking) {
        boolean success = dbHelper.updateBookingStatus(booking.getBookingId(), BookingsAdapter.STATUS_REJECTED);
        if (success) {
            // --- ADD NOTIFICATION FOR USER ---
            String userNotifTitle = "Booking Rejected ❌";
            String userNotifMsg = String.format(Locale.getDefault(),
                    "Unfortunately, your booking request for %s on %s has been rejected by the admin. Please contact support or try booking another venue/date.",
                    booking.getVenue().getName(),
                    booking.getEventDate());
            dbHelper.addNotification(booking.getUserEmail(), "BOOKING_REJECTED", userNotifTitle, userNotifMsg, booking.getBookingId(), booking.getVenueId());
            // --- END NOTIFICATION ---
            // --- New Snackbar Logic ---

            // 1. Get the anchor view
            View anchorView = getActivity().findViewById(R.id.bottom_navigation);

            // 2. Create the dynamic message
            String message = String.format("%s's booking request for %s has been rejected.",
                    booking.getUserName(),
                    booking.getVenue().getName());

            // 3. Create and customize the Snackbar
            Snackbar snackbar = Snackbar.make(anchorView, message, Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(anchorView);

            // We'll use the default dark color for "Rejected" as shown in your screenshot
            // (If you wanted it red, you could add:
            // snackbar.setBackgroundTint(ContextCompat.getColor(getContext(), R.color.text_color_rejected));

            snackbar.show();

            // --- End of New Logic ---

            // Refresh the UI
            loadBookings();
            updateSummaryCards();
        } else {
            Toast.makeText(getContext(), "Failed to reject", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the data whenever the fragment becomes visible
        updateSummaryCards();
        loadBookings();
    }
}