package com.labmanager.model;
//This class represents a chemical in the lab inventory, with properties such as name, formula, quantity, unit, location, expiry date, and hazard level. 
// It provides getters and setters for each property, allowing for easy manipulation and retrieval of chemical data within the application :) Mr.NobodyX7777
public class Chemical {
    private int id;
    private String name, formula, unit, location, expiryDate, hazardLevel;
    private double quantity;

    public Chemical() {}

    public Chemical(int id, String name, String formula, double quantity, String unit,
                     String location, String expiryDate, String hazardLevel) {
        this.id = id; this.name = name; this.formula = formula; this.quantity = quantity;
        this.unit = unit; this.location = location; this.expiryDate = expiryDate; this.hazardLevel = hazardLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getHazardLevel() { return hazardLevel; }
    public void setHazardLevel(String hazardLevel) { this.hazardLevel = hazardLevel; }
}
