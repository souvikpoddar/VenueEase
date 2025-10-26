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

public class CardPaymentFragment extends BottomSheetDialogFragment {

    private Booking bookingToPay;
    private String paymentMethod = "CARD";

    // Listener Interface
    public interface CardPaymentListener {
        void onCardPaymentAttempt(Booking booking, String cardName, String cardNumber, String expiry, String cvv, String paymentMethod);
        void onPaymentMethodBackPressed();
    }
    private CardPaymentListener listener;

    // Views
    private TextView tvCardPrompt;
    private TextInputLayout tilCardName, tilCardNumber, tilExpiry, tilCvv;
    private TextInputEditText etCardName, etCardNumber, etExpiry, etCvv;
    private MaterialButton btnBack, btnPay;
    private ImageButton btnClose;

    public static CardPaymentFragment newInstance(Booking booking) {
        CardPaymentFragment fragment = new CardPaymentFragment();
        Bundle args = new Bundle();
        args.putSerializable("BOOKING_DATA", booking);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof CardPaymentListener) {
            listener = (CardPaymentListener) getParentFragment();
        } else {
            throw new ClassCastException("Caller must implement CardPaymentListener");
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
        return inflater.inflate(R.layout.fragment_card_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);

        if (bookingToPay != null) {
            String prompt = String.format(Locale.getDefault(),
                    "Enter your card details to complete the payment of ₹%.0f.",
                    bookingToPay.getTotalPrice());
            tvCardPrompt.setText(prompt);
            btnPay.setText(String.format(Locale.getDefault(), "Pay ₹%.0f", bookingToPay.getTotalPrice()));
        } else {
            dismiss(); return;
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
        tvCardPrompt = view.findViewById(R.id.tv_card_prompt);
        tilCardName = view.findViewById(R.id.til_card_name);
        etCardName = view.findViewById(R.id.et_card_name);
        tilCardNumber = view.findViewById(R.id.til_card_number);
        etCardNumber = view.findViewById(R.id.et_card_number);
        tilExpiry = view.findViewById(R.id.til_expiry);
        etExpiry = view.findViewById(R.id.et_expiry);
        tilCvv = view.findViewById(R.id.til_cvv);
        etCvv = view.findViewById(R.id.et_cvv);
        btnBack = view.findViewById(R.id.btn_back_card);
        btnPay = view.findViewById(R.id.btn_pay_card);
        btnClose = view.findViewById(R.id.btn_close_card);
    }

    private void handlePayment() {
        // Validation
        String cardName = etCardName.getText().toString().trim();
        String cardNumber = etCardNumber.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();

        tilCardName.setError(null); tilCardNumber.setError(null);
        tilExpiry.setError(null); tilCvv.setError(null);

        if (TextUtils.isEmpty(cardName)) { tilCardName.setError("Required"); etCardName.requestFocus(); return; }
        if (TextUtils.isEmpty(cardNumber) || cardNumber.length() < 12) { tilCardNumber.setError("Invalid Card Number"); etCardNumber.requestFocus(); return; }
        if (TextUtils.isEmpty(expiry) || !expiry.matches("\\d{2}/\\d{2}")) { tilExpiry.setError("Invalid (MM/YY)"); etExpiry.requestFocus(); return; }
        if (TextUtils.isEmpty(cvv) || cvv.length() != 3) { tilCvv.setError("Invalid CVV"); etCvv.requestFocus(); return; }

        // Notify listener
        if (listener != null) {
            listener.onCardPaymentAttempt(bookingToPay, cardName, cardNumber, expiry, cvv, paymentMethod);
        }
        dismiss();
    }
}