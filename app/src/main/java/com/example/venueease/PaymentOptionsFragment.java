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
import com.google.android.material.card.MaterialCardView;
import java.util.Locale;

public class PaymentOptionsFragment extends BottomSheetDialogFragment {

    private Booking bookingToPay;

    // Interface to communicate selected option
    public interface PaymentOptionListener {
        void onPaymentOptionSelected(String option, Booking booking);
        void onPaymentCancelled(); // If user closes the dialog
    }
    private PaymentOptionListener listener;

    // Views
    private TextView tvPaymentPrompt, tvSummaryVenue, tvSummaryDate, tvSummaryTime, tvSummaryEvent, tvSummaryAmount;
    private MaterialCardView cardUpi, cardCard, cardNetbanking;
    private ImageButton btnClose;

    // Static constructor pattern for passing data
    public static PaymentOptionsFragment newInstance(Booking booking) {
        PaymentOptionsFragment fragment = new PaymentOptionsFragment();
        Bundle args = new Bundle();
        args.putSerializable("BOOKING_DATA", booking);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof PaymentOptionListener) {
            listener = (PaymentOptionListener) getParentFragment();
        } else if (context instanceof PaymentOptionListener) {
            listener = (PaymentOptionListener) context;
        } else {
            throw new ClassCastException("Caller must implement PaymentOptionListener");
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
        return inflater.inflate(R.layout.fragment_payment_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        if (bookingToPay != null) {
            populateSummary();
        } else {
            dismiss();
            return;
        }

        // Set Listeners
        btnClose.setOnClickListener(v -> {
            if (listener != null) listener.onPaymentCancelled();
            dismiss();
        });
        cardUpi.setOnClickListener(v -> {
            if (listener != null) listener.onPaymentOptionSelected("UPI", bookingToPay);
            dismiss();
        });
        cardCard.setOnClickListener(v -> {
            if (listener != null) listener.onPaymentOptionSelected("CARD", bookingToPay);
            dismiss();
        });
        cardNetbanking.setOnClickListener(v -> {
            if (listener != null) listener.onPaymentOptionSelected("NETBANKING", bookingToPay);
            dismiss();
        });
    }

    private void findViews(View view) {
        tvPaymentPrompt = view.findViewById(R.id.tv_payment_prompt);
        tvSummaryVenue = view.findViewById(R.id.tv_summary_venue);
        tvSummaryDate = view.findViewById(R.id.tv_summary_date);
        tvSummaryTime = view.findViewById(R.id.tv_summary_time);
        tvSummaryEvent = view.findViewById(R.id.tv_summary_event);
        tvSummaryAmount = view.findViewById(R.id.tv_summary_amount);
        cardUpi = view.findViewById(R.id.card_upi);
        cardCard = view.findViewById(R.id.card_card);
        cardNetbanking = view.findViewById(R.id.card_netbanking);
        btnClose = view.findViewById(R.id.btn_close_payment_options);
    }

    private void populateSummary() {
        String prompt = String.format(Locale.getDefault(),
                "Complete payment for your booking at %s on %s. Choose your preferred payment method.",
                bookingToPay.getVenue().getName(),
                bookingToPay.getEventDate());
        tvPaymentPrompt.setText(prompt);

        tvSummaryVenue.setText(bookingToPay.getVenue().getName());
        tvSummaryDate.setText(bookingToPay.getEventDate());
        tvSummaryTime.setText(String.format("%s - %s", bookingToPay.getStartTime(), bookingToPay.getEndTime()));
        tvSummaryEvent.setText(bookingToPay.getEventType());
        tvSummaryAmount.setText(String.format(Locale.getDefault(), "₹%.0f", bookingToPay.getTotalPrice()));
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
        }
    }
}