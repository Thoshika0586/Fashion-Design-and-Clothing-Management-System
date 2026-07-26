package com.fashiondesign.model;


public class Customer extends User {
    private String customerId;

    public Customer() {
    }

    public Customer(String userId, String name, String email, String password, String phone,
                    String address, String customerId) {
        super(userId, name, email, password, phone, address);
        this.customerId = customerId;
    }

    public Order placeOrders(Order order) {
        order.setCustomerId(this.customerId);
        return order;
    }

    public void viewOrders() {
        // Actual retrieval is done through OrderDAO.
    }

    public Payment makePayment(Payment payment) {
        payment.setStatus("COMPLETED");
        return payment;
    }

    public void updateProfile(String name, String phone, String address) {
        setName(name);
        setPhone(phone);
        setAddress(address);
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
