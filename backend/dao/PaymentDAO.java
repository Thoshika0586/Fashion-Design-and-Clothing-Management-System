package com.fashiondesign.dao;

import com.fashiondesign.util.IdGenerator;

import java.sql.*;
import java.util.*;

public class PaymentDAO {

    public Map<String, Object> makePayment(String orderId, String method, double amount) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String paymentId = IdGenerator.generate("PAY");
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO payments (payment_id, order_id, method, amount, status) VALUES (?,?,?,?,'COMPLETED')")) {
                    ps.setString(1, paymentId);
                    ps.setString(2, orderId);
                    ps.setString(3, method);
                    ps.setDouble(4, amount);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE orders SET status='CONFIRMED' WHERE order_id=?")) {
                    ps.setString(1, orderId);
                    ps.executeUpdate();
                }
                con.commit();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("paymentId", paymentId);
                m.put("orderId", orderId);
                m.put("method", method);
                m.put("amount", amount);
                m.put("status", "COMPLETED");
                return m;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Map<String, Object>> getPaymentsForOrder(String orderId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM payments WHERE order_id=? ORDER BY payment_date DESC")) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("paymentId", rs.getString("payment_id"));
                    m.put("orderId", rs.getString("order_id"));
                    m.put("method", rs.getString("method"));
                    m.put("amount", rs.getDouble("amount"));
                    m.put("status", rs.getString("status"));
                    m.put("paymentDate", String.valueOf(rs.getTimestamp("payment_date")));
                    list.add(m);
                }
            }
        }
        return list;
    }
}
