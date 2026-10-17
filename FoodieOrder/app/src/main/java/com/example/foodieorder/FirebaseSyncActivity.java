package com.example.foodieorder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

// 15. Firebase Realtime Database - save (sync) an order
public class FirebaseSyncActivity extends BaseActivity {

    EditText etOrderId, etRestaurant, etItem, etPrice;
    Spinner spStatus;
    View layoutSuccess;
    TextView tvResult;
    boolean done;

    String[] statuses = {"Placed", "Preparing", "Out for Delivery", "Delivered", "Cancelled"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_sync);
        setupToolbar("Sync with Firebase");

        etOrderId = findViewById(R.id.etOrderId);
        etRestaurant = findViewById(R.id.etRestaurant);
        etItem = findViewById(R.id.etItem);
        etPrice = findViewById(R.id.etPrice);
        spStatus = findViewById(R.id.spStatus);
        layoutSuccess = findViewById(R.id.layoutSuccess);
        tvResult = findViewById(R.id.tvResult);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(adapter);

        // sample values
        etOrderId.setText("ORD123");
        etRestaurant.setText("Domino's Pizza");
        etItem.setText("Margherita Pizza");
        etPrice.setText("249");
        spStatus.setSelection(3);

        findViewById(R.id.btnSaveFirebase).setOnClickListener(v -> saveOrder());
        findViewById(R.id.btnSyncAll).setOnClickListener(v -> syncAll());
    }

    private void saveOrder() {
        String id = etOrderId.getText().toString().trim();
        if (id.isEmpty() || etItem.getText().toString().trim().isEmpty()) {
            etOrderId.setError("Order ID and Item are required");
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", id);
        map.put("restaurant", etRestaurant.getText().toString().trim());
        map.put("item", etItem.getText().toString().trim());
        map.put("price", etPrice.getText().toString().trim());
        map.put("status", spStatus.getSelectedItem().toString());
        map.put("date", AppData.today());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("orders");
        done = false;
        ref.child(id).setValue(map).addOnCompleteListener(task -> {
            done = true;
            if (task.isSuccessful()) {
                showResult("Data saved to Firebase Realtime Database!");
            } else {
                showResult("Failed: " + task.getException().getMessage());
            }
        });

        // if there is no internet / Firebase is not set up, Firebase keeps the data offline
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!done) showResult("Saved offline - will sync to Firebase when connected.");
        }, 4000);
    }

    private void syncAll() {
        ArrayList<Order> orders = new DBHelper(this).getOrders();
        for (Order o : orders) {
            AppData.uploadToFirebase("ORD" + o.id, o.item, o.restaurant, o.price, o.date, o.status);
        }
        showResult(orders.size() + " orders sent to Firebase Realtime Database!");
    }

    private void showResult(String msg) {
        tvResult.setText(msg);
        layoutSuccess.setVisibility(View.VISIBLE);
    }
}
