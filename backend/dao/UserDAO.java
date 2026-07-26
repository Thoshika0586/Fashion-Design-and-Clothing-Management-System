package com.fashiondesign.dao;

import com.fashiondesign.util.IdGenerator;

import java.sql.*;
import java.util.*;

/** Handles login/registration and profile updates across Users, Customers, Designers, Admins. */
public class UserDAO {

    /** Returns a map with user + role-specific id if credentials match, otherwise null. */
    public Map<String, Object> login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUserMap(con, rs);
                }
            }
        }
        return null;
    }

    public Map<String, Object> registerCustomer(String name, String email, String password, String phone, String address) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String userId = IdGenerator.generate("U-CUS");
                String custId = IdGenerator.generate("CUS");
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (user_id, name, email, password, phone, address, role) VALUES (?,?,?,?,?,?,'CUSTOMER')")) {
                    ps.setString(1, userId);
                    ps.setString(2, name);
                    ps.setString(3, email);
                    ps.setString(4, password);
                    ps.setString(5, phone);
                    ps.setString(6, address);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO customers (customer_id, user_id) VALUES (?,?)")) {
                    ps.setString(1, custId);
                    ps.setString(2, userId);
                    ps.executeUpdate();
                }
                con.commit();
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("userId", userId);
                map.put("customerId", custId);
                map.put("name", name);
                map.put("email", email);
                map.put("role", "CUSTOMER");
                return map;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public void updateProfile(String userId, String name, String phone, String address) throws SQLException {
        String sql = "UPDATE users SET name = ?, phone = ?, address = ? WHERE user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setString(4, userId);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> getAllUsers() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT user_id, name, email, phone, address, role, created_at FROM users ORDER BY created_at DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("userId", rs.getString("user_id"));
                m.put("name", rs.getString("name"));
                m.put("email", rs.getString("email"));
                m.put("phone", rs.getString("phone"));
                m.put("address", rs.getString("address"));
                m.put("role", rs.getString("role"));
                m.put("createdAt", String.valueOf(rs.getTimestamp("created_at")));
                list.add(m);
            }
        }
        return list;
    }

    private Map<String, Object> buildUserMap(Connection con, ResultSet rs) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();
        String userId = rs.getString("user_id");
        String role = rs.getString("role");
        map.put("userId", userId);
        map.put("name", rs.getString("name"));
        map.put("email", rs.getString("email"));
        map.put("phone", rs.getString("phone"));
        map.put("address", rs.getString("address"));
        map.put("role", role);

        if ("CUSTOMER".equals(role)) {
            try (PreparedStatement ps2 = con.prepareStatement("SELECT customer_id FROM customers WHERE user_id = ?")) {
                ps2.setString(1, userId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) map.put("customerId", rs2.getString("customer_id"));
                }
            }
        } else if ("DESIGNER".equals(role)) {
            try (PreparedStatement ps2 = con.prepareStatement("SELECT design_er_id, speciality FROM designers WHERE user_id = ?")) {
                ps2.setString(1, userId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) {
                        map.put("designerId", rs2.getString("design_er_id"));
                        map.put("speciality", rs2.getString("speciality"));
                    }
                }
            }
        } else if ("ADMIN".equals(role)) {
            try (PreparedStatement ps2 = con.prepareStatement("SELECT admin_id FROM admins WHERE user_id = ?")) {
                ps2.setString(1, userId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) map.put("adminId", rs2.getString("admin_id"));
                }
            }
        }
        return map;
    }
}
