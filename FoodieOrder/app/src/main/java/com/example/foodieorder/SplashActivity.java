package com.example.foodieorder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

// 1. Splash Screen - waits 2 seconds then opens Login or Home
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean loggedIn = AppData.prefs(this).getBoolean("logged_in", false);
            if (loggedIn) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 2000);
    }
}
