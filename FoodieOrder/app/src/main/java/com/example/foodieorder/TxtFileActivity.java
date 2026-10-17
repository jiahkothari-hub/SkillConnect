package com.example.foodieorder;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

// 12. File Handling (TXT) - write and read a text file
public class TxtFileActivity extends BaseActivity {

    EditText etOrderText;
    TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txt_file);
        setupToolbar("Save Orders to TXT");

        etOrderText = findViewById(R.id.etOrderText);
        tvStatus = findViewById(R.id.tvStatus);

        // fill with the latest order
        ArrayList<Order> orders = new DBHelper(this).getOrders();
        if (!orders.isEmpty()) {
            Order o = orders.get(0);
            etOrderText.setText(AppData.orderText(o.item, o.restaurant, o.price, o.date, o.status));
        }

        findViewById(R.id.btnSaveTxt).setOnClickListener(v -> saveTxt());
        findViewById(R.id.btnReadTxt).setOnClickListener(v -> readTxt());
    }

    private void saveTxt() {
        File file = AppData.getTxtFile(this);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(etOrderText.getText().toString());
            writer.close();
            showStatus("Order saved to " + file.getAbsolutePath());
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void readTxt() {
        File file = AppData.getTxtFile(this);
        if (!file.exists()) {
            Toast.makeText(this, "File not found. Save it first.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            etOrderText.setText(sb.toString());
            showStatus("Read from " + file.getAbsolutePath());
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showStatus(String msg) {
        tvStatus.setText(msg);
        tvStatus.setVisibility(View.VISIBLE);
    }
}
