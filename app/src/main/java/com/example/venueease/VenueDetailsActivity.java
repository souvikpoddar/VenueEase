package com.example.venueease;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class VenueDetailsActivity extends AppCompatActivity {

    private Venue currentVenue;

    // Declare UI elements
    private Toolbar toolbar;
    private ImageView ivDetailVenueImage;
    private TextView tvDetailVenueName, tvDetailVenueLocation, tvDetailTagType,
            tvDetailVenueCapacity, tvDetailVenuePrice, tvDetailDescription,
            tvRatingValue, tvRatingCount; // (Rating is currently dummy)
    private LinearLayout llDetailAmenitiesContainer;
    private MaterialButton btnBookThisVenue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_details);

        // 1. Setup Toolbar
        toolbar = findViewById(R.id.toolbar_venue_details);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 2. Get the Venue object from the Intent
        currentVenue = (Venue) getIntent().getSerializableExtra("VENUE_DATA");

        // 3. Find Views
        findViews();

        // 4. Populate UI if venue data exists
        if (currentVenue != null) {
            populateUi();
        } else {
            // Handle error: Venue data not received
            Toast.makeText(this, "Error: Venue details not found.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if data is missing
        }

        // 5. Setup Book Button Listener
        btnBookThisVenue.setOnClickListener(v -> {
            // TODO: Navigate to the Booking Screen, passing the currentVenue
            Toast.makeText(this, "Book This Venue clicked for " + currentVenue.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void findViews() {
        ivDetailVenueImage = findViewById(R.id.iv_detail_venue_image);
        tvDetailVenueName = findViewById(R.id.tv_detail_venue_name);
        tvDetailVenueLocation = findViewById(R.id.tv_detail_venue_location);
        tvDetailTagType = findViewById(R.id.tv_detail_tag_type);
        tvDetailVenueCapacity = findViewById(R.id.tv_detail_venue_capacity);
        tvDetailVenuePrice = findViewById(R.id.tv_detail_venue_price);
        tvDetailDescription = findViewById(R.id.tv_detail_description);
        llDetailAmenitiesContainer = findViewById(R.id.ll_detail_amenities_container);
        tvRatingValue = findViewById(R.id.tv_rating_value); // Dummy
        tvRatingCount = findViewById(R.id.tv_rating_count); // Dummy
        btnBookThisVenue = findViewById(R.id.btn_book_this_venue);
    }

    private void populateUi() {
        // Set basic details
        getSupportActionBar().setTitle(currentVenue.getName()); // Set toolbar title too
        tvDetailVenueName.setText(currentVenue.getName());
        tvDetailVenueLocation.setText(currentVenue.getLocation());
        tvDetailTagType.setText(currentVenue.getType());
        tvDetailVenueCapacity.setText(String.format(Locale.getDefault(), "Up to %d guests", currentVenue.getCapacity()));
        tvDetailVenuePrice.setText(String.format(Locale.getDefault(), "%.0f", currentVenue.getPrice()));
        tvDetailDescription.setText(currentVenue.getDescription());

        // Load image
        String photoUriString = currentVenue.getPhotoUri();
        if (photoUriString != null && !photoUriString.isEmpty()) {
            ivDetailVenueImage.setImageURI(Uri.parse(photoUriString));
        } else {
            ivDetailVenueImage.setImageResource(android.R.drawable.ic_menu_gallery); // Placeholder
        }

        // Add amenities dynamically
        addAmenitiesToList(currentVenue.getAmenities());

        // TODO: Populate Reviews (currently uses dummy text from XML)
        // tvRatingValue.setText("4.8");
        // tvRatingCount.setText("Based on 24 reviews");
    }

    private void addAmenitiesToList(String amenitiesString) {
        llDetailAmenitiesContainer.removeAllViews(); // Clear previous views if any

        if (amenitiesString == null || amenitiesString.isEmpty()) {
            // Optionally add a "No amenities listed" TextView
            return;
        }

        String[] amenities = amenitiesString.split(",");
        for (String amenity : amenities) {
            String trimmedAmenity = amenity.trim();
            if (!trimmedAmenity.isEmpty()) {
                TextView amenityView = new TextView(this);
                amenityView.setText(trimmedAmenity);
                amenityView.setTextSize(16); // Set text size (e.g., 16sp)

                // Set the green check drawable
                amenityView.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(this, R.drawable.ic_check_green),
                        null, null, null);

                // Set drawable color (optional, if your vector isn't green)
                // amenityView.getCompoundDrawables()[0].setTint(ContextCompat.getColor(this, R.color.text_color_approved));

                amenityView.setCompoundDrawablePadding(16); // 16dp padding

                // Add bottom margin
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 8); // 8dp bottom margin
                amenityView.setLayoutParams(params);

                llDetailAmenitiesContainer.addView(amenityView);
            }
        }
    }

    // Handle the back arrow click in the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close this activity and return to previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}