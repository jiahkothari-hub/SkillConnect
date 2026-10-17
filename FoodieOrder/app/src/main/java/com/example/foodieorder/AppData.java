package com.example.foodieorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

// Common data + small helper methods used by many screens
public class AppData {

    public static final String PREFS = "FoodieOrderPrefs";

    public static final String[] RESTAURANTS = {"Domino's Pizza", "Pizza Hut", "Burger King", "Subway", "Oven Story Pizza"};
    public static final String[] LOCATIONS = {"Bandra West, Mumbai", "Andheri, Mumbai", "Malad, Mumbai", "Borivali, Mumbai", "Juhu, Mumbai"};
    public static final String[] CUISINES = {"Italian", "Italian", "Fast Food", "Healthy", "Italian"};
    public static final String[] WEBSITES = {"https://www.dominos.co.in", "https://www.pizzahut.co.in",
            "https://www.burgerking.in", "https://www.subway.com/en-IN", "https://www.ovenstory.in"};

    public static final String[] FOODS = {"Margherita Pizza", "Farmhouse Pizza", "Cheese Burger", "White Sauce Pasta"};
    public static final int[] PRICES = {249, 249, 199, 299};
    public static final int[] FOOD_IMAGES = {R.drawable.pizza, R.drawable.farmhouse_pizza, R.drawable.burger, R.drawable.pasta};

    // shopping cart (food names)
    public static ArrayList<String> cart = new ArrayList<>();

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int indexOf(String[] arr, String value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(value)) return i;
        }
        return 0;
    }

    public static int getFoodImage(String item) {
        return FOOD_IMAGES[indexOf(FOODS, item)];
    }

    public static int getPrice(String item) {
        return PRICES[indexOf(FOODS, item)];
    }

    // location saved by the user (Edit Location) or the default one
    public static String getLocation(Context c, String restaurant) {
        String def = LOCATIONS[indexOf(RESTAURANTS, restaurant)];
        return prefs(c).getString("loc_" + restaurant, def);
    }

    public static String today() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
    }

    // ---------------- Files ----------------

    public static File getTxtFile(Context c) {
        File dir = new File(c.getExternalFilesDir(null), "FoodieOrder");
        dir.mkdirs();
        return new File(dir, "orders.txt");
    }

    public static File getCsvFile(Context c) {
        File dir = new File(c.getExternalFilesDir(null), "FoodieOrder");
        dir.mkdirs();
        return new File(dir, "orders.csv");
    }

    public static String orderText(String item, String restaurant, int price, String date, String status) {
        return "Order Details\n"
                + "--------------------\n"
                + "Date: " + date + "\n"
                + "Restaurant: " + restaurant + "\n"
                + "Item: " + item + "\n"
                + "Price: ₹" + price + "\n"
                + "Status: " + status + "\n"
                + "--------------------\n";
    }

    // ---------------- Place an order ----------------

    public static void placeOrder(Context c, String item, String restaurant) {
        int price = getPrice(item);
        String date = today();
        long id = new DBHelper(c).addOrder(item, restaurant, price, date, "Placed");

        // Settings -> Auto Backup to File
        if (prefs(c).getBoolean("auto_backup", false)) {
            try {
                FileWriter fw = new FileWriter(getTxtFile(c), true); // append
                fw.write(orderText(item, restaurant, price, date, "Placed") + "\n");
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Settings -> Sync with Firebase
        if (prefs(c).getBoolean("sync_firebase", true)) {
            uploadToFirebase("ORD" + id, item, restaurant, price, date, "Placed");
        }
    }

    public static void uploadToFirebase(String orderId, String item, String restaurant, int price, String date, String status) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("item", item);
            map.put("restaurant", restaurant);
            map.put("price", String.valueOf(price));
            map.put("date", date);
            map.put("status", status);
            FirebaseDatabase.getInstance().getReference("orders").child(orderId).setValue(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- Map (Google Maps inside a WebView, no API key needed) ----------------

    public static void loadMap(WebView webView, String query) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://maps.google.com/maps?q=" + Uri.encode(query) + "&z=15&output=embed");
    }

    // ---------------- Share ----------------

    public static void share(Activity a, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        a.startActivity(Intent.createChooser(intent, "Share using"));
    }

    // ---------------- Logout ----------------

    public static void showLogoutDialog(Activity a) {
        new AlertDialog.Builder(a)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Logout", (d, w) -> {
                    FirebaseAuth.getInstance().signOut();
                    GoogleSignIn.getClient(a, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                    prefs(a).edit().putBoolean("logged_in", false).apply();
                    cart.clear();
                    Intent i = new Intent(a, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    a.startActivity(i);
                    a.finish();
                })
                .show();
    }
}
