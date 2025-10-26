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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminProfileFragment extends Fragment {

    private static final String ADMIN_EMAIL = "admin@venueease.com";

    // Views from fragment_admin_profile.xml
    private TextView tvAdminName, tvAdminEmailHeader, tvAdminEmailInfo;
    private MaterialButton btnEditProfile, btnChangePassword, btnLogout;

    // SharedPreferences for session
    private SharedPreferences sessionPrefs;
    private SharedPreferences userAccountsPrefs;

    public AdminProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();
        if (context == null) {
            // Handle error: Fragment not attached properly
            Log.e("AdminProfileFragment", "Context is null in onViewCreated. Cannot initialize SharedPreferences.");
            Toast.makeText(getContext(), "Error loading profile.", Toast.LENGTH_SHORT).show();
            return; // Exit early
        }
        sessionPrefs = context.getSharedPreferences(LoginActivity.SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        userAccountsPrefs = context.getSharedPreferences(LoginActivity.USER_ACCOUNTS_PREFS, Context.MODE_PRIVATE);

        // Find views
        tvAdminName = view.findViewById(R.id.tv_admin_name);
        tvAdminEmailHeader = view.findViewById(R.id.tv_admin_email_header);
        tvAdminEmailInfo = view.findViewById(R.id.tv_admin_email_info);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Load and display admin info
        loadProfileInfo();

        // Setup Listeners
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> logoutAdmin());
    }

    private void loadProfileInfo() {
        // Load admin details from UserAccounts SharedPreferences
        String currentAdminName = userAccountsPrefs.getString(ADMIN_EMAIL + "_fullname", "Admin User"); // Default if not found
        // Email is constant
        tvAdminName.setText(currentAdminName);
        tvAdminEmailHeader.setText(ADMIN_EMAIL);
        tvAdminEmailInfo.setText(ADMIN_EMAIL);
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Declare dialogView as final so it's accessible inside the listener
        final View dialogView = inflater.inflate(R.layout.dialog_edit_admin_profile, null);
        builder.setView(dialogView);

        TextInputEditText etEditFullName = dialogView.findViewById(R.id.et_edit_fullname);
        TextInputEditText etEditEmail = dialogView.findViewById(R.id.et_edit_email);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_edit);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_changes);

        String currentAdminName = userAccountsPrefs.getString(ADMIN_EMAIL + "_fullname", "Admin User");
        etEditFullName.setText(currentAdminName);
        etEditEmail.setText(ADMIN_EMAIL);
        etEditEmail.setEnabled(false);

        // Declare dialog as final so it's accessible inside the listener
        final AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newName = etEditFullName.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                etEditFullName.setError("Name cannot be empty");
                return;
            }

            SharedPreferences.Editor editor = userAccountsPrefs.edit();
            editor.putString(ADMIN_EMAIL + "_fullname", newName);
            editor.apply();

            tvAdminName.setText(newName);
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Use the final dialog variable
        });

        dialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Declare dialogView as final
        final View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        TextInputEditText etConfirmNewPassword = dialogView.findViewById(R.id.et_confirm_new_password);
        ImageButton btnClose = dialogView.findViewById(R.id.btn_close_change_password);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_change_password);
        MaterialButton btnChange = dialogView.findViewById(R.id.btn_confirm_change_password);

        // Declare dialog as final
        final AlertDialog dialog = builder.create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnChange.setOnClickListener(v -> {
            String currentPassInput = etCurrentPassword.getText().toString();
            String newPass = etNewPassword.getText().toString();
            String confirmPass = etConfirmNewPassword.getText().toString();

            // GET STORED PASSWORD FROM SharedPreferences
            String storedAdminPassword = userAccountsPrefs.getString(ADMIN_EMAIL, "admin");

            // Validation
            if (TextUtils.isEmpty(currentPassInput)) {
                etCurrentPassword.setError("Required"); return;
            }
            // CHECK AGAINST STORED PASSWORD
            if (!currentPassInput.equals(storedAdminPassword)) {
                etCurrentPassword.setError("Incorrect current password"); return;
            }
            if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
                etNewPassword.setError("Minimum 6 characters"); return;
            }
            if (TextUtils.isEmpty(confirmPass)) {
                etConfirmNewPassword.setError("Required"); return;
            }
            if (!newPass.equals(confirmPass)) {
                etConfirmNewPassword.setError("Passwords do not match"); return;
            }

            SharedPreferences.Editor editor = userAccountsPrefs.edit();
            editor.putString(ADMIN_EMAIL, newPass);
            editor.apply();

            Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Use the final dialog variable
        });

        dialog.show();
    }

    private void logoutAdmin() {
        // Clear session SharedPreferences
        SharedPreferences.Editor editor = sessionPrefs.edit();
        editor.clear();
        editor.apply();

        // Navigate back to LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish(); // Finish the AdminDashboardActivity
    }
}