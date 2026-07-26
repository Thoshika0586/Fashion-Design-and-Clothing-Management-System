package com.fashiondesign.dao;

import com.fashiondesign.util.IdGenerator;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class CartDAO {

    /** Gets the customer's open cart, creating one if it doesn't exist. */
    private String getOrCreateCartId(Connection con, String customerId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT cart_id FROM carts WHERE customer_id=? ORDER BY created_date DESC LIMIT 1")) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("cart_id");
            }
        }
        String cartId = IdGenerator.generate("CART");
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO carts (cart_id, customer_id, created_date) VALUES (?,?,?)")) {
            ps.setString(1, cartId);
            ps.setString(2, customerId);
            ps.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
        }
        return cartId;
    }

    public void addItem(String customerId, String designId, String size, int quantity, String customNote) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            String cartId = getOrCreateCartId(con, customerId);
            String cartItemId = IdGenerator.generate("CI");
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cart_items (cart_item_id, cart_id, design_id, size, quantity, custom_note) VALUES (?,?,?,?,?,?)")) {
                ps.setString(1, cartItemId);
                ps.setString(2, cartId);
                ps.setString(3, designId);
                ps.setString(4, size);
                ps.setInt(5, quantity);
                ps.setString(6, customNote);
                ps.executeUpdate();
            }
        }
    }

    public void removeItem(String cartItemId) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM cart_items WHERE cart_item_id=?")) {
            ps.setString(1, cartItemId);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> getCartItems(String customerId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT ci.*, d.design_name, d.category, d.price, d.sketch_image FROM cart_items ci " +
                "JOIN carts c ON ci.cart_id = c.cart_id " +
                "JOIN designs d ON ci.design_id = d.design_id " +
                "WHERE c.customer_id = ? ORDER BY ci.cart_item_id";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("cartItemId", rs.getString("cart_item_id"));
                    m.put("cartId", rs.getString("cart_id"));
                    m.put("designId", rs.getString("design_id"));
                    m.put("designName", rs.getString("design_name"));
                    m.put("category", rs.getString("category"));
                    m.put("sketchImage", rs.getString("sketch_image"));
                    m.put("size", rs.getString("size"));
                    m.put("quantity", rs.getInt("quantity"));
                    m.put("unitPrice", rs.getDouble("price"));
                    m.put("customNote", rs.getString("custom_note"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    public void clearCart(String customerId) throws SQLException {
        String sql = "DELETE ci FROM cart_items ci JOIN carts c ON ci.cart_id = c.cart_id WHERE c.customer_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ps.executeUpdate();
        }
    }
}
