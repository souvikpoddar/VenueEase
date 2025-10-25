package com.example.venueease;

import android.content.Context;
import android.content.Intent; // Import this
import android.net.Uri; // Import this
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout; // Import this
import android.widget.ImageButton;
import android.widget.ImageView; // Import this
import android.widget.LinearLayout; // Import this
import android.widget.Toast;

// Import these for ActivityResultLauncher
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddVenueFragment extends BottomSheetDialogFragment {

    // 1. Interface for communication
    // This allows us to tell AdminDashboardActivity to refresh its list
    public interface VenueAddListener {
        void onVenueAdded();
    }
    private VenueAddListener listener;

    // UI Views
    private TextInputEditText etVenueName, etLocation, etCapacity, etPrice, etDescription, etAmenities;
    private AutoCompleteTextView actVenueType;
    private MaterialButton btnCancel, btnAddNewVenue;
    private ImageButton btnClose;
    private FrameLayout flUploadPhotos;
    private LinearLayout llUploadPrompt;
    private ImageView ivVenuePreview;

    // Database
    private DatabaseHelper dbHelper;

    // 1. Declare the ActivityResultLauncher
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    // 2. Variable to store the selected image URI
    private String selectedImageUri = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure that the host activity implements the listener
        try {
            listener = (VenueAddListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement VenueAddListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_venue, container, false);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(getContext());

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 3. Initialize the ActivityResultLauncher in onCreate
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            // Image selected successfully

                            // 4. Persist permission to access the URI
                            try {
                                getContext().getContentResolver().takePersistableUriPermission(
                                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // 5. Store the URI string and update the UI
                            selectedImageUri = uri.toString();
                            ivVenuePreview.setImageURI(uri);
                            ivVenuePreview.setVisibility(View.VISIBLE);
                            llUploadPrompt.setVisibility(View.GONE);

                        } else {
                            // User cancelled
                            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find all views
        etVenueName = view.findViewById(R.id.et_venue_name);
        etLocation = view.findViewById(R.id.et_location);
        etCapacity = view.findViewById(R.id.et_capacity);
        actVenueType = view.findViewById(R.id.act_venue_type);
        etPrice = view.findViewById(R.id.et_price);
        etDescription = view.findViewById(R.id.et_description);
        etAmenities = view.findViewById(R.id.et_amenities);
        btnClose = view.findViewById(R.id.btn_close);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnAddNewVenue = view.findViewById(R.id.btn_add_new_venue);
        flUploadPhotos = view.findViewById(R.id.fl_upload_photos);
        llUploadPrompt = view.findViewById(R.id.ll_upload_prompt);
        ivVenuePreview = view.findViewById(R.id.iv_venue_preview);

        // 2. Setup Venue Type Dropdown
        setupVenueTypeDropdown();

        // 3. Setup Click Listeners
        btnClose.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());

        btnAddNewVenue.setOnClickListener(v -> {
            // This is where we validate and save
            handleAddVenue();
        });

        // 6. Set click listener for the photo upload area
        flUploadPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the modern image picker
                pickMediaLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                );
            }
        });
    }

    private void setupVenueTypeDropdown() {
        // Define the list of venue types
        String[] venueTypes = new String[] {
                "Conference Hall",
                "Banquet Hall",
                "Auditorium",
                "Meeting Room",
                "Outdoor Space",
                "Other"
        };

        // Create the adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                venueTypes
        );

        // Set the adapter to the AutoCompleteTextView
        actVenueType.setAdapter(adapter);
    }

    private void handleAddVenue() {
        // 4. Validate input first
        if (!validateInput()) {
            return; // Stop if validation fails
        }

        // 5. If validation passes, get data and save to DB
        String name = etVenueName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        int capacity = Integer.parseInt(etCapacity.getText().toString().trim());
        String type = actVenueType.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        String description = etDescription.getText().toString().trim();
        String amenities = etAmenities.getText().toString().trim();
        String photoUri = selectedImageUri;

        // 6. Call DatabaseHelper to add the venue
        boolean isAdded = dbHelper.addVenue(name, location, capacity, type, price, description, amenities, photoUri);

        if (isAdded) {
            Toast.makeText(getContext(), "Venue added successfully", Toast.LENGTH_SHORT).show();
            // 7. Notify the dashboard to refresh
            if (listener != null) {
                listener.onVenueAdded();
            }
            dismiss(); // Close the bottom sheet
        } else {
            Toast.makeText(getContext(), "Failed to add venue", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        // Get text from fields
        String name = etVenueName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String type = actVenueType.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        // Reset errors
        etVenueName.setError(null);
        etLocation.setError(null);
        etCapacity.setError(null);
        actVenueType.setError(null);
        etPrice.setError(null);

        // Check for empty fields
        if (TextUtils.isEmpty(name)) {
            etVenueName.setError("Venue Name is required");
            etVenueName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(capacityStr)) {
            etCapacity.setError("Capacity is required");
            etCapacity.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(type)) {
            actVenueType.setError("Venue Type is required");
            actVenueType.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return false;
        }

        // Check for valid numbers
        try {
            Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            etCapacity.setError("Invalid number");
            etCapacity.requestFocus();
            return false;
        }

        try {
            Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price");
            etPrice.requestFocus();
            return false;
        }

        // All checks passed
        return true;
    }
}