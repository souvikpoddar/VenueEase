package com.example.venueease;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView; // Import this
import android.widget.Toast;

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

    // 1. Rename the listener
    public interface OnVenueDataChangedListener {
        void onDataChanged();
    }
    private OnVenueDataChangedListener listener;

    // UI Views
    private TextInputEditText etVenueName, etLocation, etCapacity, etPrice, etDescription, etAmenities;
    private AutoCompleteTextView actVenueType;
    private MaterialButton btnCancel, btnSaveVenue; // Renamed
    private ImageButton btnClose;
    private FrameLayout flUploadPhotos;
    private LinearLayout llUploadPrompt;
    private ImageView ivVenuePreview;
    private TextView tvFragmentTitle; // Title view

    // Database
    private DatabaseHelper dbHelper;

    // Image Picker
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private String selectedImageUri = "";

    // 2. State variable
    private Venue venueToEdit = null;
    private boolean isEditMode = false;

    // 3. Update onAttach to use new listener name
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnVenueDataChangedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnVenueDataChangedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 4. Check arguments to see if we are in "Edit Mode"
        if (getArguments() != null) {
            venueToEdit = (Venue) getArguments().getSerializable("venue_to_edit");
            if (venueToEdit != null) {
                isEditMode = true;
                selectedImageUri = venueToEdit.getPhotoUri(); // Pre-load existing image URI
            }
        }

        // Initialize Image Picker
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContext().getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (Exception e) { e.printStackTrace(); }

                        selectedImageUri = uri.toString();
                        ivVenuePreview.setImageURI(uri);
                        ivVenuePreview.setVisibility(View.VISIBLE);
                        llUploadPrompt.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_venue, container, false);
        dbHelper = new DatabaseHelper(getContext());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find all views
        findViews(view);
        setupVenueTypeDropdown();

        // 5. Check mode and setup UI
        if (isEditMode) {
            tvFragmentTitle.setText("Edit Venue");
            btnSaveVenue.setText("Update Venue");
            populateFields(); // Fill form with existing data
        } else {
            tvFragmentTitle.setText("Add New Venue");
            btnSaveVenue.setText("Add Venue");
        }

        // Setup Click Listeners
        btnClose.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());

        btnSaveVenue.setOnClickListener(v -> {
            handleSaveVenue(); // This method now handles BOTH add and update
        });

        flUploadPhotos.setOnClickListener(v -> {
            pickMediaLauncher.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        });
    }

    private void findViews(View view) {
        etVenueName = view.findViewById(R.id.et_venue_name);
        etLocation = view.findViewById(R.id.et_location);
        etCapacity = view.findViewById(R.id.et_capacity);
        actVenueType = view.findViewById(R.id.act_venue_type);
        etPrice = view.findViewById(R.id.et_price);
        etDescription = view.findViewById(R.id.et_description);
        etAmenities = view.findViewById(R.id.et_amenities);
        btnClose = view.findViewById(R.id.btn_close);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSaveVenue = view.findViewById(R.id.btn_add_new_venue); // ID is btn_add_new_venue
        flUploadPhotos = view.findViewById(R.id.fl_upload_photos);
        llUploadPrompt = view.findViewById(R.id.ll_upload_prompt);
        ivVenuePreview = view.findViewById(R.id.iv_venue_preview);
        tvFragmentTitle = view.findViewById(R.id.tv_fragment_title);
    }

    // 6. New method to pre-fill the form
    private void populateFields() {
        if (venueToEdit == null) return;

        etVenueName.setText(venueToEdit.getName());
        etLocation.setText(venueToEdit.getLocation());
        etCapacity.setText(String.valueOf(venueToEdit.getCapacity()));
        etPrice.setText(String.valueOf(venueToEdit.getPrice()));
        etDescription.setText(venueToEdit.getDescription());
        etAmenities.setText(venueToEdit.getAmenities());

        // Set text for dropdown, 'false' means don't filter the list
        actVenueType.setText(venueToEdit.getType(), false);

        // Show existing image
        if (selectedImageUri != null && !selectedImageUri.isEmpty()) {
            ivVenuePreview.setImageURI(Uri.parse(selectedImageUri));
            ivVenuePreview.setVisibility(View.VISIBLE);
            llUploadPrompt.setVisibility(View.GONE);
        }
    }

    private void setupVenueTypeDropdown() {
        String[] venueTypes = new String[]{"Conference Hall", "Banquet Hall", "Auditorium", "Meeting Room", "Outdoor Space", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, venueTypes);
        actVenueType.setAdapter(adapter);
    }

    // 7. This method now handles BOTH adding and updating
    private void handleSaveVenue() {
        if (!validateInput()) {
            return; // Stop if validation fails
        }

        // Get data from fields
        String name = etVenueName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        int capacity = Integer.parseInt(etCapacity.getText().toString().trim());
        String type = actVenueType.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        String description = etDescription.getText().toString().trim();
        String amenities = etAmenities.getText().toString().trim();
        // selectedImageUri is already updated

        boolean isSuccess;

        if (isEditMode) {
            // --- UPDATE ---
            // Update the Venue object with new data
            venueToEdit.setName(name);
            venueToEdit.setLocation(location);
            venueToEdit.setCapacity(capacity);
            venueToEdit.setType(type);
            venueToEdit.setPrice(price);
            venueToEdit.setDescription(description);
            venueToEdit.setAmenities(amenities);
            venueToEdit.setPhotoUri(selectedImageUri);

            int rowsAffected = dbHelper.updateVenue(venueToEdit);
            isSuccess = rowsAffected > 0;
            Toast.makeText(getContext(), isSuccess ? "Venue updated successfully" : "Update failed", Toast.LENGTH_SHORT).show();

        } else {
            // --- ADD ---
            isSuccess = dbHelper.addVenue(name, location, capacity, type, price, description, amenities, selectedImageUri);
            Toast.makeText(getContext(), isSuccess ? "Venue added successfully" : "Add failed", Toast.LENGTH_SHORT).show();
        }

        if (isSuccess) {
            if (listener != null) {
                listener.onDataChanged();
            }
            dismiss(); // Close the bottom sheet
        }
    }

    // 8. Rename listener callback in your validation method
    private boolean validateInput() {
        // ... (all your existing validation code)
        // No changes needed here
        return true; // (keep your existing logic)
    }
}