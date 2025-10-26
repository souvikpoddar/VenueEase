package com.example.venueease;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {

    // Status constants for clarity
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_REJECTED = "Rejected";
    public static final String STATUS_CONFIRMED = "Confirmed"; // (e.g., after payment)

    private Context context;
    private List<Booking> bookingList;

    // Interface for handling button clicks
    public interface OnBookingActionListener {
        void onApprove(Booking booking);
        void onReject(Booking booking);
    }
    private OnBookingActionListener listener;

    public BookingsAdapter(Context context, List<Booking> bookingList, OnBookingActionListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_booking_admin, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bind(booking, listener);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    // Method to update the list
    public void updateBookings(List<Booking> newBookingList) {
        this.bookingList.clear();
        this.bookingList.addAll(newBookingList);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder Class
     */
    class BookingViewHolder extends RecyclerView.ViewHolder {

        // Declare all UI elements from list_item_booking_admin.xml
        TextView tvVenueName, tvBookingStatus, tvRequestFrom, tvEventDate, tvEventTime,
                tvEventType, tvEventPrice, tvVenueLocation, tvVenueCapacity,
                tvSpecialRequests, tvStatusMessage, tvSubmittedDate, tvAvailableTag;;
        LinearLayout llSpecialRequests, llStatusMessage, llActionButtons;
        MaterialButton btnReject, btnApprove;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find all views
            tvVenueName = itemView.findViewById(R.id.tv_venue_name);
            tvBookingStatus = itemView.findViewById(R.id.tv_booking_status);
            tvRequestFrom = itemView.findViewById(R.id.tv_request_from);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
            tvEventType = itemView.findViewById(R.id.tv_event_type);
            tvEventPrice = itemView.findViewById(R.id.tv_event_price);
            tvVenueLocation = itemView.findViewById(R.id.tv_venue_location);
            tvVenueCapacity = itemView.findViewById(R.id.tv_venue_capacity);
            tvSpecialRequests = itemView.findViewById(R.id.tv_special_requests);
            tvStatusMessage = itemView.findViewById(R.id.tv_status_message);
            tvSubmittedDate = itemView.findViewById(R.id.tv_submitted_date);
            tvAvailableTag = itemView.findViewById(R.id.tv_available_tag);
            llSpecialRequests = itemView.findViewById(R.id.ll_special_requests);
            llStatusMessage = itemView.findViewById(R.id.ll_status_message);
            llActionButtons = itemView.findViewById(R.id.ll_action_buttons);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnApprove = itemView.findViewById(R.id.btn_approve);
        }

        /**
         * Binds a Booking object to the ViewHolder's views
         */
        public void bind(Booking booking, OnBookingActionListener actionListener) {
            // --- 1. Set Basic Info ---
            if (booking.getVenue() != null) {
                tvVenueName.setText(booking.getVenue().getName());
                tvVenueLocation.setText(booking.getVenue().getLocation());
                tvVenueCapacity.setText(String.format(Locale.getDefault(), "Capacity: %d", booking.getVenue().getCapacity()));
            } else {
                // Fallback in case the venue was deleted or data is bad
                tvVenueName.setText("Venue Not Found");
                tvVenueLocation.setText("N/A");
                tvVenueCapacity.setText("N/A");
            }

            // This tag is now visible and will show
            tvAvailableTag.setVisibility(View.VISIBLE);




            tvRequestFrom.setText(String.format("Request from %s | %s", booking.getUserName(), booking.getUserEmail()));
            tvEventDate.setText(booking.getEventDate());
            tvEventTime.setText(String.format("%s - %s", booking.getStartTime(), booking.getEndTime()));
            tvEventType.setText(booking.getEventType());
            tvEventPrice.setText(String.format(Locale.getDefault(), "%.0f", booking.getTotalPrice()));
            tvSubmittedDate.setText(String.format("Submitted on %s", booking.getSubmittedDate()));

            // Handle special requests
            if (TextUtils.isEmpty(booking.getSpecialRequests())) {
                llSpecialRequests.setVisibility(View.GONE);
            } else {
                llSpecialRequests.setVisibility(View.VISIBLE);
                tvSpecialRequests.setText(booking.getSpecialRequests());
            }

            // --- 2. Handle Status-Specific UI ---
            String status = booking.getBookingStatus();
            tvBookingStatus.setText(status);

            switch (status) {
                case STATUS_PENDING:
                    tvBookingStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.tag_background_pending));
                    tvBookingStatus.setTextColor(ContextCompat.getColor(context, R.color.text_color_pending));
                    llActionButtons.setVisibility(View.VISIBLE);
                    llStatusMessage.setVisibility(View.GONE);
                    break;

                case STATUS_APPROVED:
                    tvBookingStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.tag_background_approved));
                    tvBookingStatus.setTextColor(ContextCompat.getColor(context, R.color.text_color_approved));
                    llActionButtons.setVisibility(View.GONE);
                    llStatusMessage.setVisibility(View.VISIBLE);
                    tvStatusMessage.setText("User has been notified and can now proceed with payment");
                    break;

                case STATUS_REJECTED:
                    tvBookingStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.tag_background_rejected));
                    tvBookingStatus.setTextColor(ContextCompat.getColor(context, R.color.text_color_rejected));
                    llActionButtons.setVisibility(View.GONE);
                    llStatusMessage.setVisibility(View.GONE);
                    break;

                case STATUS_CONFIRMED:
                    tvBookingStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.tag_background_confirmed));
                    tvBookingStatus.setTextColor(ContextCompat.getColor(context, R.color.text_color_confirmed));
                    llActionButtons.setVisibility(View.GONE);
                    llStatusMessage.setVisibility(View.VISIBLE);
                    tvStatusMessage.setText("Payment confirmed. Booking is finalized.");
                    break;
            }

            // --- 3. Set Click Listeners ---
            btnApprove.setOnClickListener(v -> actionListener.onApprove(booking));
            btnReject.setOnClickListener(v -> actionListener.onReject(booking));
        }
    }
}