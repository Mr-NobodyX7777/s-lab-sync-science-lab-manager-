package com.labmanager.db;

import java.sql.*;
// This class will probably do the database connection and configuration ;) Mr.NobodyX7777
public class Database {

    private static volatile String host = "localhost";
    private static volatile String port = "3306";
    private static final String DB_NAME = "lab_management";
    private static volatile String user = "root";
    private static volatile String password = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found on classpath. " +
                    "Place mysql-connector-j-x.x.x.jar in the 'lib' folder next to this app.");
        }
    }

    public static void configure(String host, String port, String user, String password) {
        Database.host = (host == null || host.isBlank()) ? "localhost" : host.trim();
        Database.port = (port == null || port.isBlank()) ? "3306" : port.trim();
        Database.user = user == null ? "" : user.trim();
        Database.password = password == null ? "" : password;
    }

    private static String baseUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static String dbUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + DB_NAME +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(baseUrl(), user, password);
    }

    //Normal connection to the lab_management database. 
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl(), user, password);
    }

   
    public static void testConnection(String host, String port, String user, String password) throws SQLException {
        String h = (host == null || host.isBlank()) ? "localhost" : host.trim();
        String p = (port == null || port.isBlank()) ? "3306" : port.trim();
        String u = user == null ? "" : user.trim();
        String url = "jdbc:mysql://" + h + ":" + p + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection ignored = DriverManager.getConnection(url, u, password == null ? "" : password)) {
            // connection succeeded; nothing else to do  O     O
            //                                            \___/ 
        }
    }

    public static String getDbName() {
        return DB_NAME;
    }

    public static String getHost() { return host; }
    public static String getPort() { return port; }
    public static String getUser() { return user; }
}
