package com.labmanager.dao;

import com.labmanager.db.Database;
import com.labmanager.model.Equipment;

import java.sql.*;
import java.util.*;

public class EquipmentDAO {

    public List<Equipment> getAll() throws SQLException {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT * FROM equipment ORDER BY name";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Equipment(rs.getInt("equipment_id"), rs.getString("name"),
                        rs.getString("category"), rs.getInt("quantity"), rs.getString("location"),
                        rs.getString("condition_status"),
                        rs.getDate("last_maintenance") == null ? "" : rs.getDate("last_maintenance").toString()));
            }
        }
        return list;
    }

    public void insert(Equipment e) throws SQLException {
        String sql = "INSERT INTO equipment (name, category, quantity, location, condition_status, last_maintenance) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getCategory());
            ps.setInt(3, e.getQuantity());
            ps.setString(4, e.getLocation());
            ps.setString(5, e.getConditionStatus());
            if (e.getLastMaintenance() == null || e.getLastMaintenance().isBlank()) ps.setNull(6, Types.DATE);
            else ps.setDate(6, java.sql.Date.valueOf(e.getLastMaintenance()));
            ps.executeUpdate();
        }
    }

    public void update(Equipment e) throws SQLException {
        String sql = "UPDATE equipment SET name=?, category=?, quantity=?, location=?, condition_status=?, last_maintenance=? WHERE equipment_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getCategory());
            ps.setInt(3, e.getQuantity());
            ps.setString(4, e.getLocation());
            ps.setString(5, e.getConditionStatus());
            if (e.getLastMaintenance() == null || e.getLastMaintenance().isBlank()) ps.setNull(6, Types.DATE);
            else ps.setDate(6, java.sql.Date.valueOf(e.getLastMaintenance()));
            ps.setInt(7, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM equipment WHERE equipment_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
