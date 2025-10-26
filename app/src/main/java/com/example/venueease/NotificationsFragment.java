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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsFragment extends Fragment implements NotificationsAdapter.NotificationActionListener {

    private DatabaseHelper dbHelper;
    private RecyclerView rvNotifications;
    private NotificationsAdapter notificationsAdapter;
    private List<Notification> notificationList;

    private ChipGroup chipGroupFilter;
    private MaterialButton btnMarkAllRead;
    private TextView tvEmptyNotifications;

    private String currentUserEmailOrAdmin;
    private boolean showUnreadOnly = false;

    public NotificationsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());

        // Determine user/admin context
        SharedPreferences sessionPrefs = getActivity().getSharedPreferences(LoginActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        String role = sessionPrefs.getString(LoginActivity.KEY_USER_ROLE, "user");
        currentUserEmailOrAdmin = "admin".equals(role) ? "admin" : sessionPrefs.getString(LoginActivity.KEY_EMAIL, null);

        // Find Views
        rvNotifications = view.findViewById(R.id.rv_notifications);
        chipGroupFilter = view.findViewById(R.id.chip_group_notif_filter);
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);
        tvEmptyNotifications = view.findViewById(R.id.tv_empty_notifications);

        // Setup
        setupRecyclerView();
        setupListeners();
        loadNotifications(); // Initial load
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications(); // Refresh when tab is shown
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(getContext(), notificationList, this);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(notificationsAdapter);
    }

    private void setupListeners() {
        // Filter Chips
        chipGroupFilter.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {

                if (checkedIds.isEmpty()) {
                    showUnreadOnly = false;
                    Chip allChip = group.findViewById(R.id.chip_notif_all);
                    if (allChip != null && !allChip.isChecked()) {
                        allChip.setChecked(true);
                    }
                } else {
                    // Get the single selected ID from the list
                    int selectedChipId = checkedIds.get(0);

                    // Compare the int ID
                    if (selectedChipId == R.id.chip_notif_unread) {
                        showUnreadOnly = true;
                    } else {
                        showUnreadOnly = false;
                    }
                }
                loadNotifications(); // Reload list based on the updated filter
            }
        });


        // Mark All Read Button
        btnMarkAllRead.setOnClickListener(v -> {
            if (currentUserEmailOrAdmin != null) {
                int updatedCount = dbHelper.markAllNotificationsAsRead(currentUserEmailOrAdmin);
                if (updatedCount > 0) {
                    Toast.makeText(getContext(), updatedCount + " notifications marked as read.", Toast.LENGTH_SHORT).show();
                    loadNotifications(); // Refresh the list
                } else {
                    Toast.makeText(getContext(), "No unread notifications.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadNotifications() {
        if (currentUserEmailOrAdmin == null) {
            tvEmptyNotifications.setText("Error loading notifications.");
            tvEmptyNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
            notificationList.clear();
            notificationsAdapter.notifyDataSetChanged();
            return;
        }

        List<Notification> newNotifications = dbHelper.getNotifications(currentUserEmailOrAdmin, showUnreadOnly);
        notificationsAdapter.updateNotifications(newNotifications);

        // Handle empty view
        if (newNotifications.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            tvEmptyNotifications.setVisibility(View.VISIBLE);
            tvEmptyNotifications.setText(showUnreadOnly ? "No unread notifications." : "No notifications yet.");
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            tvEmptyNotifications.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMarkReadUnread(Notification notification, int position) {
        boolean newReadStatus = !notification.isRead();
        boolean success = dbHelper.updateNotificationReadStatus(notification.getNotificationId(), newReadStatus);
        if (success) {
            // Update the specific item in the list and adapter
            notification.setRead(newReadStatus); // Update the model object
            notificationsAdapter.notifyItemChanged(position); // Visually update the item

            if (showUnreadOnly && newReadStatus) {
                loadNotifications();
            }

            Toast.makeText(getContext(), newReadStatus ? "Marked as read" : "Marked as unread", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteNotification(Notification notification, int position) {
        boolean success = dbHelper.deleteNotification(notification.getNotificationId());
        if (success) {
            // Remove item visually from the adapter
            notificationsAdapter.removeItem(position);

            // Update empty view
            if (notificationsAdapter.getItemCount() == 0) {
                tvEmptyNotifications.setVisibility(View.VISIBLE);
                rvNotifications.setVisibility(View.GONE);
                tvEmptyNotifications.setText(showUnreadOnly ? "No unread notifications." : "No notifications yet.");
            }
            Toast.makeText(getContext(), "Notification deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to delete notification", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNotificationClicked(Notification notification) {
        Toast.makeText(getContext(), "Clicked: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
    }
}