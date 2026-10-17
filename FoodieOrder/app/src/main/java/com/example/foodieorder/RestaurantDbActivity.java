package com.example.foodieorder;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;

// 11. SQLite Database (CRUD) - Add, Read (list), Update, Delete restaurants
public class RestaurantDbActivity extends BaseActivity {

    EditText etName, etCuisine, etLocation;
    ListView listRestaurants;
    DBHelper db;
    ArrayList<String[]> rows = new ArrayList<>();
    int selectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_db);
        setupToolbar("Restaurant Database");

        db = new DBHelper(this);
        etName = findViewById(R.id.etName);
        etCuisine = findViewById(R.id.etCuisine);
        etLocation = findViewById(R.id.etLocation);
        listRestaurants = findViewById(R.id.listRestaurants);

        // CREATE
        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            if (!validate()) return;
            db.addRestaurant(text(etName), text(etCuisine), text(etLocation));
            Toast.makeText(this, "Restaurant added", Toast.LENGTH_SHORT).show();
            clearFields();
            loadData();
        });

        // UPDATE
        findViewById(R.id.btnUpdate).setOnClickListener(v -> {
            if (selectedId == -1) {
                Toast.makeText(this, "Tap a row in the table first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validate()) return;
            db.updateRestaurant(selectedId, text(etName), text(etCuisine), text(etLocation));
            Toast.makeText(this, "Restaurant updated", Toast.LENGTH_SHORT).show();
            clearFields();
            loadData();
        });

        // DELETE
        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (selectedId == -1) {
                Toast.makeText(this, "Tap a row in the table first", Toast.LENGTH_SHORT).show();
                return;
            }
            db.deleteRestaurant(selectedId);
            Toast.makeText(this, "Restaurant deleted", Toast.LENGTH_SHORT).show();
            clearFields();
            loadData();
        });

        // select a row
        listRestaurants.setOnItemClickListener((parent, view, position, id) -> {
            String[] r = rows.get(position);
            selectedId = Integer.parseInt(r[0]);
            etName.setText(r[1]);
            etCuisine.setText(r[2]);
            etLocation.setText(r[3]);
            Toast.makeText(this, "Selected ID " + selectedId, Toast.LENGTH_SHORT).show();
        });

        loadData();
    }

    // READ
    private void loadData() {
        rows = db.getRestaurants();
        listRestaurants.setAdapter(new ArrayAdapter<String[]>(this, R.layout.item_restaurant, rows) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_restaurant, parent, false);
                }
                String[] r = rows.get(position);
                ((TextView) convertView.findViewById(R.id.tvId)).setText(r[0]);
                ((TextView) convertView.findViewById(R.id.tvName)).setText(r[1]);
                ((TextView) convertView.findViewById(R.id.tvCuisine)).setText(r[2]);
                ((TextView) convertView.findViewById(R.id.tvLocation)).setText(r[3]);
                return convertView;
            }
        });
    }

    private boolean validate() {
        if (text(etName).isEmpty() || text(etCuisine).isEmpty() || text(etLocation).isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String text(EditText et) {
        return et.getText().toString().trim();
    }

    private void clearFields() {
        etName.setText("");
        etCuisine.setText("");
        etLocation.setText("");
        selectedId = -1;
    }
}
