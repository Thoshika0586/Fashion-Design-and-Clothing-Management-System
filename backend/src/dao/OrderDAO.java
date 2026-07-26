package com.fashiondesign.dao;

import com.fashiondesign.util.IdGenerator;

import java.sql.*;
import java.util.*;

public class OrderDAO {

    /** Places an order from the customer's current cart items and clears the cart. */
    public Map<String, Object> placeOrderFromCart(String customerId) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                List<Map<String, Object>> cartItems = new ArrayList<>();
                String cartSql = "SELECT ci.*, d.price FROM cart_items ci " +
                        "JOIN carts c ON ci.cart_id = c.cart_id JOIN designs d ON ci.design_id = d.design_id " +
                        "WHERE c.customer_id = ?";
                try (PreparedStatement ps = con.prepareStatement(cartSql)) {
                    ps.setString(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> m = new HashMap<>();
                            m.put("designId", rs.getString("design_id"));
                            m.put("size", rs.getString("size"));
                            m.put("quantity", rs.getInt("quantity"));
                            m.put("price", rs.getDouble("price"));
                            m.put("customNote", rs.getString("custom_note"));
                            cartItems.add(m);
                        }
                    }
                }

                if (cartItems.isEmpty()) {
                    con.rollback();
                    return null;
                }

                String orderId = IdGenerator.generate("ORD");
                double total = 0;
                for (Map<String, Object> item : cartItems) {
                    total += (double) item.get("price") * (int) item.get("quantity");
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO orders (order_id, customer_id, total_amount, status) VALUES (?,?,?,'PENDING')")) {
                    ps.setString(1, orderId);
                    ps.setString(2, customerId);
                    ps.setDouble(3, total);
                    ps.executeUpdate();
                }

                for (Map<String, Object> item : cartItems) {
                    String orderItemId = IdGenerator.generate("OI");
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO order_items (order_item_id, order_id, design_id, size, quantity, unit_price, custom_note) VALUES (?,?,?,?,?,?,?)")) {
                        ps.setString(1, orderItemId);
                        ps.setString(2, orderId);
                        ps.setString(3, (String) item.get("designId"));
                        ps.setString(4, (String) item.get("size"));
                        ps.setInt(5, (int) item.get("quantity"));
                        ps.setDouble(6, (double) item.get("price"));
                        ps.setString(7, (String) item.get("customNote"));
                        ps.executeUpdate();
                    }
                }

                // Create a delivery record shell tied to the customer's saved address.
                String deliveryId = IdGenerator.generate("DEL");
                String address = "";
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT u.address FROM users u JOIN customers c ON u.user_id = c.user_id WHERE c.customer_id = ?")) {
                    ps.setString(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) address = rs.getString("address");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO deliveries (delivery_id, order_id, address, status) VALUES (?,?,?,'PREPARING')")) {
                    ps.setString(1, deliveryId);
                    ps.setString(2, orderId);
                    ps.setString(3, address);
                    ps.executeUpdate();
                }

                // Clear the cart
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE ci FROM cart_items ci JOIN carts c ON ci.cart_id = c.cart_id WHERE c.customer_id = ?")) {
                    ps.setString(1, customerId);
                    ps.executeUpdate();
                }

                con.commit();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("orderId", orderId);
                result.put("totalAmount", total);
                result.put("status", "PENDING");
                return result;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Map<String, Object>> getOrdersForCustomer(String customerId) throws SQLException {
        return getOrders("WHERE o.customer_id = ?", customerId);
    }

    /** Assembles a full printable bill: order, line items, customer info, payment, and delivery. */
    public Map<String, Object> getBill(String orderId) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            Map<String, Object> bill = new LinkedHashMap<>();
            String orderSql = "SELECT o.*, u.name AS customer_name, u.email AS customer_email, " +
                    "u.phone AS customer_phone, u.address AS customer_address FROM orders o " +
                    "JOIN customers c ON o.customer_id = c.customer_id " +
                    "JOIN users u ON c.user_id = u.user_id WHERE o.order_id = ?";
            try (PreparedStatement ps = con.prepareStatement(orderSql)) {
                ps.setString(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    bill.put("orderId", rs.getString("order_id"));
                    bill.put("customerName", rs.getString("customer_name"));
                    bill.put("customerEmail", rs.getString("customer_email"));
                    bill.put("customerPhone", rs.getString("customer_phone"));
                    bill.put("customerAddress", rs.getString("customer_address"));
                    bill.put("totalAmount", rs.getDouble("total_amount"));
                    bill.put("status", rs.getString("status"));
                    bill.put("orderDate", String.valueOf(rs.getTimestamp("order_date")));
                }
            }
            bill.put("items", getOrderItems(con, orderId));

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM payments WHERE order_id = ? ORDER BY payment_date DESC LIMIT 1")) {
                ps.setString(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> payment = new LinkedHashMap<>();
                        payment.put("paymentId", rs.getString("payment_id"));
                        payment.put("method", rs.getString("method"));
                        payment.put("amount", rs.getDouble("amount"));
                        payment.put("status", rs.getString("status"));
                        payment.put("paymentDate", String.valueOf(rs.getTimestamp("payment_date")));
                        bill.put("payment", payment);
                    } else {
                        bill.put("payment", null);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM deliveries WHERE order_id = ? LIMIT 1")) {
                ps.setString(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> delivery = new LinkedHashMap<>();
                        delivery.put("deliveryId", rs.getString("delivery_id"));
                        delivery.put("address", rs.getString("address"));
                        delivery.put("status", rs.getString("status"));
                        delivery.put("deliveryDate", rs.getDate("delivery_date") != null ? rs.getDate("delivery_date").toString() : null);
                        bill.put("delivery", delivery);
                    } else {
                        bill.put("delivery", null);
                    }
                }
            }
            return bill;
        }
    }

    public List<Map<String, Object>> getAllOrders() throws SQLException {
        return getOrders(null, null);
    }

    private List<Map<String, Object>> getOrders(String whereClause, String param) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT o.*, u.name AS customer_name FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                (whereClause != null ? whereClause + " " : "") +
                "ORDER BY o.order_date DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    String orderId = rs.getString("order_id");
                    m.put("orderId", orderId);
                    m.put("customerId", rs.getString("customer_id"));
                    m.put("customerName", rs.getString("customer_name"));
                    m.put("totalAmount", rs.getDouble("total_amount"));
                    m.put("status", rs.getString("status"));
                    m.put("orderDate", String.valueOf(rs.getTimestamp("order_date")));
                    m.put("items", getOrderItems(con, orderId));
                    list.add(m);
                }
            }
        }
        return list;
    }

    private List<Map<String, Object>> getOrderItems(Connection con, String orderId) throws SQLException {
        List<Map<String, Object>> items = new ArrayList<>();
        String sql = "SELECT oi.*, d.design_name, d.category, d.sketch_image FROM order_items oi " +
                "JOIN designs d ON oi.design_id = d.design_id WHERE oi.order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("orderItemId", rs.getString("order_item_id"));
                    m.put("designId", rs.getString("design_id"));
                    m.put("designName", rs.getString("design_name"));
                    m.put("category", rs.getString("category"));
                    m.put("sketchImage", rs.getString("sketch_image"));
                    m.put("size", rs.getString("size"));
                    m.put("quantity", rs.getInt("quantity"));
                    m.put("unitPrice", rs.getDouble("unit_price"));
                    m.put("customNote", rs.getString("custom_note"));
                    items.add(m);
                }
            }
        }
        return items;
    }

    public void updateStatus(String orderId, String status) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE orders SET status=? WHERE order_id=?")) {
            ps.setString(1, status);
            ps.setString(2, orderId);
            ps.executeUpdate();
        }
    }

    public void cancelOrder(String orderId) throws SQLException {
        updateStatus(orderId, "CANCELLED");
    }
}
