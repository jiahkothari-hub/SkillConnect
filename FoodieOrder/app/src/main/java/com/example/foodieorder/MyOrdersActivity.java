package com.example.foodieorder;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

// 9. My Orders  +  10. Order Options (Context Menu - long press)
public class MyOrdersActivity extends BaseActivity {

    ListView listOrders;
    TextView tvTotalOrders, tvTotalAmount, tvEmpty;
    DBHelper db;
    ArrayList<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);
        setupToolbar("My Orders");

        db = new DBHelper(this);
        listOrders = findViewById(R.id.listOrders);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvEmpty = findViewById(R.id.tvEmpty);

        registerForContextMenu(listOrders);
        listOrders.setOnItemClickListener((parent, view, position, id) -> showDetails(orders.get(position)));
        loadOrders();
    }

    private void loadOrders() {
        orders = db.getOrders();
        int total = 0;
        for (Order o : orders) {
            if (!o.status.equals("Cancelled")) total += o.price;
        }
        tvTotalOrders.setText("Total Orders: " + orders.size());
        tvTotalAmount.setText("Total Amount: ₹" + total);
        tvEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);

        listOrders.setAdapter(new ArrayAdapter<Order>(this, R.layout.item_order, orders) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_order, parent, false);
                }
                Order o = orders.get(position);
                ((ImageView) convertView.findViewById(R.id.ivOrder)).setImageResource(AppData.getFoodImage(o.item));
                ((TextView) convertView.findViewById(R.id.tvOrderItem)).setText(o.item);
                ((TextView) convertView.findViewById(R.id.tvOrderRestaurant)).setText(o.restaurant);
                ((TextView) convertView.findViewById(R.id.tvOrderPrice)).setText("₹" + o.price);
                ((TextView) convertView.findViewById(R.id.tvOrderDate)).setText(o.date);
                TextView tvStatus = convertView.findViewById(R.id.tvOrderStatus);
                tvStatus.setText(o.status);
                tvStatus.setTextColor(getResources().getColor(o.status.equals("Cancelled") ? R.color.red : R.color.green));
                return convertView;
            }
        });
    }

    private void showDetails(Order o) {
        new AlertDialog.Builder(this)
                .setTitle("Order #" + o.id)
                .setMessage(AppData.orderText(o.item, o.restaurant, o.price, o.date, o.status))
                .setPositiveButton("OK", null)
                .show();
    }

    // ---------------- Context Menu ----------------

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_order_menu, menu);
        menu.setHeaderTitle("Order Options");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Order o = orders.get(info.position);
        int id = item.getItemId();

        if (id == R.id.ctx_reorder) {
            AppData.placeOrder(this, o.item, o.restaurant);
            Toast.makeText(this, o.item + " reordered", Toast.LENGTH_SHORT).show();
            loadOrders();
        } else if (id == R.id.ctx_details) {
            showDetails(o);
        } else if (id == R.id.ctx_cancel) {
            if (o.status.equals("Delivered")) {
                Toast.makeText(this, "Delivered orders cannot be cancelled", Toast.LENGTH_SHORT).show();
            } else {
                db.updateOrderStatus(o.id, "Cancelled");
                Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show();
                loadOrders();
            }
        } else if (id == R.id.ctx_share) {
            AppData.share(this, "My Foodie Order:\n" + AppData.orderText(o.item, o.restaurant, o.price, o.date, o.status));
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
    }
}
