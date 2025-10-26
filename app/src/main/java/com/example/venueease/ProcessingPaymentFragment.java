package com.example.venueease;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ProcessingPaymentFragment extends BottomSheetDialogFragment {

    private static final long PROCESSING_DELAY_MS = 3000;

    private Booking bookingProcessed;
    private String paymentId; // The generated payment ID

    // Listener Interface
    public interface PaymentProcessingListener {
        void onPaymentSuccess(Booking booking, String paymentId);
    }
    private PaymentProcessingListener listener;

    // Static constructor
    public static ProcessingPaymentFragment newInstance(Booking booking, String paymentId) {
        ProcessingPaymentFragment fragment = new ProcessingPaymentFragment();
        Bundle args = new Bundle();
        args.putSerializable("BOOKING_DATA", booking);
        args.putString("PAYMENT_ID", paymentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof PaymentProcessingListener) {
            listener = (PaymentProcessingListener) getParentFragment();
        } else {
            throw new ClassCastException("Caller must implement PaymentProcessingListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingProcessed = (Booking) getArguments().getSerializable("BOOKING_DATA");
            paymentId = getArguments().getString("PAYMENT_ID");
        }
        // Prevent dismissing by swiping down or tapping outside
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_processing_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnClose = view.findViewById(R.id.btn_close_processing);
        btnClose.setVisibility(View.GONE); // Hide close button

        // Simulate delay then notify listener of success
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (listener != null) {
                listener.onPaymentSuccess(bookingProcessed, paymentId);
            }
            dismiss(); // Close this dialog
        }, PROCESSING_DELAY_MS);
    }
}