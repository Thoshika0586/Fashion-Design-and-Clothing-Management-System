package com.fashiondesign.model;


public class Admin extends User {
    private String adminId;

    public Admin() {
    }

    public Admin(String userId, String name, String email, String password, String phone,
                 String address, String adminId) {
        super(userId, name, email, password, phone, address);
        this.adminId = adminId;
    }

    public void manageUsers() {
        // Delegates to UserDAO
    }

    public void manageOrders() {
        // Delegates to OrderDAO
    }

    public String generateReport() {
        return "report";
    }

    public void manageInventory() {
        // Delegates to InventoryDAO.
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
}
