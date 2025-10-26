package com.example.venueease;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    public static final String USER_ACCOUNTS_PREFS = "UserAccounts";

    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnCreateAccount;
    private TextView tvSignIn;
    private SharedPreferences userAccountsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize SharedPreferences for storing user accounts
        userAccountsPrefs = getSharedPreferences(USER_ACCOUNTS_PREFS, Context.MODE_PRIVATE);

        // Find views
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email_register);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password_register);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnCreateAccount = findViewById(R.id.btn_create_account);
        tvSignIn = findViewById(R.id.tv_sign_in);

        // Set click listener for "Create Account" button
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });

        // Set click listener for "Sign in" text
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish this activity to go back to Login
                finish();
            }
        });
    }

    private void handleRegistration() {
        // Get input from fields
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full Name is required");
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone Number is required");
            etPhone.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Check if user already exists
        if (userAccountsPrefs.contains(email)) {
            etEmail.setError("This email is already registered");
            etEmail.requestFocus();
            return;
        }

        // Save New User
        SharedPreferences.Editor editor = userAccountsPrefs.edit();

        // Use the email as the key and store the password
        editor.putString(email, password);

        editor.putString(email + "_fullname", fullName);
        editor.putString(email + "_phone", phone);

        editor.apply();

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

        // Go back to the Login activity
        finish();
    }
}