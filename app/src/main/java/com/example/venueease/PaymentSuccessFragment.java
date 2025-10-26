package com.example.venueease;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class PaymentSuccessFragment extends BottomSheetDialogFragment {

    private Booking completedBooking;
    private String paymentId;

    // Listener Interface
    public interface PaymentSuccessListener {
        void onRateVenueClicked(Booking booking);
        void onPaymentSuccessClosed(); // To refresh the list
    }
    private PaymentSuccessListener listener;

    // Views
    private TextView tvPaymentId;
    private MaterialButton btnRateVenue;
    private ImageButton btnClose;

    // Static constructor
    public static PaymentSuccessFragment newInstance(Booking booking, String paymentId) {
        PaymentSuccessFragment fragment = new PaymentSuccessFragment();
        Bundle args = new Bundle();
        args.putSerializable("BOOKING_DATA", booking);
        args.putString("PAYMENT_ID", paymentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof PaymentSuccessListener) {
            listener = (PaymentSuccessListener) getParentFragment();
        } else {
            throw new ClassCastException("Caller must implement PaymentSuccessListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            completedBooking = (Booking) getArguments().getSerializable("BOOKING_DATA");
            paymentId = getArguments().getString("PAYMENT_ID");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);

        tvPaymentId.setText(paymentId != null ? paymentId : "N/A");

        // Set Listeners
        btnClose.setOnClickListener(v -> {
            if (listener != null) listener.onPaymentSuccessClosed();
            dismiss();
        });
        btnRateVenue.setOnClickListener(v -> {
            if (listener != null) listener.onRateVenueClicked(completedBooking);
            dismiss(); // Close this dialog to show the rating one
        });
    }

    private void findViews(View view) {
        tvPaymentId = view.findViewById(R.id.tv_payment_id);
        btnRateVenue = view.findViewById(R.id.btn_rate_venue);
        btnClose = view.findViewById(R.id.btn_close_success);
    }

    // Handle closing the dialog via swipe or back press
    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        // Ensure the list refreshes even if closed manually
        if (listener != null) {
            // Check if rate button was clicked maybe, otherwise call closed
            // listener.onPaymentSuccessClosed();
        }
    }
}