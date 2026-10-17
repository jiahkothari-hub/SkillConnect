package com.example.foodieorder;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

// 7. Edit Location
public class EditLocationActivity extends BaseActivity {

    EditText etLocation;
    WebView webMap;
    String restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);
        setupToolbar("Edit Location");

        restaurant = getIntent().getStringExtra("restaurant");
        if (restaurant == null) restaurant = AppData.RESTAURANTS[0];

        etLocation = findViewById(R.id.etLocation);
        webMap = findViewById(R.id.webMap);

        etLocation.setText(AppData.getLocation(this, restaurant));
        AppData.loadMap(webMap, etLocation.getText().toString());

        // press search on keyboard -> update map
        etLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                AppData.loadMap(webMap, etLocation.getText().toString());
                return true;
            }
            return false;
        });

        findViewById(R.id.btnCurrentLocation).setOnClickListener(v ->
                LocationHelper.getCurrentLocation(this, location -> {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    AppData.loadMap(webMap, lat + "," + lng);
                    LocationHelper.getAddress(this, lat, lng, address -> etLocation.setText(address));
                }));

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String loc = etLocation.getText().toString().trim();
            if (loc.isEmpty()) {
                etLocation.setError("Enter a location");
                return;
            }
            AppData.prefs(this).edit().putString("loc_" + restaurant, loc).apply();
            Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
