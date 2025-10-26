package com.example.venueease;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Locale;

public class UpiPaymentFragment extends BottomSheetDialogFragment {

    private Booking bookingToPay;
    private String paymentMethod = "UPI"; // Constant for this fragment

    // Interface to communicate back
    public interface UpiPaymentListener {
        void onUpiPaymentAttempt(Booking booking, String upiId, String paymentMethod);
        void onPaymentMethodBackPressed(); // Go back to options
    }
    private UpiPaymentListener listener;

    // Views
    private TextView tvUpiPrompt;
    private TextInputLayout tilUpiId;
    private TextInputEditText etUpiId;
    private MaterialButton btnBack, btnPay;
    private ImageButton btnClose;

    public static UpiPaymentFragment newInstance(Booking booking) {
        UpiPaymentFragment fragment = new UpiPaymentFragment();
        Bundle args = new Bundle();
        args.putSerializable("BOOKING_DATA", booking);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the hosting fragment or activity implements the listener
        if (getParentFragment() instanceof UpiPaymentListener) {
            listener = (UpiPaymentListener) getParentFragment();
        } else {
            throw new ClassCastException("Caller must implement UpiPaymentListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingToPay = (Booking) getArguments().getSerializable("BOOKING_DATA");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upi_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);

        if (bookingToPay != null) {
            String prompt = String.format(Locale.getDefault(),
                    "Enter your UPI details to complete the payment of ₹%.0f.",
                    bookingToPay.getTotalPrice());
            tvUpiPrompt.setText(prompt);
            btnPay.setText(String.format(Locale.getDefault(), "Pay ₹%.0f", bookingToPay.getTotalPrice()));
        } else {
            dismiss();
            return;
        }

        // Set Listeners
        btnClose.setOnClickListener(v -> dismiss());
        btnBack.setOnClickListener(v -> {
            if (listener != null) listener.onPaymentMethodBackPressed();
            dismiss();
        });
        btnPay.setOnClickListener(v -> handlePayment());
    }

    private void findViews(View view) {
        tvUpiPrompt = view.findViewById(R.id.tv_upi_prompt);
        tilUpiId = view.findViewById(R.id.til_upi_id);
        etUpiId = view.findViewById(R.id.et_upi_id);
        btnBack = view.findViewById(R.id.btn_back_upi);
        btnPay = view.findViewById(R.id.btn_pay_upi);
        btnClose = view.findViewById(R.id.btn_close_upi);
    }

    private void handlePayment() {
        String upiId = etUpiId.getText().toString().trim();
        if (TextUtils.isEmpty(upiId)) {
            tilUpiId.setError("UPI ID is required");
            etUpiId.requestFocus();
            return;
        }
        // Basic UPI ID format check (optional but recommended)
        if (!upiId.contains("@")) {
            tilUpiId.setError("Invalid UPI ID format");
            etUpiId.requestFocus();
            return;
        }
        tilUpiId.setError(null);

        // Notify listener to start processing
        if (listener != null) {
            listener.onUpiPaymentAttempt(bookingToPay, upiId, paymentMethod);
        }
        dismiss(); // Close this dialog
    }
}