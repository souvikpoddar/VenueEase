package com.example.venueease; // CHECK PACKAGE NAME!

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class BookingFormActivity extends AppCompatActivity {

    private TextView textEventDate;
    private TextView textStartTime;
    private TextView textEndTime;
    private TextView textEventType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.book_venue_title));
        }

        // 1. Find the TextViews (where the user taps)
        textEventDate = findViewById(R.id.text_event_date);
        textStartTime = findViewById(R.id.text_start_time);
        textEndTime = findViewById(R.id.text_end_time);
        textEventType = findViewById(R.id.text_venue_type_value); // Assuming you add this ID to Event Type field

        // 2. Attach Click Listeners to launch the Pickers
        textEventDate.setOnClickListener(v -> showDatePicker());
        textStartTime.setOnClickListener(v -> showTimePicker(textStartTime));
        textEndTime.setOnClickListener(v -> showTimePicker(textEndTime));

        // 3. For Event Type, we'll simulate a dropdown/list selection using a Toast for simplicity
        textEventType.setOnClickListener(v -> showEventTypePicker());

        // 4. Submit Button Placeholder
        findViewById(R.id.btn_submit_request).setOnClickListener(v -> {
            Toast.makeText(this, "Submitting Booking Request...", Toast.LENGTH_SHORT).show();
            // In a real app, validation and network request would go here.
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // --- Picker/Dropdown Methods ---

    // A. Shows the Calendar picker
    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    // Update the field with the selected date (dd-mm-yyyy format)
                    textEventDate.setText(String.format("%02d-%02d-%d", d, m + 1, y));
                }, year, month, day);
        datePickerDialog.show();
    }

    // B. Shows the Time picker
    private void showTimePicker(TextView timeField) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, h, m) -> {
                    // Update the field with the selected time (HH:MM format)
                    timeField.setText(String.format("%02d:%02d", h, m));
                }, hour, minute, false); // false = 12-hour format, true = 24-hour format
        timePickerDialog.show();
    }

    // C. Simulates the Event Type Dropdown (for UI demonstration)
    private void showEventTypePicker() {
        // Since showing a long, custom list is complex, we use a simple action
        // In a real app, this would open a DialogFragment or Custom Bottom Sheet
        Toast.makeText(this, "Event Type List Would Open Here!", Toast.LENGTH_LONG).show();
        textEventType.setText("Corporate Meeting"); // Set a demo value
    }
}