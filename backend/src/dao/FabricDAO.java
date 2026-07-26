package com.fashiondesign.dao;

import com.fashiondesign.util.IdGenerator;

import java.sql.*;
import java.util.*;

public class FabricDAO {

    public List<Map<String, Object>> getAllFabrics() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM fabrics ORDER BY fabric_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Map<String, Object> addFabric(String fabricName, String color, double pricePerUnit) throws SQLException {
        String fabricId = IdGenerator.generate("FAB");
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO fabrics (fabric_id, fabric_name, color, price_per_unit) VALUES (?,?,?,?)")) {
            ps.setString(1, fabricId);
            ps.setString(2, fabricName);
            ps.setString(3, color);
            ps.setDouble(4, pricePerUnit);
            ps.executeUpdate();
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fabricId", fabricId);
        m.put("fabricName", fabricName);
        m.put("color", color);
        m.put("pricePerUnit", pricePerUnit);
        return m;
    }

    private Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fabricId", rs.getString("fabric_id"));
        m.put("fabricName", rs.getString("fabric_name"));
        m.put("color", rs.getString("color"));
        m.put("pricePerUnit", rs.getDouble("price_per_unit"));
        return m;
    }
}
