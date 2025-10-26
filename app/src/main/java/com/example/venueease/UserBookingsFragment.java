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

    @Override
    public void onPayNowClicked(Booking booking) {
        PaymentOptionsFragment optionsFragment = PaymentOptionsFragment.newInstance(booking);
        optionsFragment.show(getChildFragmentManager(), "PaymentOptions");
    }

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

    @Override
    public void onPaymentCancelled() {
        Toast.makeText(getContext(), "Payment cancelled", Toast.LENGTH_SHORT).show();
    }

    private void handlePaymentAttempt(Booking booking, String paymentMethodDetails, String paymentMethod) {
        String paymentId = "PAY_" + System.currentTimeMillis() + "_" + paymentMethod;

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

    @Override
    public void onPaymentMethodBackPressed() {
        Toast.makeText(getContext(), "Back pressed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPaymentSuccess(Booking booking, String paymentId) {
        // Update booking status in Database to "Confirmed"
        boolean success = dbHelper.updateBookingStatus(booking.getBookingId(), BookingsAdapter.STATUS_CONFIRMED);

        if (success) {
            // ADD NOTIFICATIONS (ADMIN + USER)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String paymentDate = sdf.format(new Date());

            // Admin Notification
            String adminNotifTitle = "Payment Received \uD83D\uDCB8";
            String adminNotifMsg = String.format(Locale.getDefault(),
                    "Payment of ₹%.0f received from %s for %s booking on %s. The booking is now confirmed.",
                    booking.getTotalPrice(),
                    booking.getUserName(),
                    booking.getVenue().getName(),
                    booking.getEventDate());
            dbHelper.addNotification("admin", "PAYMENT_RECEIVED", adminNotifTitle, adminNotifMsg, booking.getBookingId(), booking.getVenueId());

            // User Notification
            String userNotifTitle = "Payment Successful! ✅";
            String userNotifMsg = String.format(Locale.getDefault(),
                    "Payment confirmed for your booking at %s on %s. Your booking is now confirmed and you will receive further details shortly. Thank you for choosing our services! Payment ID: %s",
                    booking.getVenue().getName(),
                    booking.getEventDate(),
                    paymentId);
            dbHelper.addNotification(booking.getUserEmail(), "PAYMENT_SUCCESSFUL", userNotifTitle, userNotifMsg, booking.getBookingId(), booking.getVenueId());
            // END NOTIFICATIONS

            // Show the Success Dialog
            PaymentSuccessFragment successFragment = PaymentSuccessFragment.newInstance(booking, paymentId);
            successFragment.show(getChildFragmentManager(), "PaymentSuccess");

            // Refresh the list in the background
            loadUserBookings();

        } else {
            Toast.makeText(getContext(), "Error confirming booking status.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRateVenueClicked(Booking booking) {
        RatingDialogFragment ratingDialog = RatingDialogFragment.newInstance(booking);
        ratingDialog.show(getChildFragmentManager(), "RatingDialog");
    }

    @Override
    public void onPaymentSuccessClosed() {
        loadUserBookings();
    }

    @Override
    public void onRatingSubmitted(Booking booking, float rating, String comment) {
        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date = dateFormat.format(new Date());

        // Save rating to DB
        boolean success = dbHelper.addRating(
                booking.getVenueId(),
                booking.getBookingId(),
                booking.getUserId(),
                rating,
                comment,
                date);

        if (success) {
            // ADD NOTIFICATION FOR ADMIN
            String adminNotifTitle = "New Rating Submitted ⭐";
            String adminNotifMsg = String.format(Locale.getDefault(),
                    "%s submitted a %.1f star rating for %s (Booking #%d). Comment: %s",
                    booking.getUserName(),
                    rating,
                    booking.getVenue().getName(),
                    booking.getBookingId(),
                    comment.isEmpty() ? "No comment" : comment);
            dbHelper.addNotification("admin", "RATING_SUBMITTED", adminNotifTitle, adminNotifMsg, booking.getBookingId(), booking.getVenueId());

            Toast.makeText(getContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to submit rating.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRatingSkipped(Booking booking) {
        Toast.makeText(getContext(), "Rating skipped.", Toast.LENGTH_SHORT).show();
    }
}