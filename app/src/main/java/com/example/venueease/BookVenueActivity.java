package com.example.venueease;

import android.os.Bundle;
import android.view.MenuItem;
import android.app.DatePickerDialog;
import android.app.Activity;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookVenueActivity extends AppCompatActivity {

    private Venue venueToBook;

    // UI Elements
    private Toolbar toolbar;
    private TextView tvBookVenueName, tvBookVenueLocation, tvBookVenueCapacity, tvBookVenuePrice;
    private TextInputEditText etEventDate, etGuestCount, etSpecialRequests;
    private AutoCompleteTextView actStartTime, actEndTime, actEventType;
    private TextInputLayout tilGuestCount;
    private MaterialButton btnSubmitBooking;

    private DatabaseHelper dbHelper; // Add DB Helper
    private String selectedDbDate = null; // Variable to store DB-formatted date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_venue);

        dbHelper = new DatabaseHelper(this); // Initialize DB Helper

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar_book_venue);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Get Venue data
        venueToBook = (Venue) getIntent().getSerializableExtra("VENUE_TO_BOOK");

        // Find Views
        findViews();

        // Populate UI if venue exists
        if (venueToBook != null) {
            populateVenueInfo();
            setupDateTimePickers();
            setupTimeDropdowns();
            setupEventTypeDropdown();
        } else {
            Toast.makeText(this, "Error: Venue data missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Setup Submit Button Listener
        btnSubmitBooking.setOnClickListener(v -> {
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
        // Validation
        if (!validateBookingInput()) {
            return;
        }

        // Get Input Data
        String startTime = actStartTime.getText().toString();
        String endTime = actEndTime.getText().toString();
        String eventType = actEventType.getText().toString();
        int guestCount = Integer.parseInt(etGuestCount.getText().toString().trim());
        String specialRequests = etSpecialRequests.getText().toString().trim();

        // Get User Data (from Session SharedPreferences)
        SharedPreferences sessionPrefs = getSharedPreferences(LoginActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sessionPrefs.getString(LoginActivity.KEY_EMAIL, "unknown@example.com");

        SharedPreferences userAccountsPrefs = getSharedPreferences(LoginActivity.USER_ACCOUNTS_PREFS, Context.MODE_PRIVATE);
        String userName = userAccountsPrefs.getString(userEmail + "_fullname", "Unknown User");
        int userId = 0;

        // Calculate Price
        double totalPrice = calculateTotalPrice(startTime, endTime, venueToBook.getPrice());
        if (totalPrice < 0) {
            Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_LONG).show();
            return;
        }

        // Get Current Date for 'submitted_date'
        SimpleDateFormat submittedDateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        String submittedDate = submittedDateFormat.format(new Date());

        // Save to Database
        boolean success = dbHelper.addBooking(
                venueToBook.getId(), userId, userName, userEmail,
                selectedDbDate, startTime, endTime, eventType,
                totalPrice, specialRequests, submittedDate
        );

        // Handle Result
        if (success) {
            // ADD NOTIFICATION FOR ADMIN
            String adminNotifTitle = "New Booking Request \uD83D\uDCEF️"; // Bell emoji
            String adminNotifMsg = String.format(Locale.getDefault(),
                    "%s has requested to book %s for %s on %s from %s to %s. Total amount: ₹%.0f. Please review and approve/reject this request.",
                    userName,
                    venueToBook.getName(),
                    eventType,
                    selectedDbDate, // Use the DB formatted date
                    startTime,
                    endTime,
                    totalPrice);
            dbHelper.addNotification("admin", "NEW_BOOKING", adminNotifTitle, adminNotifMsg, 0 /*newBookingId*/, venueToBook.getId());

            Toast.makeText(this, "Booking request submitted successfully!", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to submit booking request.", Toast.LENGTH_LONG).show();
        }
    }

    // Validates all the booking form fields.
    private boolean validateBookingInput() {
        // Reset errors
        etEventDate.setError(null);
        actStartTime.setError(null);
        actEndTime.setError(null);
        actEventType.setError(null);
        tilGuestCount.setError(null);

        // Get values
        String date = etEventDate.getText().toString();
        String startTime = actStartTime.getText().toString();
        String endTime = actEndTime.getText().toString();
        String eventType = actEventType.getText().toString();
        String guestStr = etGuestCount.getText().toString().trim();

        // Check empty fields
        if (TextUtils.isEmpty(date) || selectedDbDate == null) {
            etEventDate.setError("Event Date is required");
            etEventDate.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(startTime)) {
            actStartTime.setError("Start Time is required");
            actStartTime.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(endTime)) {
            actEndTime.setError("End Time is required");
            actEndTime.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(eventType)) {
            actEventType.setError("Event Type is required");
            actEventType.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(guestStr)) {
            tilGuestCount.setError("Number of Guests is required");
            etGuestCount.requestFocus();
            return false;
        }

        // Check guest count validity
        try {
            int guests = Integer.parseInt(guestStr);
            if (guests <= 0) {
                tilGuestCount.setError("Guest count must be positive");
                etGuestCount.requestFocus();
                return false;
            }
            if (guests > venueToBook.getCapacity()) {
                tilGuestCount.setError("Guest count exceeds venue capacity (" + venueToBook.getCapacity() + ")");
                etGuestCount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            tilGuestCount.setError("Invalid number");
            etGuestCount.requestFocus();
            return false;
        }
        return true;
    }

    // Calculates the total price based on duration and hourly rate
    private double calculateTotalPrice(String startTime, String endTime, double hourlyRate) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);

            // Calculate duration in milliseconds
            long durationMillis = end.getTime() - start.getTime();

            if (durationMillis <= 0) {
                return -1; // End time is not after start time
            }

            // Convert duration to hours (as a double)
            double durationHours = (double) durationMillis / TimeUnit.HOURS.toMillis(1);

            // Calculate total price
            return durationHours * hourlyRate;

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
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
        // Date Picker
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

                        // Format for display
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());
                        String displayDate = displayFormat.format(selectedDateCal.getTime());

                        // Format for database
                        SimpleDateFormat dbFormat = new SimpleDateFormat("E, MMM d, yyyy", Locale.getDefault());
                        String dbDate = dbFormat.format(selectedDateCal.getTime());

                        etEventDate.setText(displayDate);
                        this.selectedDbDate = dbDate;

                    }, year, month, day);

            // Prevent picking past dates
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

    }

    private void setupTimeDropdowns() {
        // Define time slots
        List<String> timeSlots = new ArrayList<>();
        for (int hour = 8; hour <= 22; hour++) {
            timeSlots.add(String.format(Locale.getDefault(), "%02d:00", hour));
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