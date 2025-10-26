package com.example.venueease;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class UserBookingsAdapter extends RecyclerView.Adapter<UserBookingsAdapter.UserBookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;

    // Interface for Pay Now button
    public interface OnPayNowClickListener {
        void onPayNowClicked(Booking booking);
    }
    private OnPayNowClickListener payNowListener;

    public UserBookingsAdapter(Context context, List<Booking> bookingList, OnPayNowClickListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.payNowListener = listener;
    }

    @NonNull
    @Override
    public UserBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_booking_user, parent, false);
        return new UserBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserBookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bind(booking, payNowListener);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    // Method to update the list
    public void updateUserBookings(List<Booking> newBookingList) {
        this.bookingList.clear();
        this.bookingList.addAll(newBookingList);
        notifyDataSetChanged();
    }

    // ViewHolder Class
    class UserBookingViewHolder extends RecyclerView.ViewHolder {

        // Declare UI elements
        TextView tvVenueName, tvBookingId, tvStatusText, tvBookingDate, tvBookingTime,
                tvEventType, tvBookingPrice, tvSpecialRequests, tvActionTitle,
                tvActionSubtitle, tvFooterMessage;
        ImageView ivStatusIcon, ivActionIcon;
        LinearLayout llStatusBadge, llSpecialRequests;
        RelativeLayout rlStatusAction;
        MaterialButton btnPayNow;


        public UserBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views
            tvVenueName = itemView.findViewById(R.id.tv_user_booking_venue_name);
            tvBookingId = itemView.findViewById(R.id.tv_user_booking_id);
            llStatusBadge = itemView.findViewById(R.id.ll_status_badge);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            tvStatusText = itemView.findViewById(R.id.tv_user_booking_status);
            tvBookingDate = itemView.findViewById(R.id.tv_user_booking_date);
            tvBookingTime = itemView.findViewById(R.id.tv_user_booking_time);
            tvEventType = itemView.findViewById(R.id.tv_user_booking_event_type);
            tvBookingPrice = itemView.findViewById(R.id.tv_user_booking_price);
            llSpecialRequests = itemView.findViewById(R.id.ll_user_special_requests);
            tvSpecialRequests = itemView.findViewById(R.id.tv_user_special_requests);
            rlStatusAction = itemView.findViewById(R.id.rl_status_action);
            ivActionIcon = itemView.findViewById(R.id.iv_action_icon);
            tvActionTitle = itemView.findViewById(R.id.tv_action_title);
            tvActionSubtitle = itemView.findViewById(R.id.tv_action_subtitle);
            btnPayNow = itemView.findViewById(R.id.btn_pay_now);
            tvFooterMessage = itemView.findViewById(R.id.tv_footer_message);
        }

        void bind(Booking booking, OnPayNowClickListener listener) {
            // Basic Info
            tvVenueName.setText(booking.getVenue() != null ? booking.getVenue().getName() : "Venue Not Found");
            tvBookingId.setText(String.format(Locale.getDefault(), "Booking #%d", booking.getBookingId()));
            tvBookingDate.setText(booking.getEventDate()); // Assuming DB format is user-friendly
            tvBookingTime.setText(String.format("%s - %s", booking.getStartTime(), booking.getEndTime()));
            tvEventType.setText(booking.getEventType());
            tvBookingPrice.setText(String.format(Locale.getDefault(), "₹%.0f", booking.getTotalPrice()));

            // Special Requests
            if (TextUtils.isEmpty(booking.getSpecialRequests())) {
                llSpecialRequests.setVisibility(View.GONE);
            } else {
                llSpecialRequests.setVisibility(View.VISIBLE);
                tvSpecialRequests.setText(booking.getSpecialRequests());
            }

            // Status Badge and Action Area
            String status = booking.getBookingStatus();
            int statusBgRes, statusIconRes, statusTextColor;
            String statusText, footerText = "";
            boolean showActionArea = false;
            boolean showPayButton = false;
            String actionTitle = "", actionSubtitle = "";
            int actionIconRes = R.drawable.ic_info;
            int actionIconTint = R.color.text_color_confirmed;

            switch (status) {
                case BookingsAdapter.STATUS_PENDING:
                    statusText = "Pending Review";
                    statusBgRes = R.drawable.tag_background_pending;
                    statusIconRes = R.drawable.ic_pending;
                    statusTextColor = R.color.text_color_pending;
                    footerText = "Submitted on " + booking.getSubmittedDate() + " • Waiting for admin approval";
                    break;
                case BookingsAdapter.STATUS_APPROVED:
                    statusText = "Approved - Payment Pending";
                    statusBgRes = R.drawable.tag_background_approved;
                    statusIconRes = R.drawable.ic_check_circle;
                    statusTextColor = R.color.text_color_approved;
                    showActionArea = true;
                    showPayButton = true;
                    actionTitle = "Your booking has been approved!";
                    actionSubtitle = "Complete payment to confirm your booking";
                    actionIconRes = R.drawable.ic_check_circle;
                    actionIconTint = R.color.text_color_approved;
                    break;
                case BookingsAdapter.STATUS_REJECTED:
                    statusText = "Rejected";
                    statusBgRes = R.drawable.tag_background_rejected;
                    statusIconRes = R.drawable.ic_rejected;
                    statusTextColor = R.color.text_color_rejected;
                    footerText = "Submitted on " + booking.getSubmittedDate() + " • Booking rejected";
                    break;
                case BookingsAdapter.STATUS_CONFIRMED:
                    statusText = "Confirmed";
                    statusBgRes = R.drawable.tag_background_confirmed;
                    statusIconRes = R.drawable.ic_confirmed;
                    statusTextColor = R.color.text_color_confirmed;
                    footerText = "Booking confirmed on " + booking.getSubmittedDate();
                    break;
                default:
                    statusText = status;
                    statusBgRes = R.drawable.tag_background;
                    statusIconRes = R.drawable.ic_info;
                    statusTextColor = android.R.color.black;
            }

            // Apply Status Badge styles
            llStatusBadge.setBackgroundResource(statusBgRes);
            ivStatusIcon.setImageResource(statusIconRes);
            ivStatusIcon.setColorFilter(ContextCompat.getColor(context, statusTextColor));
            tvStatusText.setText(statusText);
            tvStatusText.setTextColor(ContextCompat.getColor(context, statusTextColor));

            // Show/Hide Footer
            if (!footerText.isEmpty()) {
                tvFooterMessage.setText(footerText);
                tvFooterMessage.setVisibility(View.VISIBLE);
            } else {
                tvFooterMessage.setVisibility(View.GONE);
            }

            // Show/Hide Action Area
            if (showActionArea) {
                rlStatusAction.setVisibility(View.VISIBLE);
                tvActionTitle.setText(actionTitle);
                tvActionSubtitle.setText(actionSubtitle);
                ivActionIcon.setImageResource(actionIconRes);
                ivActionIcon.setColorFilter(ContextCompat.getColor(context, actionIconTint));
                tvActionTitle.setTextColor(ContextCompat.getColor(context, actionIconTint));
                tvActionSubtitle.setTextColor(ContextCompat.getColor(context, actionIconTint));

                if (showPayButton) {
                    btnPayNow.setVisibility(View.VISIBLE);
                    btnPayNow.setOnClickListener(v -> listener.onPayNowClicked(booking));
                } else {
                    btnPayNow.setVisibility(View.GONE);
                }
            } else {
                rlStatusAction.setVisibility(View.GONE);
            }
        }
    }
}