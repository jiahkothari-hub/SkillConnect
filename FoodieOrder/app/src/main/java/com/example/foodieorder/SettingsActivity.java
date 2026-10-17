package com.example.foodieorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

// 21. Settings (saved with SharedPreferences)
public class SettingsActivity extends BaseActivity {

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar("Settings");

        prefs = AppData.prefs(this);

        setupSwitch(R.id.swNotifications, "notifications", true, "Notifications");
        setupSwitch(R.id.swLocation, "location_services", true, "Location Services");
        setupSwitch(R.id.swSync, "sync_firebase", true, "Sync with Firebase");
        setupSwitch(R.id.swBackup, "auto_backup", false, "Auto Backup to File");

        SwitchCompat swDark = findViewById(R.id.swDark);
        swDark.setChecked(prefs.getBoolean("dark_mode", false));
        swDark.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean("dark_mode", checked).apply();
            AppCompatDelegate.setDefaultNightMode(checked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        });

        findViewById(R.id.tvAbout).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("About App")
                .setMessage("Foodie Order v1.0\n\nA food ordering app made for Mobile Application Development.\n\n"
                        + "Features: Firebase Authentication, SQLite CRUD, File Handling (TXT/CSV), "
                        + "Firebase Realtime Database, Google Location Tracker, Restaurant Ordering, WebView and Sharing.")
                .setPositiveButton("OK", null)
                .show());

        findViewById(R.id.tvHelp).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@foodieorder.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Foodie Order - Feedback");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSwitch(int id, String key, boolean def, String name) {
        SwitchCompat sw = findViewById(id);
        sw.setChecked(prefs.getBoolean(key, def));
        sw.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean(key, checked).apply();
            Toast.makeText(this, name + (checked ? " ON" : " OFF"), Toast.LENGTH_SHORT).show();
        });
    }
}
