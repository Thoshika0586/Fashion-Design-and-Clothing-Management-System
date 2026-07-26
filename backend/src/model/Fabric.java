package com.fashiondesign.model;

public class Fabric {
    private String fabricId;
    private String fabricName;
    private String color;
    private double pricePerUnit;

    public Fabric() {
    }

    public Fabric(String fabricId, String fabricName, String color, double pricePerUnit) {
        this.fabricId = fabricId;
        this.fabricName = fabricName;
        this.color = color;
        this.pricePerUnit = pricePerUnit;
    }

    public void addFabric() { /* persistence handled by FabricDAO */ }

    public void updateFabric() { /* persistence handled by FabricDAO */ }

    public String getFabricId() {
        return fabricId;
    }

    public void setFabricId(String fabricId) {
        this.fabricId = fabricId;
    }

    public String getFabricName() {
        return fabricName;
    }

    public void setFabricName(String fabricName) {
        this.fabricName = fabricName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
}
