package com.example.foodieorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

// SQLite database with two tables: restaurants and orders
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "foodie_order.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE restaurants (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, cuisine TEXT, location TEXT)");
        db.execSQL("CREATE TABLE orders (id INTEGER PRIMARY KEY AUTOINCREMENT, item TEXT, restaurant TEXT, price INTEGER, date TEXT, status TEXT)");

        // sample restaurants
        db.execSQL("INSERT INTO restaurants (name, cuisine, location) VALUES ('Domino''s', 'Italian', 'Bandra West')");
        db.execSQL("INSERT INTO restaurants (name, cuisine, location) VALUES ('Pizza Hut', 'Italian', 'Andheri')");
        db.execSQL("INSERT INTO restaurants (name, cuisine, location) VALUES ('Burger King', 'Fast Food', 'Malad')");
        db.execSQL("INSERT INTO restaurants (name, cuisine, location) VALUES ('Subway', 'Healthy', 'Borivali')");

        // sample orders (oldest first, list shows newest first)
        db.execSQL("INSERT INTO orders (item, restaurant, price, date, status) VALUES ('White Sauce Pasta', 'Oven Story Pizza', 299, '08 May 2024', 'Delivered')");
        db.execSQL("INSERT INTO orders (item, restaurant, price, date, status) VALUES ('Cheese Burger', 'Burger King', 199, '10 May 2024', 'Delivered')");
        db.execSQL("INSERT INTO orders (item, restaurant, price, date, status) VALUES ('Margherita Pizza', 'Domino''s Pizza', 249, '12 May 2024', 'Delivered')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS restaurants");
        db.execSQL("DROP TABLE IF EXISTS orders");
        onCreate(db);
    }

    // ---------------- Restaurants (CRUD) ----------------

    public long addRestaurant(String name, String cuisine, String location) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("cuisine", cuisine);
        cv.put("location", location);
        return getWritableDatabase().insert("restaurants", null, cv);
    }

    public int updateRestaurant(int id, String name, String cuisine, String location) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("cuisine", cuisine);
        cv.put("location", location);
        return getWritableDatabase().update("restaurants", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteRestaurant(int id) {
        return getWritableDatabase().delete("restaurants", "id=?", new String[]{String.valueOf(id)});
    }

    // each row: {id, name, cuisine, location}
    public ArrayList<String[]> getRestaurants() {
        ArrayList<String[]> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT id, name, cuisine, location FROM restaurants", null);
        while (c.moveToNext()) {
            list.add(new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3)});
        }
        c.close();
        return list;
    }

    // ---------------- Orders ----------------

    public long addOrder(String item, String restaurant, int price, String date, String status) {
        ContentValues cv = new ContentValues();
        cv.put("item", item);
        cv.put("restaurant", restaurant);
        cv.put("price", price);
        cv.put("date", date);
        cv.put("status", status);
        return getWritableDatabase().insert("orders", null, cv);
    }

    public void updateOrderStatus(int id, String status) {
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        getWritableDatabase().update("orders", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public ArrayList<Order> getOrders() {
        ArrayList<Order> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT id, item, restaurant, price, date, status FROM orders ORDER BY id DESC", null);
        while (c.moveToNext()) {
            list.add(new Order(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }
}
