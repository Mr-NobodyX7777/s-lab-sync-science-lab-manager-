package com.labmanager.dao;

import com.labmanager.db.Database;
import com.labmanager.model.Teacher;

import java.sql.*;
import java.util.*;

public class TeacherDAO {

    public List<Teacher> getAll() throws SQLException {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT * FROM teachers ORDER BY name";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Teacher(rs.getInt("teacher_id"), rs.getString("name"),
                        rs.getString("subject"), rs.getString("phone"), rs.getString("email")));
            }
        }
        return list;
    }

    public void insert(Teacher t) throws SQLException {
        String sql = "INSERT INTO teachers (name, subject, phone, email) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getSubject());
            ps.setString(3, t.getPhone());
            ps.setString(4, t.getEmail());
            ps.executeUpdate();
        }
    }

    public void update(Teacher t) throws SQLException {
        String sql = "UPDATE teachers SET name=?, subject=?, phone=?, email=? WHERE teacher_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getSubject());
            ps.setString(3, t.getPhone());
            ps.setString(4, t.getEmail());
            ps.setInt(5, t.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM teachers WHERE teacher_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
