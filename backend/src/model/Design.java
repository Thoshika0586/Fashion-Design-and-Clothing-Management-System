package com.fashiondesign.model;


public class Design {
    private String designId;
    private String designerId;
    private String designName;
    private String category;    // FROCK or SAREE
    private String pattern;
    private String fabricId;
    private double price;
    private String sketchImage;
    private String description;
    private String status;      // AVAILABLE or ARCHIVED

    public Design() {
    }

    public void createDesign() { /* persistence handled by DesignDAO */ }

    public void updateDesign() { /* persistence handled by DesignDAO */ }

    public String getDesignId() {
        return designId;
    }

    public void setDesignId(String designId) {
        this.designId = designId;
    }

    public String getDesignerId() {
        return designerId;
    }

    public void setDesignerId(String designerId) {
        this.designerId = designerId;
    }

    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getFabricId() {
        return fabricId;
    }

    public void setFabricId(String fabricId) {
        this.fabricId = fabricId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSketchImage() {
        return sketchImage;
    }

    public void setSketchImage(String sketchImage) {
        this.sketchImage = sketchImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
