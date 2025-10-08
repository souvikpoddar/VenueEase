package com.example.venueease; // <-- MAKE SURE THIS PACKAGE NAME IS CORRECT!

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class VenueFilterBottomSheet extends BottomSheetDialogFragment {

    public VenueFilterBottomSheet() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_filter_venues, container, false);

        // Setup Close button functionality (using lambda)
        ImageButton btnClose = view.findViewById(R.id.btn_close_filter);
        btnClose.setOnClickListener(v -> dismiss());

        // Setup Apply Filters button functionality (using lambda)
        view.findViewById(R.id.btn_apply_filters).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Filters Applied!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        // Setup Clear All button functionality (using lambda)
        view.findViewById(R.id.btn_clear_all).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Filters Cleared!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}