package com.example.venueease;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity; // Import this

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher; // Import this
import androidx.activity.result.contract.ActivityResultContracts; // Import this

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.snackbar.Snackbar; // Import this

public class LoginActivity extends AppCompatActivity {

    // Hardcoded Admin Credentials
    private static final String ADMIN_EMAIL = "admin@venueease.com";
    private static final String ADMIN_PASSWORD = "admin";

    // SharedPreferences constants
    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn;
    private TextView tvForgotPassword, tvSignUp;

    private SharedPreferences sharedPreferences;

    private ActivityResultLauncher<Intent> resetPasswordLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 1. CHECK IF ALREADY LOGGED IN
        checkLoginStatus();

        setContentView(R.layout.activity_login);

        // 2. Initialize the launcher
        resetPasswordLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // This is the callback
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Show the Snackbar if the result is OK
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
                // We will call a function to handle the login logic
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
                // Launch using the new launcher instead of startActivity()
                resetPasswordLauncher.launch(intent);
            }
        });
    }

    private void checkLoginStatus() {
        // Check if the 'isLoggedIn' flag is true in SharedPreferences
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            // User is already logged in, go directly to Dashboard
            navigateToAdminDashboard();
        }
        // If false, the app will just continue to show the login screen (setContentView)
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // 3. VALIDATE CREDENTIALS
        if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
            // Credentials are correct

            // 4. SAVE TO SHAREDPREFERENCES
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_EMAIL, email);
            editor.apply(); // Use apply() for asynchronous save

            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

            // 5. NAVIGATE TO DASHBOARD
            navigateToAdminDashboard();

        } else {
            // Invalid credentials
            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        // Add flags to clear the back stack
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Call finish() to remove LoginActivity from the back stack
    }
}