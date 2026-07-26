package com.fashiondesign.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private String cartId;
    private String customerId;
    private String createdDate;
    private List<CartItem> items = new ArrayList<>();

    public Cart() {
    }

    public void addItem(CartItem item) {
        items.add(item);
    }

    public void removeItem(String cartItemId) {
        items.removeIf(i -> i.getCartItemId().equals(cartItemId));
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
