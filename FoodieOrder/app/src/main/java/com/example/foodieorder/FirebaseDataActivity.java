package com.example.foodieorder;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// 16. View Firebase Data - reads orders from Firebase Realtime Database
public class FirebaseDataActivity extends BaseActivity {

    ListView listFirebase;
    TextView tvInfo;
    ArrayList<String[]> data = new ArrayList<>(); // {id, item, restaurant, date, price, status}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_data);
        setupToolbar("Firebase Orders");

        listFirebase = findViewById(R.id.listFirebase);
        tvInfo = findViewById(R.id.tvInfo);
        loadData();
    }

    private void loadData() {
        tvInfo.setText("Loading...");
        FirebaseDatabase.getInstance().getReference("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        data.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            data.add(new String[]{
                                    s.getKey(),
                                    String.valueOf(s.child("item").getValue()),
                                    String.valueOf(s.child("restaurant").getValue()),
                                    String.valueOf(s.child("date").getValue()),
                                    String.valueOf(s.child("price").getValue()),
                                    String.valueOf(s.child("status").getValue())
                            });
                        }
                        tvInfo.setText(data.isEmpty() ? "No data in Firebase yet. Use 'Sync with Firebase' first." : data.size() + " orders in Firebase");
                        showList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvInfo.setText("Error: " + error.getMessage());
                    }
                });
    }

    private void showList() {
        listFirebase.setAdapter(new ArrayAdapter<String[]>(this, R.layout.item_firebase_order, data) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_firebase_order, parent, false);
                }
                String[] d = data.get(position);
                ((TextView) convertView.findViewById(R.id.tvFbId)).setText(d[0]);
                ((TextView) convertView.findViewById(R.id.tvFbItem)).setText(d[1]);
                ((TextView) convertView.findViewById(R.id.tvFbRestaurant)).setText(d[2] + "  •  " + d[3]);
                ((TextView) convertView.findViewById(R.id.tvFbPrice)).setText("₹" + d[4]);
                ((TextView) convertView.findViewById(R.id.tvFbStatus)).setText(d[5]);
                return convertView;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
