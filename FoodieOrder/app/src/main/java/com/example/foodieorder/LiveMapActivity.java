package com.example.foodieorder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

// 18. Live Location on Map - restaurant on Google Map + route
public class LiveMapActivity extends BaseActivity {

    WebView webMap;
    String restaurant, location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);
        setupToolbar("Track Restaurant Location");

        restaurant = getIntent().getStringExtra("restaurant");
        location = getIntent().getStringExtra("location");
        if (restaurant == null) restaurant = AppData.RESTAURANTS[0];
        if (location == null) location = AppData.getLocation(this, restaurant);

        ((TextView) findViewById(R.id.tvRestaurant)).setText(restaurant);
        ((TextView) findViewById(R.id.tvAddress)).setText(location);

        webMap = findViewById(R.id.webMap);
        AppData.loadMap(webMap, restaurant + ", " + location);

        // show my position on the same map
        findViewById(R.id.btnMyLocation).setOnClickListener(v ->
                LocationHelper.getCurrentLocation(this, loc ->
                        AppData.loadMap(webMap, loc.getLatitude() + "," + loc.getLongitude())));

        // open Google Maps with directions to the restaurant
        findViewById(R.id.btnRoute).setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                    + Uri.encode(restaurant + ", " + location));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });
    }
}
