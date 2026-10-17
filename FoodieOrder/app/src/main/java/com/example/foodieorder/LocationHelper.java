package com.example.foodieorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;
import java.util.Locale;

// Google Fused Location Provider helper
public class LocationHelper {

    public static final int RC_LOCATION = 200;

    public interface LocationCallback {
        void onLocation(Location location);
    }

    public interface AddressCallback {
        void onAddress(String address);
    }

    // returns true if permission is already given, otherwise asks the user
    public static boolean hasPermission(Activity a) {
        if (ContextCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(a, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, RC_LOCATION);
        return false;
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Activity a, LocationCallback callback) {
        if (!hasPermission(a)) return;
        Toast.makeText(a, "Getting location...", Toast.LENGTH_SHORT).show();
        LocationServices.getFusedLocationProviderClient(a)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocation(location);
                    } else {
                        Toast.makeText(a, "Location not available. Turn on GPS / set location in emulator.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(a, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // converts latitude/longitude into a readable address (runs in background thread)
    public static void getAddress(Activity a, double lat, double lng, AddressCallback callback) {
        new Thread(() -> {
            String result = String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng);
            try {
                Geocoder geocoder = new Geocoder(a, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(lat, lng, 1);
                if (list != null && !list.isEmpty()) {
                    result = list.get(0).getAddressLine(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String finalResult = result;
            a.runOnUiThread(() -> callback.onAddress(finalResult));
        }).start();
    }
}
