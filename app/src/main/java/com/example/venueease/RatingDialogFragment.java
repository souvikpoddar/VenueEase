package com.example.venueease;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RatingDialogFragment extends BottomSheetDialogFragment {

    private Booking bookingToRate;
    private float currentRating = 0; // To store the rating value

    // Listener Interface
    public interface RatingListener {
        void onRatingSubmitted(Booking booking, float rating, String comment);
        void onRatingSkipped(Booking booking);
    }
    private RatingListener listener;

    // Views
    private TextView tvVenueName;
    private RatingBar ratingBar;
    private TextInputEditText etComment;
    private MaterialButton btnSkip, btnSubmit;

    public static RatingDialogFragment newInstance(Booking booking) {
        RatingDialogFragment fragment = new RatingDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("BOOKING_DATA", booking);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof RatingListener) {
            listener = (RatingListener) getParentFragment();
        } else {
            throw new ClassCastException("Calling fragment must implement RatingListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingToRate = (Booking) getArguments().getSerializable("BOOKING_DATA");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rating_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);

        if (bookingToRate != null && bookingToRate.getVenue() != null) {
            tvVenueName.setText("Rate and review " + bookingToRate.getVenue().getName());
        } else {
            tvVenueName.setText("Rate and review this venue");
        }

        // Listener for rating bar changes
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            currentRating = rating;
        });

        // Button Listeners
        btnSkip.setOnClickListener(v -> {
            if (listener != null) listener.onRatingSkipped(bookingToRate);
            dismiss();
        });

        btnSubmit.setOnClickListener(v -> handleSubmitRating());
    }

    private void findViews(View view) {
        tvVenueName = view.findViewById(R.id.tv_rate_venue_name);
        ratingBar = view.findViewById(R.id.rating_bar);
        etComment = view.findViewById(R.id.et_rating_comment);
        btnSkip = view.findViewById(R.id.btn_skip_rating);
        btnSubmit = view.findViewById(R.id.btn_submit_rating);
    }

    private void handleSubmitRating() {
        if (currentRating == 0) {
            // Require at least one star
            Toast.makeText(getContext(), "Please select a rating (1-5 stars)", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = etComment.getText().toString().trim();

        // Notify listener
        if (listener != null) {
            listener.onRatingSubmitted(bookingToRate, currentRating, comment);
        }
        dismiss();
    }
}