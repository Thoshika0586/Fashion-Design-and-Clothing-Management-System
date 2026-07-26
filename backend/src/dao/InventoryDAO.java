package com.fashiondesign.dao;

import com.fashiondesign.util.IdGenerator;

import java.sql.*;
import java.util.*;

public class InventoryDAO {

    public List<Map<String, Object>> getAllInventory() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM inventory ORDER BY item_type, item_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Map<String, Object> addItem(String itemType, String itemName, int quantity, double unitPrice) throws SQLException {
        String id = IdGenerator.generate("INV");
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO inventory (inventory_id, item_type, item_name, quantity, unit_price) VALUES (?,?,?,?,?)")) {
            ps.setString(1, id);
            ps.setString(2, itemType);
            ps.setString(3, itemName);
            ps.setInt(4, quantity);
            ps.setDouble(5, unitPrice);
            ps.executeUpdate();
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("inventoryId", id);
        m.put("itemType", itemType);
        m.put("itemName", itemName);
        m.put("quantity", quantity);
        m.put("unitPrice", unitPrice);
        return m;
    }

    public void updateStock(String inventoryId, int quantity) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE inventory SET quantity=? WHERE inventory_id=?")) {
            ps.setInt(1, quantity);
            ps.setString(2, inventoryId);
            ps.executeUpdate();
        }
    }

    public void deleteItem(String inventoryId) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM inventory WHERE inventory_id=?")) {
            ps.setString(1, inventoryId);
            ps.executeUpdate();
        }
    }

    private Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("inventoryId", rs.getString("inventory_id"));
        m.put("itemType", rs.getString("item_type"));
        m.put("itemName", rs.getString("item_name"));
        m.put("quantity", rs.getInt("quantity"));
        m.put("unitPrice", rs.getDouble("unit_price"));
        return m;
    }
}
