package com.example.venueease;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // Hardcoded Admin Credentials
    private static final String ADMIN_EMAIL = "admin@venueease.com";
    private static final String ADMIN_PASSWORD = "admin";

    // SharedPreferences for the CURRENT SESSION
    public static final String SESSION_PREFS_NAME = "UserPrefs";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USER_ROLE = "user_role"; // "admin" or "user"

    // SharedPreferences for ALL SAVED USER ACCOUNTS
    public static final String USER_ACCOUNTS_PREFS = "UserAccounts";

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn;
    private TextView tvForgotPassword, tvSignUp;

    private SharedPreferences sessionPrefs;
    private SharedPreferences userAccountsPrefs;
    private ActivityResultLauncher<Intent> resetPasswordLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize BOTH SharedPreferences
        sessionPrefs = getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE);
        userAccountsPrefs = getSharedPreferences(USER_ACCOUNTS_PREFS, Context.MODE_PRIVATE);

        initializeAdminCredentials();

        // 1. CHECK IF ALREADY LOGGED IN
        checkLoginStatus();

        setContentView(R.layout.activity_login);

        resetPasswordLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        View rootLayout = findViewById(R.id.login_root_layout);
                        Snackbar.make(rootLayout, "Password reset link sent to your email!", Snackbar.LENGTH_LONG).show();
                    }
                }
        );

        // Find Views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignUp = findViewById(R.id.tv_sign_up);

        // 2. SIGN IN BUTTON CLICK LISTENER
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Navigation to Register
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Navigation to Reset Password
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                resetPasswordLauncher.launch(intent);
            }
        });
    }

    private void initializeAdminCredentials() {
        // Check if the admin email key already exists in user accounts
        if (!userAccountsPrefs.contains(ADMIN_EMAIL)) {
            SharedPreferences.Editor editor = userAccountsPrefs.edit();
            // Store password using email as key
            editor.putString(ADMIN_EMAIL, ADMIN_PASSWORD);
            // Store full name using email_fullname as key
            editor.putString(ADMIN_EMAIL + "_fullname", "Admin User"); // Initial default name
            editor.apply();
            Log.i("LoginActivity", "Initial admin credentials saved to SharedPreferences.");
        }
    }

    /**
     * Checks session status and navigates to the correct dashboard based on role.
     */
    private void checkLoginStatus() {
        if (sessionPrefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            // User is logged in, check their role
            String role = sessionPrefs.getString(KEY_USER_ROLE, "user"); // Default to "user"

            if (role.equals("admin")) {
                navigateToAdminDashboard();
            } else {
                navigateToUserDashboard();
            }
        }
    }

    /**
     * Handles login logic for both Admin and registered Users.
     */
    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {  return; }
        if (TextUtils.isEmpty(password)) { return; }

        // --- 1. Check for ADMIN ---
        if (email.equals(ADMIN_EMAIL)) {
            // Fetch the stored admin password
            String storedAdminPassword = userAccountsPrefs.getString(ADMIN_EMAIL, ADMIN_PASSWORD); // Default to original if somehow missing

            if (password.equals(storedAdminPassword)) {
                // Admin login successful
                saveSession("admin", email);
                Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                navigateToAdminDashboard();
            } else {
                // Incorrect password for admin
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_LONG).show();
            }
            return; // Stop here if it was the admin email
        }

        // --- 2. Check for USER (No changes needed here) ---
        if (userAccountsPrefs.contains(email)) {
            // ... (existing user password check logic) ...
            String savedPassword = userAccountsPrefs.getString(email, null);
            if (password.equals(savedPassword)) {
                saveSession("user", email);
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                navigateToUserDashboard();
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_LONG).show();
            }
        } else {
            // Email not found in user accounts (and wasn't admin)
            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Saves the current user's session details
     */
    private void saveSession(String role, String email) {
        SharedPreferences.Editor editor = sessionPrefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToUserDashboard() {
        // You need to create this Activity
        Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}