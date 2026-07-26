package com.fashiondesign.model;

public class Payment {
    private String paymentId;
    private String orderId;
    private PaymentMethod method;
    private double amount;
    private String status; // PENDING, COMPLETED, FAILED
    private String paymentDate;

    public Payment() {
    }

    public void processPayment() {
        this.status = "COMPLETED";
    }

    public String generateReceipt() {
        return "Receipt for order " + orderId + ": " + amount + " via " + method + " [" + status + "]";
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
}
