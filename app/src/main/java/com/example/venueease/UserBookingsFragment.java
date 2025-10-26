package com.example.venueease;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat; // For rating date
import java.util.Date; // For rating date
import java.util.UUID; // For dummy payment ID
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserBookingsFragment extends Fragment implements
        UserBookingsAdapter.OnPayNowClickListener,
        PaymentOptionsFragment.PaymentOptionListener,
        UpiPaymentFragment.UpiPaymentListener,
        CardPaymentFragment.CardPaymentListener,
        NetbankingPaymentFragment.NetbankingPaymentListener,
        ProcessingPaymentFragment.PaymentProcessingListener,
        PaymentSuccessFragment.PaymentSuccessListener,
        RatingDialogFragment.RatingListener {

    private DatabaseHelper dbHelper;
    private RecyclerView rvUserBookings;
    private UserBookingsAdapter userBookingsAdapter;
    private List<Booking> bookingList;

    private TextView tvBookingCountHeader;
    private TextView tvEmptyUserBookings;

    // We need the user's email to filter bookings
    private String currentUserEmail;

    // Required empty public constructor
    public UserBookingsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());

        // Get current user's email from session
        SharedPreferences sessionPrefs = getActivity().getSharedPreferences(LoginActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        currentUserEmail = sessionPrefs.getString(LoginActivity.KEY_EMAIL, null);

        // Find views
        rvUserBookings = view.findViewById(R.id.rv_user_bookings);
        tvBookingCountHeader = view.findViewById(R.id.tv_booking_count_header);
        tvEmptyUserBookings = view.findViewById(R.id.tv_empty_user_bookings);

        // Setup
        setupRecyclerView();
        loadUserBookings();
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        userBookingsAdapter = new UserBookingsAdapter(getContext(), bookingList, this); // 'this' is the listener
        rvUserBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUserBookings.setAdapter(userBookingsAdapter);
    }

    // In UserBookingsFragment.java
    private void loadUserBookings() {
        if (currentUserEmail == null) {
            tvEmptyUserBookings.setText("Error: Could not find user information.");
            tvEmptyUserBookings.setVisibility(View.VISIBLE);
            rvUserBookings.setVisibility(View.GONE);
            tvBookingCountHeader.setText("0 bookings");
            return;
        }

        // Pass the user's email, null for status, and null for date to get all their bookings
        List<Booking> userBookings = dbHelper.getBookings(currentUserEmail, null, null);

        // Update adapter
        userBookingsAdapter.updateUserBookings(userBookings);

        // Update header count
        int count = userBookings.size();
        tvBookingCountHeader.setText(String.format(Locale.getDefault(), "%d booking%s", count, count == 1 ? "" : "s"));

        // Show/Hide empty view
        if (userBookings.isEmpty()) {
            rvUserBookings.setVisibility(View.GONE);
            tvEmptyUserBookings.setVisibility(View.VISIBLE);
            tvEmptyUserBookings.setText("You haven't made any bookings yet.");
        } else {
            rvUserBookings.setVisibility(View.VISIBLE);
            tvEmptyUserBookings.setVisibility(View.GONE);
        }
    }

    /**
     * Callback for the "Pay Now" button click from the adapter.
     */
    @Override
    public void onPayNowClicked(Booking booking) {
        PaymentOptionsFragment optionsFragment = PaymentOptionsFragment.newInstance(booking);
        // Important: Show using getChildFragmentManager() so the fragment can find its parent
        optionsFragment.show(getChildFragmentManager(), "PaymentOptions");
    }

    /**
     * From PaymentOptionsFragment - User selected a payment method
     */
    @Override
    public void onPaymentOptionSelected(String option, Booking booking) {
        switch (option) {
            case "UPI":
                UpiPaymentFragment upiFragment = UpiPaymentFragment.newInstance(booking);
                upiFragment.show(getChildFragmentManager(), "UpiPayment");
                break;
            case "CARD":
                CardPaymentFragment cardFragment = CardPaymentFragment.newInstance(booking);
                cardFragment.show(getChildFragmentManager(), "CardPayment");
                break;
            case "NETBANKING":
                NetbankingPaymentFragment nbFragment = NetbankingPaymentFragment.newInstance(booking);
                nbFragment.show(getChildFragmentManager(), "NetbankingPayment");
                break;
        }
    }

    /**
     * From PaymentOptionsFragment - User closed the dialog
     */
    @Override
    public void onPaymentCancelled() {
        // Optional: Show a message or do nothing
        Toast.makeText(getContext(), "Payment cancelled", Toast.LENGTH_SHORT).show();
    }

    /**
     * From specific payment fragments - User clicked "Pay"
     * We centralize the processing logic here.
     */
    private void handlePaymentAttempt(Booking booking, String paymentMethodDetails, String paymentMethod) {
        // Generate a dummy Payment ID
        String paymentId = "PAY_" + System.currentTimeMillis() + "_" + paymentMethod;

        // Show the processing dialog
        ProcessingPaymentFragment processingFragment = ProcessingPaymentFragment.newInstance(booking, paymentId);
        processingFragment.show(getChildFragmentManager(), "ProcessingPayment");
    }

    @Override
    public void onUpiPaymentAttempt(Booking booking, String upiId, String paymentMethod) {
        handlePaymentAttempt(booking, "UPI ID: " + upiId, paymentMethod);
    }

    @Override
    public void onCardPaymentAttempt(Booking booking, String cardName, String cardNumber, String expiry, String cvv, String paymentMethod) {
        handlePaymentAttempt(booking, "Card ending " + cardNumber.substring(cardNumber.length() - 4), paymentMethod);
    }

    @Override
    public void onNetbankingPaymentAttempt(Booking booking, String selectedBank, String paymentMethod) {
        handlePaymentAttempt(booking, "Bank: " + selectedBank, paymentMethod);
    }

    /**
     * From payment fragments - User clicked "Back"
     */
    @Override
    public void onPaymentMethodBackPressed() {
        // Re-show the payment options dialog
        // Need the booking object again, might need to store it temporarily
        // For simplicity, we can just show a toast for now
        Toast.makeText(getContext(), "Back pressed", Toast.LENGTH_SHORT).show();
        // Ideally, re-fetch the booking associated with the back press
        // and show PaymentOptionsFragment.newInstance(booking)
    }

    /**
     * From ProcessingPaymentFragment - Dummy processing finished
     */
    @Override
    public void onPaymentSuccess(Booking booking, String paymentId) {
        // 1. Update booking status in Database to "Confirmed"
        boolean success = dbHelper.updateBookingStatus(booking.getBookingId(), BookingsAdapter.STATUS_CONFIRMED);

        if (success) {
            // 2. Show the Success Dialog
            PaymentSuccessFragment successFragment = PaymentSuccessFragment.newInstance(booking, paymentId);
            successFragment.show(getChildFragmentManager(), "PaymentSuccess");

            // 3. Refresh the list in the background
            loadUserBookings();

        } else {
            Toast.makeText(getContext(), "Error confirming booking status.", Toast.LENGTH_SHORT).show();
            // Optionally show an error dialog
        }
    }

    /**
     * From PaymentSuccessFragment - User clicked "Rate Venue"
     */
    @Override
    public void onRateVenueClicked(Booking booking) {
        RatingDialogFragment ratingDialog = RatingDialogFragment.newInstance(booking);
        ratingDialog.show(getChildFragmentManager(), "RatingDialog");
    }

    /**
     * From PaymentSuccessFragment - User closed the success dialog
     */
    @Override
    public void onPaymentSuccessClosed() {
        // List should already be refreshed by onPaymentSuccess
        // We might not need this callback if onDismiss in success fragment handles it
        loadUserBookings(); // Refresh just in case
    }

    /**
     * From RatingDialogFragment - User submitted a rating
     */
    @Override
    public void onRatingSubmitted(Booking booking, float rating, String comment) {
        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date = dateFormat.format(new Date());

        // Save rating to DB
        boolean success = dbHelper.addRating(
                booking.getVenueId(),
                booking.getBookingId(),
                booking.getUserId(), // Assuming user ID is stored correctly
                rating,
                comment,
                date);

        if (success) {
            Toast.makeText(getContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to submit rating.", Toast.LENGTH_SHORT).show();
        }
        // No need to refresh booking list here unless rating affects it directly
    }

    /**
     * From RatingDialogFragment - User clicked "Skip"
     */
    @Override
    public void onRatingSkipped(Booking booking) {
        // Do nothing or show a simple message
        Toast.makeText(getContext(), "Rating skipped.", Toast.LENGTH_SHORT).show();
    }
}