package com.example.googlemapexp8;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    EditText etLatitude, etLongitude;
    Button btnLocate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        btnLocate = findViewById(R.id.btnLocate);

        // load the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnLocate.setOnClickListener(v -> {
            String latText = etLatitude.getText().toString().trim();
            String lngText = etLongitude.getText().toString().trim();

            if (latText.isEmpty() || lngText.isEmpty()) {
                Toast.makeText(this, "Please enter latitude and longitude", Toast.LENGTH_SHORT).show();
                return;
            }

            double lat, lng;
            try {
                lat = Double.parseDouble(latText);
                lng = Double.parseDouble(lngText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                Toast.makeText(this, "Latitude: -90 to 90, Longitude: -180 to 180", Toast.LENGTH_SHORT).show();
                return;
            }

            showLocation(lat, lng, "Selected Location");
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // default marker: Indore
        showLocation(22.7196, 75.8577, "Indore");
    }

    // puts a marker on the given latitude and longitude and moves the camera there
    private void showLocation(double lat, double lng, String title) {
        if (mMap == null) {
            Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }
        LatLng location = new LatLng(lat, lng);
        mMap.clear();
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .snippet("Lat: " + lat + ", Lng: " + lng));
        if (marker != null) marker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
    }
}
