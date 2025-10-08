package com.example.venueease; // <-- CHECK THIS LINE!

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class VenueDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CRITICAL FIX: Removed complex edge-to-edge code that caused the 'main' error.
        // We now just load the layout directly.
        setContentView(R.layout.activity_venue_details);

        // Note: The toolbar functionality (back button) is handled by the XML layout.
    }
}