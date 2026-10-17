package com.example.foodieorder;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

// Runs once when the app starts: applies the saved Dark Mode setting
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        boolean dark = AppData.prefs(this).getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(dark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
