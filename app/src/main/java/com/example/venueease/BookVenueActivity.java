package com.example.venueease;

import android.os.Bundle;
import android.view.MenuItem;
import android.app.DatePickerDialog;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookVenueActivity extends AppCompatActivity {

    private Venue venueToBook;

    // UI Elements
    private Toolbar toolbar;
    private TextView tvBookVenueName, tvBookVenueLocation, tvBookVenueCapacity, tvBookVenuePrice;
    private TextInputEditText etEventDate, etGuestCount, etSpecialRequests;
    private AutoCompleteTextView actStartTime, actEndTime, actEventType;
    private TextInputLayout tilGuestCount;
    private MaterialButton btnSubmitBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_venue);

        // 1. Setup Toolbar
        toolbar = findViewById(R.id.toolbar_book_venue);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 2. Get Venue data
        venueToBook = (Venue) getIntent().getSerializableExtra("VENUE_TO_BOOK");

        // 3. Find Views
        findViews();

        // 4. Populate UI if venue exists
        if (venueToBook != null) {
            populateVenueInfo();
            // TODO: Setup dropdowns and pickers
            setupDateTimePickers();
            setupTimeDropdowns();
            setupEventTypeDropdown();
        } else {
            Toast.makeText(this, "Error: Venue data missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 5. Setup Submit Button Listener
        btnSubmitBooking.setOnClickListener(v -> {
            // TODO: Implement validation and booking submission
            handleSubmitBooking();
        });
    }

    private void findViews() {
        tvBookVenueName = findViewById(R.id.tv_book_venue_name);
        tvBookVenueLocation = findViewById(R.id.tv_book_venue_location);
        tvBookVenueCapacity = findViewById(R.id.tv_book_venue_capacity);
        tvBookVenuePrice = findViewById(R.id.tv_book_venue_price);
        etEventDate = findViewById(R.id.et_event_date);
        actStartTime = findViewById(R.id.act_start_time);
        actEndTime = findViewById(R.id.act_end_time);
        actEventType = findViewById(R.id.act_event_type);
        tilGuestCount = findViewById(R.id.til_guest_count);
        etGuestCount = findViewById(R.id.et_guest_count);
        etSpecialRequests = findViewById(R.id.et_special_requests);
        btnSubmitBooking = findViewById(R.id.btn_submit_booking);
    }

    private void populateVenueInfo() {
        tvBookVenueName.setText(venueToBook.getName());
        tvBookVenueLocation.setText(venueToBook.getLocation());
        tvBookVenueCapacity.setText(String.format(Locale.getDefault(), "Up to %d guests", venueToBook.getCapacity()));
        tvBookVenuePrice.setText(String.format(Locale.getDefault(), "₹%.0f/hour", venueToBook.getPrice()));
        // Set the helper text for guest count dynamically
        tilGuestCount.setHelperText("Maximum capacity: " + venueToBook.getCapacity() + " guests");
    }

    private void handleSubmitBooking() {
        // TODO: Validate all fields
        // TODO: Get user info (from SharedPreferences)
        // TODO: Create Booking object
        // TODO: Save to database using DatabaseHelper
        // TODO: Show confirmation and finish
        Toast.makeText(this, "Submit Booking Clicked", Toast.LENGTH_SHORT).show();
    }

    // Handle Toolbar back arrow
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDateTimePickers() {
        // --- Date Picker ---
        etEventDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    BookVenueActivity.this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        // Set calendar to selected date to format it
                        Calendar selectedDateCal = Calendar.getInstance();
                        selectedDateCal.set(selectedYear, selectedMonth, selectedDayOfMonth);

                        // Format for display (e.g., "26 / 10 / 2025")
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());
                        String displayDate = displayFormat.format(selectedDateCal.getTime());

                        // Format for database (e.g., "Sun, Oct 26, 2025") - MUST match BookingsFragment
                        SimpleDateFormat dbFormat = new SimpleDateFormat("E, MMM d, yyyy", Locale.getDefault());
                        String dbDate = dbFormat.format(selectedDateCal.getTime());

                        etEventDate.setText(displayDate);
                        // You might want to store the dbDate in a member variable
                        // to use when submitting the booking
                        // this.selectedDbDate = dbDate;

                    }, year, month, day);

            // Prevent picking past dates
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

    }

    private void setupTimeDropdowns() {
        // Define time slots (e.g., hourly from 8 AM to 10 PM)
        List<String> timeSlots = new ArrayList<>();
        for (int hour = 8; hour <= 22; hour++) {
            timeSlots.add(String.format(Locale.getDefault(), "%02d:00", hour));
            // Optional: Add half-hour slots if needed
            // if (hour < 22) {
            //     timeSlots.add(String.format(Locale.getDefault(), "%02d:30", hour));
            // }
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, timeSlots);

        actStartTime.setAdapter(timeAdapter);
        actEndTime.setAdapter(timeAdapter);
    }

    private void setupEventTypeDropdown() {
        // Define event types
        String[] eventTypes = new String[] {
                "Meeting",
                "Conference",
                "Seminar",
                "Workshop",
                "Wedding Reception",
                "Birthday Party",
                "Corporate Event",
                "Other"
        };

        ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, eventTypes);

        actEventType.setAdapter(eventAdapter);
    }
}