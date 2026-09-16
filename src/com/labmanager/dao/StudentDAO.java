package com.labmanager.dao;

import com.labmanager.db.Database;
import com.labmanager.model.Student;

import java.sql.*;
import java.util.*;

public class StudentDAO {

    public List<Student> getAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY name";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(rs.getInt("student_id"), rs.getString("name"),
                        rs.getString("class_grade"), rs.getString("roll_no"), rs.getString("phone")));
            }
        }
        return list;
    }

    public void insert(Student s) throws SQLException {
        String sql = "INSERT INTO students (name, class_grade, roll_no, phone) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getClassGrade());
            ps.setString(3, s.getRollNo());
            ps.setString(4, s.getPhone());
            ps.executeUpdate();
        }
    }

    public void update(Student s) throws SQLException {
        String sql = "UPDATE students SET name=?, class_grade=?, roll_no=?, phone=? WHERE student_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getClassGrade());
            ps.setString(3, s.getRollNo());
            ps.setString(4, s.getPhone());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
