package com.example.venueease;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
        // --- TEMPORARY: Add test data ---
        dbHelper.addTestData();
        // --- REMOVE THIS LINE IN PRODUCTION ---

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
        // Fetch bookings from DB based on current filters
        List<Booking> newBookings = dbHelper.getBookings(mCurrentStatusFilter, mCurrentDateFilter);
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
        new DatePickerDialog(getContext(), (datePicker, year, month, day) -> {
            // Format date as "dd / mm / yyyy"
            String selectedDate = String.format(Locale.getDefault(), "%02d / %02d / %04d", day, month + 1, year);
            tvFilterDateValue.setText(selectedDate);
            mCurrentDateFilter = selectedDate; // Set the filter
            loadBookings();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // --- Adapter Action Callbacks ---

    @Override
    public void onApprove(Booking booking) {
        boolean success = dbHelper.updateBookingStatus(booking.getBookingId(), BookingsAdapter.STATUS_APPROVED);
        if (success) {
            Toast.makeText(getContext(), "Booking Approved", Toast.LENGTH_SHORT).show();
            // Refresh both the list and the summary cards
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
            Toast.makeText(getContext(), "Booking Rejected", Toast.LENGTH_SHORT).show();
            // Refresh both the list and the summary cards
            loadBookings();
            updateSummaryCards();
        } else {
            Toast.makeText(getContext(), "Failed to reject", Toast.LENGTH_SHORT).show();
        }
    }
}