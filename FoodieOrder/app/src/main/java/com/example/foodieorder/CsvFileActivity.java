package com.example.foodieorder;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

// 13. File Handling (CSV) - export orders to a CSV file and read it back
public class CsvFileActivity extends BaseActivity {

    TableLayout tablePreview;
    TextView tvPath;
    View layoutSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csv_file);
        setupToolbar("Save Orders to CSV");

        tablePreview = findViewById(R.id.tablePreview);
        tvPath = findViewById(R.id.tvPath);
        layoutSuccess = findViewById(R.id.layoutSuccess);

        findViewById(R.id.btnExport).setOnClickListener(v -> exportCsv());
        findViewById(R.id.btnViewCsv).setOnClickListener(v -> viewCsv());

        if (AppData.getCsvFile(this).exists()) showPreview();
    }

    private void exportCsv() {
        File file = AppData.getCsvFile(this);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write("Date,Restaurant,Item,Price,Status\n");
            ArrayList<Order> orders = new DBHelper(this).getOrders();
            for (Order o : orders) {
                writer.write(o.date + "," + o.restaurant + "," + o.item + "," + o.price + "," + o.status + "\n");
            }
            writer.close();
            layoutSuccess.setVisibility(View.VISIBLE);
            showPreview();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // reads the CSV file and shows it as a table
    private void showPreview() {
        tablePreview.removeAllViews();
        File file = AppData.getCsvFile(this);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                TableRow row = new TableRow(this);
                for (String cell : line.split(",")) {
                    TextView tv = new TextView(this);
                    tv.setText(cell);
                    tv.setPadding(6, 8, 6, 8);
                    tv.setTextSize(11);
                    if (header) tv.setTypeface(null, Typeface.BOLD);
                    row.addView(tv);
                }
                tablePreview.addView(row);
                header = false;
            }
            reader.close();
            tvPath.setText("File saved in: " + file.getAbsolutePath());
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // shows the raw CSV text
    private void viewCsv() {
        File file = AppData.getCsvFile(this);
        if (!file.exists()) {
            Toast.makeText(this, "Export the CSV first", Toast.LENGTH_SHORT).show();
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
            new AlertDialog.Builder(this)
                    .setTitle("orders.csv")
                    .setMessage(sb.toString())
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
