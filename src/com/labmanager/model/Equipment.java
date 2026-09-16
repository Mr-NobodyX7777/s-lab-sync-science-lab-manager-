package com.labmanager.model;
//This class represents a piece of equipment in the lab. It contains information such as the equipment's name, category, quantity, location, condition status
// and last maintenance date. This class is used to manage and track the equipment in the lab.
public class Equipment {
    private int id;
    private String name, category, location, conditionStatus, lastMaintenance;
    private int quantity;

    public Equipment() {}

    public Equipment(int id, String name, String category, int quantity, String location,
                      String conditionStatus, String lastMaintenance) {
        this.id = id; this.name = name; this.category = category; this.quantity = quantity;
        this.location = location; this.conditionStatus = conditionStatus; this.lastMaintenance = lastMaintenance;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getConditionStatus() { return conditionStatus; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }
    public String getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(String lastMaintenance) { this.lastMaintenance = lastMaintenance; }
}
