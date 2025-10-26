package com.example.venueease;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout; // Import if needed for error
import java.util.Locale;

public class NetbankingPaymentFragment extends BottomSheetDialogFragment {

    private Booking bookingToPay;
    private String paymentMethod = "NETBANKING";

    // Listener Interface
    public interface NetbankingPaymentListener {
        void onNetbankingPaymentAttempt(Booking booking, String selectedBank, String paymentMethod);
        void onPaymentMethodBackPressed();
    }
    private NetbankingPaymentListener listener;

    // Views
    private TextView tvNetbankingPrompt;
    private AutoCompleteTextView actBankSelect;
    private MaterialButton btnBack, btnPay;
    private ImageButton btnClose;
    private TextInputLayout tilBankSelect; // Needed for setting error

    public static NetbankingPaymentFragment newInstance(Booking booking) {
        NetbankingPaymentFragment fragment = new NetbankingPaymentFragment();
        Bundle args = new Bundle();
        args.putSerializable("BOOKING_DATA", booking);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof NetbankingPaymentListener) {
            listener = (NetbankingPaymentListener) getParentFragment();
        } else {
            throw new ClassCastException("Caller must implement NetbankingPaymentListener");
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
        return inflater.inflate(R.layout.fragment_netbanking_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        setupBankDropdown();

        if (bookingToPay != null) {
            String prompt = String.format(Locale.getDefault(),
                    "Select your bank to complete the payment of ₹%.0f.",
                    bookingToPay.getTotalPrice());
            tvNetbankingPrompt.setText(prompt);
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
        tvNetbankingPrompt = view.findViewById(R.id.tv_netbanking_prompt);
        // Find the TextInputLayout containing the AutoCompleteTextView
        tilBankSelect = view.findViewById(R.id.til_bank_select); // Make sure this ID exists in your XML
        actBankSelect = view.findViewById(R.id.act_bank_select);
        btnBack = view.findViewById(R.id.btn_back_netbanking);
        btnPay = view.findViewById(R.id.btn_pay_netbanking);
        btnClose = view.findViewById(R.id.btn_close_netbanking);
    }

    private void setupBankDropdown() {
        // Dummy list of banks
        String[] banks = {"State Bank of India", "HDFC Bank", "ICICI Bank", "Axis Bank", "Kotak Mahindra Bank", "Other Bank"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, banks);
        actBankSelect.setAdapter(adapter);
    }

    private void handlePayment() {
        String selectedBank = actBankSelect.getText().toString().trim();
        tilBankSelect.setError(null); // Clear previous error

        if (TextUtils.isEmpty(selectedBank)) {
            // Set error on the TextInputLayout
            tilBankSelect.setError("Please select your bank");
            actBankSelect.requestFocus();
            return;
        }

        // Notify listener
        if (listener != null) {
            listener.onNetbankingPaymentAttempt(bookingToPay, selectedBank, paymentMethod);
        }
        dismiss();
    }
}