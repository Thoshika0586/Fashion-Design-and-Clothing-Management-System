package com.fashiondesign.model;

public class Inventory {
    private String inventoryId;
    private String itemType;
    private String itemName;
    private int quantity;
    private double unitPrice;

    public Inventory() {
    }

    public void addItem() { /* persistence handled by InventoryDAO */ }

    public void deleteItem() { /* persistence handled by InventoryDAO */ }

    public void updateStock(int newQuantity) {
        this.quantity = newQuantity;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
