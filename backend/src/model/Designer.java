package com.fashiondesign.model;

import java.util.ArrayList;
import java.util.List;


public class Designer extends User {
    private String designId;
    private String speciality;
    private List<Design> designs = new ArrayList<>();

    public Designer() {
    }

    public Designer(String userId, String name, String email, String password, String phone,
                    String address, String designId, String speciality) {
        super(userId, name, email, password, phone, address);
        this.designId = designId;
        this.speciality = speciality;
    }

    public Design createDesign(String designName, String category, String pattern,
                               String fabricId, double price, String sketchImage, String description) {
        Design d = new Design();
        d.setDesignerId(this.designId);
        d.setDesignName(designName);
        d.setCategory(category);
        d.setPattern(pattern);
        d.setFabricId(fabricId);
        d.setPrice(price);
        d.setSketchImage(sketchImage);
        d.setDescription(description);
        designs.add(d);
        return d;
    }

    public void updateDesign(Design design) {
        for (int i = 0; i < designs.size(); i++) {
            if (designs.get(i).getDesignId().equals(design.getDesignId())) {
                designs.set(i, design);
            }
        }
    }

    public List<Design> viewOrders() {
        return new ArrayList<>();
    }

    public void manageInventory() {
        // Delegates to InventoryDAO in the service layer.
    }

    @Override
    public String getRole() {
        return "DESIGNER";
    }

    public String getDesignId() {
        return designId;
    }

    public void setDesignId(String designId) {
        this.designId = designId;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }
}
