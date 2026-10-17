package com.example.foodieorder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

// 3. Sign Up (Email) - creates a Firebase account and saves user details
public class SignUpActivity extends BaseActivity {

    EditText etName, etEmail, etPassword, etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setupToolbar("Sign Up");

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvGoSignIn = findViewById(R.id.tvGoSignIn);

        btnSignUp.setOnClickListener(v -> signUp());
        tvGoSignIn.setOnClickListener(v -> finish());
    }

    private void signUp() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        if (name.isEmpty()) { etName.setError("Name required"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Enter a valid email"); return; }
        if (password.length() < 6) { etPassword.setError("Minimum 6 characters"); return; }
        if (!password.equals(confirm)) { etConfirm.setError("Passwords do not match"); return; }

        // save user on the phone (SharedPreferences)
        AppData.prefs(this).edit()
                .putString("user_name", name)
                .putString("user_email", email)
                .putString("user_password", password)
                .apply();

        // create the account in Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created in Firebase", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Account saved on this phone (Firebase not available)", Toast.LENGTH_LONG).show();
                    }
                    AppData.prefs(this).edit().putBoolean("logged_in", true).apply();
                    Intent i = new Intent(this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                });
    }
}
