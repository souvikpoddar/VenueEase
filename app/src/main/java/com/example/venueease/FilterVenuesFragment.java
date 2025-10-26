package com.example.venueease;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class FilterVenuesFragment extends BottomSheetDialogFragment {

    // Interface to send data back to the activity
    public interface FilterListener {
        void onFiltersApplied(FilterCriteria criteria);
    }
    private FilterListener listener;

    // UI Views
    private TextInputEditText etDate;
    private AutoCompleteTextView actVenueType, actMinCapacity, actMaxPrice;
    private MaterialButton btnClearAll, btnApplyFilters;
    private ImageButton btnCloseFilter;

    // Data for dropdowns
    private final String[] venueTypes = {"All Types", "Conference Hall", "Banquet Hall", "Auditorium", "Meeting Room", "Outdoor Space", "Other"};
    private final String[] capacities = {"Any capacity", "50+", "100+", "200+", "500+"};
    private final String[] prices = {"Any price", "Under ₹500/hr", "Under ₹1000/hr", "Under ₹2000/hr"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Get the listener from the fragment that set us as its target
            listener = (FilterListener) getTargetFragment();
        } catch (ClassCastException e) {
            // This will now correctly tell you if VenuesFragment forgot to implement
            throw new ClassCastException("Calling fragment must implement FilterListener");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_venues, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        etDate = view.findViewById(R.id.et_date);
        actVenueType = view.findViewById(R.id.act_venue_type);
        actMinCapacity = view.findViewById(R.id.act_min_capacity);
        actMaxPrice = view.findViewById(R.id.act_max_price);
        btnClearAll = view.findViewById(R.id.btn_clear_all);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);
        btnCloseFilter = view.findViewById(R.id.btn_close_filter);

        // Setup dropdowns
        setupDropdowns();

        // Setup click listeners
        btnCloseFilter.setOnClickListener(v -> dismiss());

        etDate.setOnClickListener(v -> showDatePicker());

        btnClearAll.setOnClickListener(v -> clearFilters());

        btnApplyFilters.setOnClickListener(v -> applyFilters());
    }

    private void setupDropdowns() {
        actVenueType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, venueTypes));
        actMinCapacity.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, capacities));
        actMaxPrice.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, prices));
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(getContext(), (datePicker, y, m, d) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
            etDate.setText(selectedDate);
        }, year, month, day).show();
    }

    private void clearFilters() {
        etDate.setText("");
        actVenueType.setText(venueTypes[0], false);
        actMinCapacity.setText(capacities[0], false);
        actMaxPrice.setText(prices[0], false);
        // Apply the cleared filters (which is no filters)
        applyFilters();
    }

    private void applyFilters() {
        // Get values
        String date = etDate.getText().toString().trim();
        String venueType = actVenueType.getText().toString().trim();
        String capacityStr = actMinCapacity.getText().toString().trim();
        String priceStr = actMaxPrice.getText().toString().trim();

        // Parse values
        int minCapacity = FilterCriteria.ANY_CAPACITY;
        if (capacityStr.contains("+")) {
            minCapacity = Integer.parseInt(capacityStr.replace("+", ""));
        }

        double maxPrice = FilterCriteria.ANY_PRICE;
        if (priceStr.contains("₹")) {
            maxPrice = Double.parseDouble(priceStr.split("₹")[1].split("/")[0]);
        }

        if (venueType.equals("All Types")) {
            venueType = null; // Use null to signify "All"
        }

        if (date.isEmpty()) {
            date = null; // Use null to signify "Any"
        }

        // Create criteria object
        FilterCriteria criteria = new FilterCriteria(date, venueType, minCapacity, maxPrice);

        // Send to activity
        listener.onFiltersApplied(criteria);
        dismiss();
    }
}