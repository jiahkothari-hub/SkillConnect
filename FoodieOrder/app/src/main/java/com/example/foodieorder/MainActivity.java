package com.example.foodieorder;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

// 4. Main Screen (Home)
// 5. Options Menu (Toolbar) + Navigation Drawer
// 6. Popup Menu (More Options)
// 20. Share Restaurant
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navView;
    Spinner spRestaurant, spFood;
    TextView tvLocation, tvDots;
    ImageView ivFood;
    int imageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Foodie Order");

        // navigation drawer
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        spRestaurant = findViewById(R.id.spRestaurant);
        spFood = findViewById(R.id.spFood);
        tvLocation = findViewById(R.id.tvLocation);
        tvDots = findViewById(R.id.tvDots);
        ivFood = findViewById(R.id.ivFood);

        // spinners
        ArrayAdapter<String> rAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, AppData.RESTAURANTS);
        rAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRestaurant.setAdapter(rAdapter);

        ArrayAdapter<String> fAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, AppData.FOODS);
        fAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFood.setAdapter(fAdapter);

        spRestaurant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tvLocation.setText(AppData.getLocation(MainActivity.this, getRestaurant()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spFood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                imageIndex = position;
                showImage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // image slider buttons
        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            imageIndex = (imageIndex - 1 + AppData.FOOD_IMAGES.length) % AppData.FOOD_IMAGES.length;
            spFood.setSelection(imageIndex);
            showImage();
        });
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            imageIndex = (imageIndex + 1) % AppData.FOOD_IMAGES.length;
            spFood.setSelection(imageIndex);
            showImage();
        });

        // "Our Menu" list
        LinearLayout menuContainer = findViewById(R.id.menuContainer);
        for (int i = 0; i < AppData.FOODS.length; i++) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_menu_food, menuContainer, false);
            ((ImageView) row.findViewById(R.id.ivItem)).setImageResource(AppData.FOOD_IMAGES[i]);
            ((TextView) row.findViewById(R.id.tvItemName)).setText(AppData.FOODS[i]);
            ((TextView) row.findViewById(R.id.tvItemPrice)).setText("₹" + AppData.PRICES[i]);
            String food = AppData.FOODS[i];
            row.findViewById(R.id.btnAdd).setOnClickListener(v -> addToCart(food));
            menuContainer.addView(row);
        }

        findViewById(R.id.btnEditLocation).setOnClickListener(v -> {
            Intent i = new Intent(this, EditLocationActivity.class);
            i.putExtra("restaurant", getRestaurant());
            startActivity(i);
        });

        Button btnWebsite = findViewById(R.id.btnWebsite);
        btnWebsite.setOnClickListener(v -> {
            Intent i = new Intent(this, WebsiteActivity.class);
            i.putExtra("url", AppData.WEBSITES[spRestaurant.getSelectedItemPosition()]);
            startActivity(i);
        });

        findViewById(R.id.btnMoreOptions).setOnClickListener(this::showPopupMenu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh location (may be changed in Edit Location) and drawer header (may be changed in Profile)
        tvLocation.setText(AppData.getLocation(this, getRestaurant()));
        View header = navView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.tvHeaderName)).setText(AppData.prefs(this).getString("user_name", "User"));
        ((TextView) header.findViewById(R.id.tvHeaderEmail)).setText(AppData.prefs(this).getString("user_email", ""));
        navView.setCheckedItem(R.id.nav_home);
    }

    private String getRestaurant() {
        return spRestaurant.getSelectedItem().toString();
    }

    private String getFood() {
        return spFood.getSelectedItem().toString();
    }

    private void showImage() {
        ivFood.setImageResource(AppData.FOOD_IMAGES[imageIndex]);
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < AppData.FOOD_IMAGES.length; i++) {
            dots.append(i == imageIndex ? "● " : "○ ");
        }
        tvDots.setText(dots.toString().trim());
    }

    private void addToCart(String food) {
        AppData.cart.add(food);
        Toast.makeText(this, food + " added to cart (" + AppData.cart.size() + " items)", Toast.LENGTH_SHORT).show();
    }

    // ---------------- 6. Popup Menu ----------------

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popup.setForceShowIcon(true);
        }
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.pop_cart) {
                addToCart(getFood());
            } else if (id == R.id.pop_buy) {
                buyNow();
            } else if (id == R.id.pop_wishlist) {
                String list = AppData.prefs(this).getString("wishlist", "");
                AppData.prefs(this).edit().putString("wishlist", list + getFood() + "\n").apply();
                Toast.makeText(this, getFood() + " added to Wishlist", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.pop_details) {
                showRestaurantDetails();
            } else if (id == R.id.pop_file) {
                chooseFileType();
            } else if (id == R.id.pop_map) {
                Intent i = new Intent(this, LiveMapActivity.class);
                i.putExtra("restaurant", getRestaurant());
                i.putExtra("location", tvLocation.getText().toString());
                startActivity(i);
            }
            return true;
        });
        popup.show();
    }

    private void buyNow() {
        String food = getFood();
        String restaurant = getRestaurant();
        new AlertDialog.Builder(this)
                .setTitle("Buy Now")
                .setMessage("Order " + food + " from " + restaurant + " for ₹" + AppData.getPrice(food) + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Place Order", (d, w) -> {
                    AppData.placeOrder(this, food, restaurant);
                    Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MyOrdersActivity.class));
                })
                .show();
    }

    private void showRestaurantDetails() {
        int i = spRestaurant.getSelectedItemPosition();
        new AlertDialog.Builder(this)
                .setTitle(AppData.RESTAURANTS[i])
                .setMessage("Cuisine: " + AppData.CUISINES[i]
                        + "\nLocation: " + tvLocation.getText()
                        + "\nWebsite: " + AppData.WEBSITES[i]
                        + "\nTimings: 10:00 AM - 11:00 PM"
                        + "\nRating: 4.3 ★")
                .setPositiveButton("OK", null)
                .show();
    }

    private void chooseFileType() {
        String[] options = {"TXT File", "CSV File"};
        new AlertDialog.Builder(this)
                .setTitle("File Handling")
                .setItems(options, (d, which) -> {
                    if (which == 0) startActivity(new Intent(this, TxtFileActivity.class));
                    else startActivity(new Intent(this, CsvFileActivity.class));
                })
                .show();
    }

    private void showCart() {
        if (AppData.cart.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        int total = 0;
        for (String food : AppData.cart) {
            sb.append(food).append("  -  ₹").append(AppData.getPrice(food)).append("\n");
            total += AppData.getPrice(food);
        }
        sb.append("\nRestaurant: ").append(getRestaurant());
        sb.append("\nTotal: ₹").append(total);

        new AlertDialog.Builder(this)
                .setTitle("My Cart (" + AppData.cart.size() + ")")
                .setMessage(sb.toString())
                .setPositiveButton("Place Order", (d, w) -> {
                    for (String food : AppData.cart) {
                        AppData.placeOrder(this, food, getRestaurant());
                    }
                    AppData.cart.clear();
                    Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MyOrdersActivity.class));
                })
                .setNeutralButton("Clear Cart", (d, w) -> AppData.cart.clear())
                .setNegativeButton("Close", null)
                .show();
    }

    private void shareRestaurant() {
        int i = spRestaurant.getSelectedItemPosition();
        AppData.share(this, "Check out " + AppData.RESTAURANTS[i] + " (" + tvLocation.getText()
                + ") on Foodie Order! Try the " + getFood() + " for just ₹" + AppData.getPrice(getFood())
                + ".\n" + AppData.WEBSITES[i]);
    }

    // ---------------- 5. Options Menu (Toolbar) ----------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cart) {
            showCart();
        } else if (id == R.id.action_share) {
            shareRestaurant();
        } else if (id == R.id.action_orders) {
            startActivity(new Intent(this, MyOrdersActivity.class));
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.action_logout) {
            AppData.showLogoutDialog(this);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // ---------------- Navigation Drawer ----------------

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_orders) {
            startActivity(new Intent(this, MyOrdersActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_db) {
            startActivity(new Intent(this, RestaurantDbActivity.class));
        } else if (id == R.id.nav_files) {
            chooseFileType();
        } else if (id == R.id.nav_location) {
            startActivity(new Intent(this, LocationTrackerActivity.class));
        } else if (id == R.id.nav_firebase) {
            startActivity(new Intent(this, FirebaseAuthActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            AppData.showLogoutDialog(this);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
