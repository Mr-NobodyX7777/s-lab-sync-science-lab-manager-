package com.labmanager.dao;

import com.labmanager.db.Database;
import com.labmanager.model.Experiment;
import com.labmanager.model.Student;

import java.sql.*;
import java.util.*;

public class ExperimentDAO {

    public List<Experiment> getAll() throws SQLException {
        List<Experiment> list = new ArrayList<>();
        String sql = "SELECT e.*, t.name AS teacher_name, " +
                     "(SELECT COUNT(*) FROM experiment_students es WHERE es.experiment_id = e.experiment_id) AS student_count " +
                     "FROM experiments e " +
                     "LEFT JOIN teachers t ON e.teacher_id = t.teacher_id ORDER BY e.exp_date DESC, e.experiment_id DESC";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Experiment ex = new Experiment();
                ex.setId(rs.getInt("experiment_id"));
                ex.setTitle(rs.getString("title"));
                ex.setDescription(rs.getString("description"));
                ex.setExpDate(rs.getDate("exp_date") == null ? "" : rs.getDate("exp_date").toString());
                ex.setStartTime(rs.getString("start_time"));
                ex.setEndTime(rs.getString("end_time"));
                int tid = rs.getInt("teacher_id");
                ex.setTeacherId(rs.wasNull() ? null : tid);
                ex.setTeacherName(rs.getString("teacher_name"));
                ex.setStatus(rs.getString("status"));
                String color = rs.getString("color_hex");
                ex.setColorHex(color == null || color.isBlank() ? "#2EC4B6" : color);
                ex.setStudentCount(rs.getInt("student_count"));
                list.add(ex);
            }
        }
        return list;
    }

    public int insert(Experiment ex) throws SQLException {
        String sql = "INSERT INTO experiments (title, description, exp_date, start_time, end_time, teacher_id, status, color_hex) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, ex);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void update(Experiment ex) throws SQLException {
        String sql = "UPDATE experiments SET title=?, description=?, exp_date=?, start_time=?, end_time=?, teacher_id=?, status=?, color_hex=? WHERE experiment_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            fillStatement(ps, ex);
            ps.setInt(9, ex.getId());
            ps.executeUpdate();
        }
    }

    private void fillStatement(PreparedStatement ps, Experiment ex) throws SQLException {
        ps.setString(1, ex.getTitle());
        ps.setString(2, ex.getDescription());
        if (ex.getExpDate() == null || ex.getExpDate().isBlank()) ps.setNull(3, Types.DATE);
        else ps.setDate(3, java.sql.Date.valueOf(ex.getExpDate()));
        ps.setString(4, ex.getStartTime());
        ps.setString(5, ex.getEndTime());
        if (ex.getTeacherId() == null) ps.setNull(6, Types.INTEGER);
        else ps.setInt(6, ex.getTeacherId());
        ps.setString(7, ex.getStatus());
        ps.setString(8, ex.getColorHex() == null ? "#2EC4B6" : ex.getColorHex());
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM experiments WHERE experiment_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Replace the full list of students assigned to an experiment. */
    public void setStudents(int experimentId, List<Integer> studentIds) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement del = c.prepareStatement("DELETE FROM experiment_students WHERE experiment_id=?")) {
                del.setInt(1, experimentId);
                del.executeUpdate();
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO experiment_students (experiment_id, student_id) VALUES (?, ?)")) {
                for (int sid : studentIds) {
                    ins.setInt(1, experimentId);
                    ins.setInt(2, sid);
                    ins.addBatch();
                }
                if (!studentIds.isEmpty()) ins.executeBatch();
            }
            c.commit();
        }
    }

    public List<Student> getStudentsFor(int experimentId) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.* FROM students s JOIN experiment_students es ON s.student_id = es.student_id " +
                     "WHERE es.experiment_id = ? ORDER BY s.name";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, experimentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(rs.getInt("student_id"), rs.getString("name"),
                            rs.getString("class_grade"), rs.getString("roll_no"), rs.getString("phone")));
                }
            }
        }
        return list;
    }
}
