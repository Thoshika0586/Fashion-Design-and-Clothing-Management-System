package com.fashiondesign.dao;

import java.sql.*;
import java.util.*;

public class DeliveryDAO {

    public List<Map<String, Object>> getAllDeliveries() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT dv.*, u.name AS customer_name FROM deliveries dv " +
                "JOIN orders o ON dv.order_id = o.order_id " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id ORDER BY dv.delivery_id DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("deliveryId", rs.getString("delivery_id"));
                m.put("orderId", rs.getString("order_id"));
                m.put("customerName", rs.getString("customer_name"));
                m.put("address", rs.getString("address"));
                m.put("status", rs.getString("status"));
                m.put("deliveryDate", rs.getDate("delivery_date") != null ? rs.getDate("delivery_date").toString() : null);
                list.add(m);
            }
        }
        return list;
    }

    public void updateStatus(String deliveryId, String status, String deliveryDate) throws SQLException {
        String sql = "UPDATE deliveries SET status=?, delivery_date=? WHERE delivery_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            if (deliveryDate != null && !deliveryDate.isEmpty()) {
                ps.setDate(2, java.sql.Date.valueOf(deliveryDate));
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setString(3, deliveryId);
            ps.executeUpdate();
        }
    }
}
