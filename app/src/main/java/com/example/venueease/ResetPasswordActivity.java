package com.example.venueease;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmailReset;
    private MaterialButton btnSendResetLink;
    private TextView tvBackToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Find views
        etEmailReset = findViewById(R.id.et_email_reset);
        btnSendResetLink = findViewById(R.id.btn_send_reset_link);
        tvBackToSignIn = findViewById(R.id.tv_back_to_sign_in);

        // 1. "Send Reset Link" button click listener
        btnSendResetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendResetLink();
            }
        });

        // 2. "Back to Sign In" text click listener
        tvBackToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Both actions just go back to the Login screen
                navigateToLogin();
            }
        });
    }

    private void handleSendResetLink() {
        String email = etEmailReset.getText().toString().trim();

        // Simple validation to ensure email is not empty
        if (TextUtils.isEmpty(email)) {
            etEmailReset.setError("Email is required");
            etEmailReset.requestFocus();
            return;
        }

        // Show the dummy toast message as requested
        Toast.makeText(this, "Password reset link sent to your email!", Toast.LENGTH_LONG).show();

        // Redirect back to the sign in page
        navigateToLogin();
    }

    private void navigateToLogin() {
        // We just 'finish' this activity.
        // This will pop it off the stack and return to the previous activity (LoginActivity).
        finish();
    }
}