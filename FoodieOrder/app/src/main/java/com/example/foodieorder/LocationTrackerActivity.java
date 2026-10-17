package com.example.foodieorder;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

// 17. Google Location Tracker (current location + live tracking)
public class LocationTrackerActivity extends BaseActivity {

    WebView webMap;
    TextView tvLat, tvLng, tvAddress, tvStatus;
    SwitchCompat switchLive;
    FusedLocationProviderClient client;
    LocationCallback liveCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracker);
        setupToolbar("My Current Location");

        webMap = findViewById(R.id.webMap);
        tvLat = findViewById(R.id.tvLat);
        tvLng = findViewById(R.id.tvLng);
        tvAddress = findViewById(R.id.tvAddress);
        tvStatus = findViewById(R.id.tvStatus);
        switchLive = findViewById(R.id.switchLive);
        client = LocationServices.getFusedLocationProviderClient(this);

        AppData.loadMap(webMap, "Mumbai");

        findViewById(R.id.btnGetLocation).setOnClickListener(v ->
                LocationHelper.getCurrentLocation(this, this::showLocation));

        liveCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) showLocation(loc);
            }
        };

        switchLive.setOnCheckedChangeListener((b, checked) -> {
            if (checked) startLiveTracking();
            else stopLiveTracking();
        });
    }

    private void showLocation(Location loc) {
        double lat = loc.getLatitude();
        double lng = loc.getLongitude();
        tvLat.setText("Latitude: " + lat);
        tvLng.setText("Longitude: " + lng);
        AppData.loadMap(webMap, lat + "," + lng);
        LocationHelper.getAddress(this, lat, lng, address -> tvAddress.setText("Address: " + address));
        tvStatus.setText("✔ Location updated successfully!");
    }

    @SuppressLint("MissingPermission")
    private void startLiveTracking() {
        if (!LocationHelper.hasPermission(this)) {
            switchLive.setChecked(false);
            return;
        }
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        client.requestLocationUpdates(request, liveCallback, Looper.getMainLooper());
        Toast.makeText(this, "Live tracking started (every 5 sec)", Toast.LENGTH_SHORT).show();
    }

    private void stopLiveTracking() {
        client.removeLocationUpdates(liveCallback);
        tvStatus.setText("Live tracking stopped");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted. Tap again.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.removeLocationUpdates(liveCallback);
    }
}
