package com.example.foodieorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// 14. Firebase Authentication (Email / Google)
public class FirebaseAuthActivity extends BaseActivity {

    FirebaseAuth auth;
    TextView tvUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_auth);
        setupToolbar("Firebase Authentication");

        auth = FirebaseAuth.getInstance();
        tvUser = findViewById(R.id.tvUser);

        View.OnClickListener emailClick = v -> showEmailDialog();
        View.OnClickListener googleClick = v -> GoogleHelper.startSignIn(this);
        findViewById(R.id.btnEmailSignIn).setOnClickListener(emailClick);
        findViewById(R.id.ivEmail).setOnClickListener(emailClick);
        findViewById(R.id.btnGoogleSignIn).setOnClickListener(googleClick);
        findViewById(R.id.ivGoogle).setOnClickListener(googleClick);

        findViewById(R.id.btnSync).setOnClickListener(v -> startActivity(new Intent(this, FirebaseSyncActivity.class)));
        findViewById(R.id.btnViewData).setOnClickListener(v -> startActivity(new Intent(this, FirebaseDataActivity.class)));
        findViewById(R.id.btnFirebaseLogout).setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Signed out of Firebase", Toast.LENGTH_SHORT).show();
            updateUser();
        });

        updateUser();
    }

    private void updateUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            tvUser.setText("Signed in to Firebase as:\n" + user.getEmail());
        } else {
            tvUser.setText("Not signed in to Firebase");
        }
    }

    private void showEmailDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_email_login, null);
        EditText etEmail = view.findViewById(R.id.etDlgEmail);
        EditText etPassword = view.findViewById(R.id.etDlgPassword);
        etEmail.setText(AppData.prefs(this).getString("user_email", ""));

        new AlertDialog.Builder(this)
                .setTitle("Sign in with Email")
                .setView(view)
                .setPositiveButton("Sign In", (d, w) -> {
                    String email = etEmail.getText().toString().trim();
                    String pass = etPassword.getText().toString().trim();
                    if (email.isEmpty() || pass.length() < 6) {
                        Toast.makeText(this, "Enter email and 6+ character password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Firebase login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        updateUser();
                    });
                })
                .setNeutralButton("Register", (d, w) -> {
                    String email = etEmail.getText().toString().trim();
                    String pass = etPassword.getText().toString().trim();
                    if (email.isEmpty() || pass.length() < 6) {
                        Toast.makeText(this, "Enter email and 6+ character password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Firebase account created", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Register failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        updateUser();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleHelper.RC_GOOGLE) {
            GoogleHelper.handleResult(this, data, this::updateUser);
        }
    }
}
