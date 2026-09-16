package com.labmanager.db;

import java.sql.*;

/**
 * Creates the database and all required tables automatically the first
 * time the application runs. Safe to call every startup (uses IF NOT EXISTS).
 * This section is the base of the whole program. But it got boring and repetitive to write all the SQL statements. Mr.NobodyX7777 ;) 
 */
public class SchemaInitializer {

    public static void initialize() throws SQLException {
        // 1. Making sure the database exists
        try (Connection conn = Database.getServerConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + Database.getDbName() +
                    " CHARACTER SET utf8mb4");
        }

        // 2. Create tables inside that database
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS teachers (
                    teacher_id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    subject VARCHAR(100),
                    phone VARCHAR(20),
                    email VARCHAR(100)
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS students (
                    student_id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    class_grade VARCHAR(30),
                    roll_no VARCHAR(30),
                    phone VARCHAR(20)
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS equipment (
                    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    category VARCHAR(100),
                    quantity INT DEFAULT 0,
                    location VARCHAR(150),
                    condition_status VARCHAR(50),
                    last_maintenance DATE
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS chemicals (
                    chemical_id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    formula VARCHAR(100),
                    quantity DOUBLE DEFAULT 0,
                    unit VARCHAR(20),
                    location VARCHAR(150),
                    expiry_date DATE,
                    hazard_level VARCHAR(50)
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS experiments (
                    experiment_id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(200) NOT NULL,
                    description VARCHAR(500),
                    exp_date DATE,
                    start_time VARCHAR(20),
                    end_time VARCHAR(20),
                    teacher_id INT,
                    status VARCHAR(30) DEFAULT 'Scheduled',
                    color_hex VARCHAR(7) DEFAULT '#2EC4B6',
                    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL
                )
            """);

            // Safe migration: if the app was run before this field existed, add it now.
            try {
                st.executeUpdate("ALTER TABLE experiments ADD COLUMN color_hex VARCHAR(7) DEFAULT '#2EC4B6'");
            } catch (SQLException alreadyExists) {
                // Column already present (MySQL error 1060) - don't do anything.
            }

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS experiment_students (
                    experiment_id INT NOT NULL,
                    student_id INT NOT NULL,
                    PRIMARY KEY (experiment_id, student_id),
                    FOREIGN KEY (experiment_id) REFERENCES experiments(experiment_id) ON DELETE CASCADE,
                    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
                )
            """);
            //O   O
            //\___/   <--- I know this looks ugly, but i don't care. I just wanted to add a little smiley face here. Mr.NobodyX7777 ;)
        }
    }
}
