package com.example.foodieorder;

// Simple model class for an order
public class Order {
    public int id;
    public String item;
    public String restaurant;
    public int price;
    public String date;
    public String status;

    public Order(int id, String item, String restaurant, int price, String date, String status) {
        this.id = id;
        this.item = item;
        this.restaurant = restaurant;
        this.price = price;
        this.date = date;
        this.status = status;
    }
}
