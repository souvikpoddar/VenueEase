package com.example.venueease;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddVenueFragment extends BottomSheetDialogFragment {

    // We'll declare all our UI views here later
    private ImageButton btnClose;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_venue, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        btnClose = view.findViewById(R.id.btn_close);

        // --- LOGIC TO BE ADDED LATER ---
        // We'll add click listeners for btn_add_new_venue and validation here

        // For now, just make the close button work
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the bottom sheet
                dismiss();
            }
        });

        // We also need to add items to the AutoCompleteTextView for "Venue Type"
    }
}