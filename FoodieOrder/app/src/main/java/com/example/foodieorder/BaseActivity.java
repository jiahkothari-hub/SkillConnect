package com.example.foodieorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

// Every screen with a toolbar + back arrow extends this class
public class BaseActivity extends AppCompatActivity {

    protected void setupToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
