package com.example.foodieorder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

// 19. Profile
public class ProfileActivity extends BaseActivity {

    TextView tvName, tvEmail, tvPhone, tvAddress, tvCuisine;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupToolbar("My Profile");

        prefs = AppData.prefs(this);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvCuisine = findViewById(R.id.tvCuisine);

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditDialog());
        showProfile();
    }

    private void showProfile() {
        tvName.setText(prefs.getString("user_name", "User"));
        tvEmail.setText(prefs.getString("user_email", ""));
        tvPhone.setText(prefs.getString("user_phone", "+91 9876543210"));
        tvAddress.setText(prefs.getString("user_address", "Bandra West, Mumbai"));
        tvCuisine.setText(prefs.getString("user_cuisine", "Italian"));
    }

    private void showEditDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etAddress = view.findViewById(R.id.etAddress);
        EditText etCuisine = view.findViewById(R.id.etCuisine);
        etName.setText(tvName.getText());
        etPhone.setText(tvPhone.getText());
        etAddress.setText(tvAddress.getText());
        etCuisine.setText(tvCuisine.getText());

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(view)
                .setPositiveButton("Save", (d, w) -> {
                    prefs.edit()
                            .putString("user_name", etName.getText().toString().trim())
                            .putString("user_phone", etPhone.getText().toString().trim())
                            .putString("user_address", etAddress.getText().toString().trim())
                            .putString("user_cuisine", etCuisine.getText().toString().trim())
                            .apply();
                    showProfile();
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
