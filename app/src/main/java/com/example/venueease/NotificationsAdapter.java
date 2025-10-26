package com.example.venueease;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;

    // Interface for actions
    public interface NotificationActionListener {
        void onMarkReadUnread(Notification notification, int position);
        void onDeleteNotification(Notification notification, int position);
        void onNotificationClicked(Notification notification);
    }
    private NotificationActionListener listener;

    public NotificationsAdapter(Context context, List<Notification> notificationList, NotificationActionListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.bind(notification, listener, position);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Method to update the list
    public void updateNotifications(List<Notification> newNotificationList) {
        this.notificationList.clear();
        this.notificationList.addAll(newNotificationList);
        notifyDataSetChanged();
    }

    // Method to remove an item at a specific position
    public void removeItem(int position) {
        if (position >= 0 && position < notificationList.size()) {
            notificationList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notificationList.size());
        }
    }

    // Method to update the read status of an item at a specific position
    public void updateItemReadStatus(int position, boolean isRead) {
        if (position >= 0 && position < notificationList.size()) {
            notificationList.get(position).setRead(isRead);
            notifyItemChanged(position);
        }
    }


    // ViewHolder Class
    class NotificationViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardNotificationItem;
        ImageView ivNotifIcon, ivMarkReadUnread, ivDeleteNotif;
        TextView tvNotifTitle, tvNotifMessage, tvNotifTimestamp;
        LinearLayout llNotifDetails;
        TextView tvNotifDetail1, tvNotifDetail2, tvNotifDetail3, tvNotifDetail4; // Detail fields

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotificationItem = itemView.findViewById(R.id.card_notification_item);
            ivNotifIcon = itemView.findViewById(R.id.iv_notif_icon);
            ivMarkReadUnread = itemView.findViewById(R.id.iv_mark_read_unread);
            ivDeleteNotif = itemView.findViewById(R.id.iv_delete_notif);
            tvNotifTitle = itemView.findViewById(R.id.tv_notif_title);
            tvNotifMessage = itemView.findViewById(R.id.tv_notif_message);
            tvNotifTimestamp = itemView.findViewById(R.id.tv_notif_timestamp);
            llNotifDetails = itemView.findViewById(R.id.ll_notif_details);
            tvNotifDetail1 = itemView.findViewById(R.id.tv_notif_detail_1);
            tvNotifDetail2 = itemView.findViewById(R.id.tv_notif_detail_2);
            tvNotifDetail3 = itemView.findViewById(R.id.tv_notif_detail_3);
            tvNotifDetail4 = itemView.findViewById(R.id.tv_notif_detail_4);
        }

        void bind(Notification notification, NotificationActionListener actionListener, int position) {
            tvNotifTitle.setText(notification.getTitle());
            tvNotifMessage.setText(notification.getMessage());
            tvNotifTimestamp.setText(notification.getTimestamp());

            int iconRes = R.drawable.ic_bell;
            int iconTint = android.R.color.holo_blue_dark;
            boolean showDetailsBox = false;

            String type = notification.getNotificationType();
            if ("NEW_BOOKING".equals(type)) { // Admin
                iconRes = R.drawable.ic_bell;
                iconTint = android.R.color.holo_blue_dark;;
                showDetailsBox = true;
            } else if ("BOOKING_APPROVED".equals(type)) { // Both
                iconRes = R.drawable.ic_check_circle;
                iconTint = R.color.text_color_approved;
                showDetailsBox = true; // Show details for approved too
            } else if ("BOOKING_REJECTED".equals(type)) { // User
                iconRes = R.drawable.ic_rejected;
                iconTint = R.color.text_color_rejected;
            } else if ("PAYMENT_RECEIVED".equals(type) || "PAYMENT_SUCCESSFUL".equals(type)) {
                iconRes = R.drawable.ic_check_circle;
                iconTint = R.color.text_color_approved;
                showDetailsBox = true;
            } else if ("RATING_SUBMITTED".equals(type)) { // Admin
                iconRes = R.drawable.ic_star;
                iconTint = android.R.color.holo_blue_dark;
            }

            ivNotifIcon.setImageResource(iconRes);
            ivNotifIcon.setColorFilter(ContextCompat.getColor(context, iconTint));

            // Handle Read/Unread State
            if (notification.isRead()) {
                cardNotificationItem.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                tvNotifTitle.setTypeface(null, Typeface.NORMAL);
                tvNotifMessage.setTypeface(null, Typeface.NORMAL);
                ivMarkReadUnread.setImageResource(R.drawable.ic_mark_unread); // Icon to mark as UNREAD
                ivMarkReadUnread.setContentDescription("Mark as unread");
            } else {
                cardNotificationItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.unread_notification_background));
                tvNotifTitle.setTypeface(null, Typeface.BOLD);
                tvNotifMessage.setTypeface(null, Typeface.BOLD);
                ivMarkReadUnread.setImageResource(R.drawable.ic_check); // Icon to mark as READ
                ivMarkReadUnread.setContentDescription("Mark as read");
            }

            // Handle Optional Details Box
            if (showDetailsBox) {
                llNotifDetails.setVisibility(View.VISIBLE);
                tvNotifDetail1.setText("Date: " + notification.getBookingId());
                tvNotifDetail2.setText("User: " + notification.getUserEmail());
                tvNotifDetail3.setText("Venue ID: " + notification.getVenueId());
                tvNotifDetail4.setText("Details...");
                // Hide fields if no data
                tvNotifDetail1.setVisibility(!TextUtils.isEmpty(tvNotifDetail1.getText()) ? View.VISIBLE : View.GONE);
                tvNotifDetail2.setVisibility(!TextUtils.isEmpty(tvNotifDetail2.getText()) ? View.VISIBLE : View.GONE);
                tvNotifDetail3.setVisibility(!TextUtils.isEmpty(tvNotifDetail3.getText()) ? View.VISIBLE : View.GONE);
                tvNotifDetail4.setVisibility(!TextUtils.isEmpty(tvNotifDetail4.getText()) ? View.VISIBLE : View.GONE);

            } else {
                llNotifDetails.setVisibility(View.GONE);
            }

            // Set Click Listeners
            ivMarkReadUnread.setOnClickListener(v -> actionListener.onMarkReadUnread(notification, position));
            ivDeleteNotif.setOnClickListener(v -> actionListener.onDeleteNotification(notification, position));
        }
    }
}