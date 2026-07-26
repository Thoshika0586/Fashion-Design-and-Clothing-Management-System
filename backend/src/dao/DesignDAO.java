package com.fashiondesign.dao;

import com.fashiondesign.util.IdGenerator;

import java.sql.*;
import java.util.*;

/** Manages the designer's sketches/creations (frocks and sarees). */
public class DesignDAO {

    public List<Map<String, Object>> getAllDesigns(String category) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT d.*, f.fabric_name, f.color AS fabric_color FROM designs d " +
                "LEFT JOIN fabrics f ON d.fabric_id = f.fabric_id WHERE d.status = 'AVAILABLE'" +
                (category != null && !category.isEmpty() ? " AND d.category = ?" : "") +
                " ORDER BY d.created_at DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (category != null && !category.isEmpty()) ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Map<String, Object> getDesignById(String designId) throws SQLException {
        String sql = "SELECT d.*, f.fabric_name, f.color AS fabric_color FROM designs d " +
                "LEFT JOIN fabrics f ON d.fabric_id = f.fabric_id WHERE d.design_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, designId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Map<String, Object> createDesign(String designerId, String designName, String category, String pattern,
                                              String fabricId, double price, String sketchImage, String description) throws SQLException {
        String designId = IdGenerator.generate("DSG");
        String sql = "INSERT INTO designs (design_id, designer_id, design_name, category, pattern, fabric_id, price, sketch_image, description, status) " +
                "VALUES (?,?,?,?,?,?,?,?,?, 'AVAILABLE')";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, designId);
            ps.setString(2, designerId);
            ps.setString(3, designName);
            ps.setString(4, category);
            ps.setString(5, pattern);
            ps.setString(6, fabricId);
            ps.setDouble(7, price);
            ps.setString(8, sketchImage);
            ps.setString(9, description);
            ps.executeUpdate();
        }
        return getDesignById(designId);
    }

    public void updateDesign(String designId, String designName, String category, String pattern,
                              String fabricId, double price, String sketchImage, String description, String status) throws SQLException {
        String sql = "UPDATE designs SET design_name=?, category=?, pattern=?, fabric_id=?, price=?, sketch_image=?, description=?, status=? WHERE design_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, designName);
            ps.setString(2, category);
            ps.setString(3, pattern);
            ps.setString(4, fabricId);
            ps.setDouble(5, price);
            ps.setString(6, sketchImage);
            ps.setString(7, description);
            ps.setString(8, status);
            ps.setString(9, designId);
            ps.executeUpdate();
        }
    }

    public void deleteDesign(String designId) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE designs SET status='ARCHIVED' WHERE design_id=?")) {
            ps.setString(1, designId);
            ps.executeUpdate();
        }
    }

    private Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("designId", rs.getString("design_id"));
        m.put("designerId", rs.getString("designer_id"));
        m.put("designName", rs.getString("design_name"));
        m.put("category", rs.getString("category"));
        m.put("pattern", rs.getString("pattern"));
        m.put("fabricId", rs.getString("fabric_id"));
        m.put("fabricName", rs.getString("fabric_name"));
        m.put("fabricColor", rs.getString("fabric_color"));
        m.put("price", rs.getDouble("price"));
        m.put("sketchImage", rs.getString("sketch_image"));
        m.put("description", rs.getString("description"));
        m.put("status", rs.getString("status"));
        return m;
    }
}
