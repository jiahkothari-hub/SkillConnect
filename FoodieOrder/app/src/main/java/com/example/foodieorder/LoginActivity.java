package com.example.foodieorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

// 2. Login screen (Email + Google with Firebase Authentication)
public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnGoogle = findViewById(R.id.btnGoogle);
        Button btnTabGoogle = findViewById(R.id.btnTabGoogle);
        TextView tvForgot = findViewById(R.id.tvForgot);
        TextView tvGoSignUp = findViewById(R.id.tvGoSignUp);

        btnSignIn.setOnClickListener(v -> signIn());
        btnGoogle.setOnClickListener(v -> GoogleHelper.startSignIn(this));
        btnTabGoogle.setOnClickListener(v -> GoogleHelper.startSignIn(this));
        tvGoSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));

        tvForgot.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etEmail.setError("Enter your email first");
                return;
            }
            auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Could not send reset email", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void signIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email required");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password required");
            return;
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            SharedPreferences p = AppData.prefs(this);
            if (task.isSuccessful()) {
                p.edit().putString("user_email", email).apply();
                goHome();
            } else if (email.equals(p.getString("user_email", "")) && password.equals(p.getString("user_password", ""))) {
                // Firebase not reachable -> use the account saved on this phone
                goHome();
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goHome() {
        AppData.prefs(this).edit().putBoolean("logged_in", true).apply();
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleHelper.RC_GOOGLE) {
            GoogleHelper.handleResult(this, data, this::goHome);
        }
    }
}
