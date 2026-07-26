package com.fashiondesign.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String customerId;
    private double totalAmount;
    private String status; // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    private String orderDate;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public void placeOrder() {
        this.status = "PENDING";
    }

    public void cancelOrder() {
        this.status = "CANCELLED";
    }

    public void calculateTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.calculateTotalPrice();
        }
        this.totalAmount = total;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
