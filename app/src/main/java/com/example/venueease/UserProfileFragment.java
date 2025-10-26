package com.example.venueease;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText; // Use EditText for dialogs
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class UserProfileFragment extends Fragment {

    // Views from fragment_user_profile.xml
    private TextView tvUserProfileName, tvUserProfileEmailHeader, tvUserProfileFullnameInfo, tvUserProfileEmailInfo;
    private MaterialButton btnEditUserProfile, btnChangeUserPassword, btnUserLogout;

    // SharedPreferences
    private SharedPreferences sessionPrefs;
    private SharedPreferences userAccountsPrefs;

    // To store current user details
    private String currentUserEmail;
    private String currentUserName;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();
        if (context == null) {
            Log.e("UserProfileFragment", "Context is null in onViewCreated.");
            return; // Exit if context is not available
        }

        sessionPrefs = context.getSharedPreferences(LoginActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        userAccountsPrefs = context.getSharedPreferences(LoginActivity.USER_ACCOUNTS_PREFS, Context.MODE_PRIVATE);

        // Find views
        tvUserProfileName = view.findViewById(R.id.tv_user_profile_name);
        tvUserProfileEmailHeader = view.findViewById(R.id.tv_user_profile_email_header);
        tvUserProfileFullnameInfo = view.findViewById(R.id.tv_user_profile_fullname_info);
        tvUserProfileEmailInfo = view.findViewById(R.id.tv_user_profile_email_info);
        btnEditUserProfile = view.findViewById(R.id.btn_edit_user_profile);
        btnChangeUserPassword = view.findViewById(R.id.btn_change_user_password);
        btnUserLogout = view.findViewById(R.id.btn_user_logout);

        // Load and display user info
        loadProfileInfo();

        // Setup Listeners
        btnEditUserProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangeUserPassword.setOnClickListener(v -> showChangePasswordDialog());
        btnUserLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadProfileInfo() {
        // Get current user's email from session
        currentUserEmail = sessionPrefs.getString(LoginActivity.KEY_EMAIL, null);

        if (currentUserEmail == null) {
            // Handle error - user should be logged in to see this screen
            Toast.makeText(getContext(), "Error: User session not found.", Toast.LENGTH_SHORT).show();
            logoutUser(); // Log out if session is invalid
            return;
        }

        // Load user details from UserAccounts using email
        currentUserName = userAccountsPrefs.getString(currentUserEmail + "_fullname", "User"); // Default if somehow missing

        // Set UI text
        tvUserProfileName.setText(currentUserName);
        tvUserProfileEmailHeader.setText(currentUserEmail);
        tvUserProfileFullnameInfo.setText(currentUserName);
        tvUserProfileEmailInfo.setText(currentUserEmail);
    }

    private void showEditProfileDialog() {
        if (currentUserEmail == null) return; // Should not happen

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_user_profile, null);
        builder.setView(dialogView);

        TextInputEditText etEditFullName = dialogView.findViewById(R.id.et_edit_user_fullname);
        TextInputEditText etEditEmail = dialogView.findViewById(R.id.et_edit_user_email);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_user_edit);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_user_changes);

        // Pre-fill fields
        etEditFullName.setText(currentUserName);
        etEditEmail.setText(currentUserEmail);

        final AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newName = etEditFullName.getText().toString().trim();
            String newEmail = etEditEmail.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(newName)) {
                etEditFullName.setError("Name cannot be empty"); return;
            }
            if (TextUtils.isEmpty(newEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                etEditEmail.setError("Enter a valid email"); return;
            }

            // Check if email is being changed and if the new one already exists (excluding the current user)
            if (!newEmail.equals(currentUserEmail) && userAccountsPrefs.contains(newEmail)) {
                // Check if it's the admin email (cannot take admin email)
                if (newEmail.equals("admin@venueease.com")) {
                    etEditEmail.setError("Cannot use admin email address."); return;
                }
                // Check if it belongs to another user
                // Note: Since we only store email->password, we can't easily distinguish
                //       if the existing entry belongs to the *current* user if they haven't changed email yet.
                //       A safer approach is needed if multiple accounts with same email but different cases were possible.
                //       For this simple SharedPreferences setup, we allow changing email only if the new email doesn't exist at all.
                etEditEmail.setError("Email already registered by another user."); return;

            }

            // --- SAVE CHANGES TO SharedPreferences ---
            SharedPreferences.Editor editor = userAccountsPrefs.edit();

            // If email changed, we need to update all related keys
            if (!newEmail.equals(currentUserEmail)) {
                // Get old data
                String password = userAccountsPrefs.getString(currentUserEmail, "");
                // Remove old entries
                editor.remove(currentUserEmail);
                editor.remove(currentUserEmail + "_fullname");
                // Add new entries
                editor.putString(newEmail, password);
                editor.putString(newEmail + "_fullname", newName);
                // Update the current session email
                sessionPrefs.edit().putString(LoginActivity.KEY_EMAIL, newEmail).apply();
                currentUserEmail = newEmail; // Update local variable
            } else {
                // Only name changed
                editor.putString(currentUserEmail + "_fullname", newName);
            }
            editor.apply();
            // --- END SAVE ---

            currentUserName = newName; // Update local variable

            // Update the UI
            loadProfileInfo(); // Reload profile info on the main screen
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showChangePasswordDialog() {
        if (currentUserEmail == null) return; // Should not happen

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        TextInputEditText etConfirmNewPassword = dialogView.findViewById(R.id.et_confirm_new_password);
        ImageButton btnClose = dialogView.findViewById(R.id.btn_close_change_password);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_change_password);
        MaterialButton btnChange = dialogView.findViewById(R.id.btn_confirm_change_password);

        final AlertDialog dialog = builder.create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnChange.setOnClickListener(v -> {
            String currentPassInput = etCurrentPassword.getText().toString();
            String newPass = etNewPassword.getText().toString();
            String confirmPass = etConfirmNewPassword.getText().toString();

            // Get the user's stored password
            String storedPassword = userAccountsPrefs.getString(currentUserEmail, null);

            // Validation
            if (TextUtils.isEmpty(currentPassInput)) { /*...*/ return; }
            if (storedPassword == null || !currentPassInput.equals(storedPassword)) {
                etCurrentPassword.setError("Incorrect current password"); return;
            }
            if (TextUtils.isEmpty(newPass) || newPass.length() < 6) { /*...*/ return; }
            if (TextUtils.isEmpty(confirmPass)) { /*...*/ return; }
            if (!newPass.equals(confirmPass)) { /*...*/ return; }

            // --- SAVE NEW PASSWORD to SharedPreferences ---
            SharedPreferences.Editor editor = userAccountsPrefs.edit();
            editor.putString(currentUserEmail, newPass); // Overwrite old password
            editor.apply();
            // --- END SAVE ---

            Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void logoutUser() {
        // Clear session SharedPreferences
        SharedPreferences.Editor editor = sessionPrefs.edit();
        editor.clear();
        editor.apply();

        // Navigate back to LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish(); // Finish the UserDashboardActivity
    }
}