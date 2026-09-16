package com.labmanager.dao;

import com.labmanager.db.Database;
import com.labmanager.model.Chemical;

import java.sql.*;
import java.util.*;

public class ChemicalDAO {

    public List<Chemical> getAll() throws SQLException {
        List<Chemical> list = new ArrayList<>();
        String sql = "SELECT * FROM chemicals ORDER BY name";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Chemical(rs.getInt("chemical_id"), rs.getString("name"), rs.getString("formula"),
                        rs.getDouble("quantity"), rs.getString("unit"), rs.getString("location"),
                        rs.getDate("expiry_date") == null ? "" : rs.getDate("expiry_date").toString(),
                        rs.getString("hazard_level")));
            }
        }
        return list;
    }

    public void insert(Chemical ch) throws SQLException {
        String sql = "INSERT INTO chemicals (name, formula, quantity, unit, location, expiry_date, hazard_level) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ch.getName());
            ps.setString(2, ch.getFormula());
            ps.setDouble(3, ch.getQuantity());
            ps.setString(4, ch.getUnit());
            ps.setString(5, ch.getLocation());
            if (ch.getExpiryDate() == null || ch.getExpiryDate().isBlank()) ps.setNull(6, Types.DATE);
            else ps.setDate(6, java.sql.Date.valueOf(ch.getExpiryDate()));
            ps.setString(7, ch.getHazardLevel());
            ps.executeUpdate();
        }
    }

    public void update(Chemical ch) throws SQLException {
        String sql = "UPDATE chemicals SET name=?, formula=?, quantity=?, unit=?, location=?, expiry_date=?, hazard_level=? WHERE chemical_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ch.getName());
            ps.setString(2, ch.getFormula());
            ps.setDouble(3, ch.getQuantity());
            ps.setString(4, ch.getUnit());
            ps.setString(5, ch.getLocation());
            if (ch.getExpiryDate() == null || ch.getExpiryDate().isBlank()) ps.setNull(6, Types.DATE);
            else ps.setDate(6, java.sql.Date.valueOf(ch.getExpiryDate()));
            ps.setString(7, ch.getHazardLevel());
            ps.setInt(8, ch.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM chemicals WHERE chemical_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
